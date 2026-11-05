/*
 * @test
 * @summary java.lang.instrument.Instrumentation conformance: white-box structure + black-box behavior.
 * @run main InstrumentationTest
 */
import java.lang.instrument.*;
import java.util.*;
import java.util.jar.JarFile;

public class InstrumentationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.instrument.Instrumentation");
        check(c.isInterface(), "interface");
        check(c.getMethod("addTransformer", ClassFileTransformer.class, boolean.class).getReturnType() == void.class,
              "addTransformer(ClassFileTransformer,boolean)");
        check(c.getMethod("removeTransformer", ClassFileTransformer.class).getReturnType() == boolean.class,
              "removeTransformer returns boolean");
        check(c.getMethod("isRetransformClassesSupported").getReturnType() == boolean.class,
              "isRetransformClassesSupported returns boolean");
        check(c.getMethod("retransformClasses", Class[].class).getReturnType() == void.class,
              "retransformClasses returns void");
        check(c.getMethod("isRedefineClassesSupported").getReturnType() == boolean.class,
              "isRedefineClassesSupported returns boolean");
        check(c.getMethod("getAllLoadedClasses").getReturnType() == Class[].class, "getAllLoadedClasses returns Class[]");
        check(c.getMethod("getObjectSize", Object.class).getReturnType() == long.class, "getObjectSize returns long");
        check(c.getMethod("isNativeMethodPrefixSupported").getReturnType() == boolean.class,
              "isNativeMethodPrefixSupported returns boolean");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Instrumentation inst = new Instrumentation() {
            public void addTransformer(ClassFileTransformer t, boolean canRetransform) { }
            public void addTransformer(ClassFileTransformer t) { }
            public boolean removeTransformer(ClassFileTransformer t) { return true; }
            public boolean isRetransformClassesSupported() { return true; }
            public void retransformClasses(Class<?>... classes) { }
            public boolean isRedefineClassesSupported() { return false; }
            public void redefineClasses(ClassDefinition... defs) { }
            public boolean isModifiableClass(Class<?> theClass) { return theClass == String.class; }
            public Class[] getAllLoadedClasses() { return new Class[]{String.class, Integer.class}; }
            public Class[] getInitiatedClasses(ClassLoader loader) { return new Class[0]; }
            public long getObjectSize(Object objectToSize) { return 16L; }
            public void appendToBootstrapClassLoaderSearch(JarFile jarfile) { }
            public void appendToSystemClassLoaderSearch(JarFile jarfile) { }
            public boolean isNativeMethodPrefixSupported() { return false; }
            public void setNativeMethodPrefix(ClassFileTransformer transformer, String prefix) { }
            public void redefineModule(Module module, Set<Module> extraReads, Map<String, Set<Module>> extraExports,
                                       Map<String, Set<Module>> extraOpens, Set<Class<?>> extraUses,
                                       Map<Class<?>, List<Class<?>>> extraProvides) { }
            public boolean isModifiableModule(Module module) { return false; }
        };

        check(inst.isRetransformClassesSupported(), "isRetransformClassesSupported roundtrip");
        check(!inst.isRedefineClassesSupported(), "isRedefineClassesSupported roundtrip");
        check(inst.isModifiableClass(String.class), "isModifiableClass roundtrip");
        check(!inst.isModifiableClass(Integer.class), "isModifiableClass false roundtrip");
        Class<?>[] loaded = inst.getAllLoadedClasses();
        check(loaded.length == 2 && loaded[0] == String.class && loaded[1] == Integer.class,
              "getAllLoadedClasses roundtrip");
        check(inst.getInitiatedClasses(null).length == 0, "getInitiatedClasses roundtrip");
        check(inst.getObjectSize(new Object()) == 16L, "getObjectSize roundtrip");
        check(!inst.isNativeMethodPrefixSupported(), "isNativeMethodPrefixSupported roundtrip");
        check(inst.removeTransformer(null), "removeTransformer roundtrip");
        check(!inst.isModifiableModule(String.class.getModule()), "isModifiableModule roundtrip");
        // void methods are callable no-ops
        inst.addTransformer(null);
        inst.addTransformer(null, true);
        try { inst.retransformClasses(String.class); } catch (UnmodifiableClassException expected) { }
        try { inst.redefineClasses(new ClassDefinition(String.class, new byte[0])); }
        catch (ClassNotFoundException | UnmodifiableClassException expected) { }
        inst.setNativeMethodPrefix(null, "$$prefix$$");
    }


    static void testDeepModuleSurfacePresent() throws Exception {
        boolean hasRedefineModule = false;
        boolean hasIsModifiableModule = false;
        for (java.lang.reflect.Method m : Instrumentation.class.getMethods()) {
            if (m.getName().equals("redefineModule") && m.getParameterTypes().length == 6) {
                hasRedefineModule = true;
            }
            if (m.getName().equals("isModifiableModule") && m.getParameterTypes().length == 1) {
                hasIsModifiableModule = true;
            }
        }
        check(hasRedefineModule, "JDK 27 Instrumentation has redefineModule(Module,...)");
        check(hasIsModifiableModule, "JDK 27 Instrumentation has isModifiableModule(Module)");
        check(Class.forName("java.lang.instrument.UnmodifiableModuleException") != null,
              "UnmodifiableModuleException present in JDK 27");
        check(Instrumentation.class.getMethod("isModifiableModule", Module.class)
                .getReturnType() == boolean.class, "isModifiableModule returns boolean");
        check(Instrumentation.class.getMethod("redefineModule", Module.class,
                java.util.Set.class, java.util.Map.class, java.util.Map.class,
                java.util.Set.class, java.util.Map.class).getReturnType() == void.class,
              "redefineModule returns void");
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
        testWhiteBox();
        testBlackBox();
        testDeepModuleSurfacePresent();
        testDeepAgentClosedLoop();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
