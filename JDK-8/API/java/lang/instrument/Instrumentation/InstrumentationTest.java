/*
 * @test
 * @summary java.lang.instrument.Instrumentation conformance: interface method surface.
 * @run main InstrumentationTest
 */

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class InstrumentationTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testInstrumentation() {
        check(Instrumentation.class.isInterface(), "is interface");
        Set<String> names = new HashSet<String>();
        for (Method m : Instrumentation.class.getMethods()) {
            names.add(m.getName());
        }
        String[] required = {
            "addTransformer", "removeTransformer", "isRetransformClassesSupported",
            "retransformClasses", "isRedefineClassesSupported", "redefineClasses",
            "isModifiableClass", "getAllLoadedClasses", "getInitiatedClasses",
            "getObjectSize", "appendToBootstrapClassLoaderSearch",
            "appendToSystemClassLoaderSearch", "isNativeMethodPrefixSupported",
            "setNativeMethodPrefix"
        };
        for (String name : required) {
            check(names.contains(name), "has method " + name);
        }
    }


    static void testDeepModuleSurfaceAbsent() throws Exception {
        // JDK 8: no module instrumentation surface, no UnmodifiableModuleException
        boolean hasRedefineModule = false;
        boolean hasIsModifiableModule = false;
        for (java.lang.reflect.Method m : Instrumentation.class.getMethods()) {
            if (m.getName().equals("redefineModule")) {
                hasRedefineModule = true;
            }
            if (m.getName().equals("isModifiableModule")) {
                hasIsModifiableModule = true;
            }
        }
        check(!hasRedefineModule, "JDK 8 Instrumentation has no redefineModule");
        check(!hasIsModifiableModule, "JDK 8 Instrumentation has no isModifiableModule");
        try {
            Class.forName("java.lang.instrument.UnmodifiableModuleException");
            check(false, "UnmodifiableModuleException absent in JDK 8");
        } catch (ClassNotFoundException expected) {
            check(true, "UnmodifiableModuleException absent in JDK 8");
        }
        check(java.lang.instrument.ClassFileTransformer.class.getMethods().length == 1,
              "JDK 8 ClassFileTransformer has exactly one (abstract) transform method");
    }

    static void testDeepAgentClosedLoop() throws Exception {
        String classesDirName = System.getProperty("test.classes", ".");
        java.io.File classesDir = new java.io.File(classesDirName);
        java.io.File classFile = new java.io.File(classesDir, "InstrumentationTest$AgentLife.class");
        check(classFile.isFile(), "nested AgentLife.class compiled into test.classes: " + classFile);
        java.io.File tmp = java.nio.file.Files.createTempDirectory("jck-agent").toFile();
        try {
            java.io.File jar = new java.io.File(tmp, "agent.jar");
            java.util.jar.Manifest mf = new java.util.jar.Manifest();
            java.util.jar.Attributes attrs = mf.getMainAttributes();
            attrs.put(java.util.jar.Attributes.Name.MANIFEST_VERSION, "1.0");
            attrs.putValue("Premain-Class", "InstrumentationTest$AgentLife");
            attrs.putValue("Can-Redefine-Classes", "true");
            attrs.putValue("Can-Retransform-Classes", "true");
            attrs.putValue("Can-Set-Native-Method-Prefix", "true");
            java.io.FileOutputStream fos = new java.io.FileOutputStream(jar);
            java.util.jar.JarOutputStream jos = new java.util.jar.JarOutputStream(fos, mf);
            jos.putNextEntry(new java.util.jar.JarEntry("InstrumentationTest$AgentLife.class"));
            java.io.FileInputStream in = new java.io.FileInputStream(classFile);
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) {
                jos.write(buf, 0, n);
            }
            in.close();
            jos.closeEntry();
            jos.close();
            check(jar.length() > 0, "agent jar built with JarOutputStream: " + jar.length() + " bytes");

            String javaBin = new java.io.File(
                    new java.io.File(System.getProperty("java.home"), "bin"), "java").getAbsolutePath();
            ProcessBuilder pb = new ProcessBuilder(javaBin,
                    "-javaagent:" + jar.getAbsolutePath(),
                    "-cp", classesDir.getAbsolutePath(),
                    "InstrumentationTest$AgentLife");
            pb.redirectErrorStream(true);
            Process proc = pb.start();
            java.util.Map<String, String> marks =
                    new java.util.HashMap<String, String>();
            StringBuilder out = new StringBuilder();
            java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(proc.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line).append('\n');
                if (line.startsWith("AGENT ")) {
                    int eq = line.indexOf('=');
                    if (eq > 0) {
                        marks.put(line.substring(6, eq), line.substring(eq + 1));
                    }
                }
            }
            reader.close();
            check(proc.waitFor(120, java.util.concurrent.TimeUnit.SECONDS), "child JVM terminated");
            check(proc.exitValue() == 0, "child JVM exit code 0, output: " + out);
            check("true".equals(marks.get("DONE")), "child ran to completion");
            check("true".equals(marks.get("redefine")), "child: isRedefineClassesSupported()=true");
            check("true".equals(marks.get("retransform")), "child: isRetransformClassesSupported()=true");
            check("true".equals(marks.get("nativePrefix")), "child: isNativeMethodPrefixSupported()=true");
            check("true".equals(marks.get("modifiableString")), "child: isModifiableClass(String.class)=true");
            check("false".equals(marks.get("modifiableArray")), "child: isModifiableClass(int[].class)=false");
            check("false".equals(marks.get("modifiablePrim")), "child: isModifiableClass(int.class)=false");
            check(Integer.parseInt(marks.get("allLoaded")) > 0, "child: getAllLoadedClasses() non-empty");
            check(Integer.parseInt(marks.get("initiatedBoot")) > 0, "child: getInitiatedClasses(boot) non-empty");
            check("true".equals(marks.get("initiatedHasObject")),
                  "child: boot-initiated classes contain java.lang.Object");
            check(Long.parseLong(marks.get("objectSize")) > 0, "child: getObjectSize() > 0");
            check("true".equals(marks.get("removedNoop")), "child: removeTransformer(registered)=true");
            check("true".equals(marks.get("mainAlive")), "child main reached");
            check(Integer.parseInt(marks.get("transformCalls")) >= 1,
                  "child: registered transformer observed >= 1 later class load");
            check("false".equals(marks.get("removeUnknown")),
                  "child: removeTransformer(never registered)=false");
        } finally {
            deleteRecursively(tmp);
        }
    }

    static void deleteRecursively(java.io.File f) {
        java.io.File[] children = f.listFiles();
        if (children != null) {
            for (java.io.File c : children) {
                deleteRecursively(c);
            }
        }
        f.delete();
    }


    // Nested agent loaded via -javaagent in a child JVM (see testDeepAgentClosedLoop).
    // Kept nested so jtreg never picks it up as a standalone test file.
    public static class AgentLife {
        static volatile int transformCalls;
        static volatile Instrumentation saved;

        public static void premain(String agentArgs, Instrumentation inst) {
            saved = inst;
            final java.lang.instrument.ClassFileTransformer counting =
                    new java.lang.instrument.ClassFileTransformer() {
                public byte[] transform(ClassLoader loader, String name, Class<?> being,
                                        java.security.ProtectionDomain pd, byte[] buf) {
                    transformCalls++;
                    return null; // null = leave the class file untouched
                }
            };
            inst.addTransformer(counting, true); // throws UOE if retransform unsupported
            final java.lang.instrument.ClassFileTransformer noop =
                    new java.lang.instrument.ClassFileTransformer() {
                public byte[] transform(ClassLoader loader, String name, Class<?> being,
                                        java.security.ProtectionDomain pd, byte[] buf) {
                    return null;
                }
            };
            inst.addTransformer(noop);
            boolean removedNoop = inst.removeTransformer(noop);
            int all = inst.getAllLoadedClasses().length;
            int boot = 0;
            boolean hasObject = false;
            for (Class<?> c : inst.getInitiatedClasses(null)) {
                boot++;
                if (c == Object.class) {
                    hasObject = true;
                }
            }
            System.out.println("AGENT redefine=" + inst.isRedefineClassesSupported());
            System.out.println("AGENT retransform=" + inst.isRetransformClassesSupported());
            System.out.println("AGENT nativePrefix=" + inst.isNativeMethodPrefixSupported());
            System.out.println("AGENT modifiableString=" + inst.isModifiableClass(String.class));
            System.out.println("AGENT modifiableArray=" + inst.isModifiableClass(int[].class));
            System.out.println("AGENT modifiablePrim=" + inst.isModifiableClass(int.class));
            System.out.println("AGENT allLoaded=" + all);
            System.out.println("AGENT initiatedBoot=" + boot);
            System.out.println("AGENT initiatedHasObject=" + hasObject);
            System.out.println("AGENT objectSize=" + inst.getObjectSize(new Object()));
            System.out.println("AGENT removedNoop=" + removedNoop);
        }

        public static void main(String[] args) {
            System.out.println("AGENT mainAlive=true");
            // classes defined after premain (including this main class) flowed through
            // the registered counting transformer
            System.out.println("AGENT transformCalls=" + transformCalls);
            boolean removedUnknown = saved.removeTransformer(
                    new java.lang.instrument.ClassFileTransformer() {
                public byte[] transform(ClassLoader loader, String name, Class<?> being,
                                        java.security.ProtectionDomain pd, byte[] buf) {
                    return null;
                }
            });
            System.out.println("AGENT removeUnknown=" + removedUnknown);
            System.out.println("AGENT DONE=true");
        }
    }

    public static void main(String[] args) throws Exception {
        testInstrumentation();
        testDeepModuleSurfaceAbsent();
        testDeepAgentClosedLoop();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
