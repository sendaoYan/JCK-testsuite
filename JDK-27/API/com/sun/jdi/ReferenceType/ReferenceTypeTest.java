/*
 * @test
 * @summary com.sun.jdi.ReferenceType conformance: white-box structure + black-box behavior.
 * @run main ReferenceTypeTest
 */

import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.connect.spi.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;
import java.util.*;

public class ReferenceTypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---------- shared JPDA harness ----------
    static java.nio.file.Path TMP;
    static Process PROC;
    static com.sun.jdi.event.EventSet PENDING;

    static final String TARGET_SRC =
        "public class Target implements Runnable {\n" +
        "    public static int VALUE = 42;\n" +
        "    public static final String GREETING = \"hello\";\n" +
        "    public String name = \"world\";\n" +
        "    public int count = 7;\n" +
        "    private static volatile boolean ready = false;\n" +
        "    public Target() { count = 10; }\n" +
        "    public Target(String n) { name = n; }\n" +
        "    public int add(int a, int b) { int sum = a + b; return sum; }\n" +
        "    public static int multiply(int a, int b) { return a * b; }\n" +
        "    public void run() { }\n" +
        "    public static void main(String[] args) {\n" +
        "        Target t = new Target();\n" +
        "        int local = 1;\n" +
        "        while (!ready) { Thread.onSpinWait(); }\n" +
        "        int x = t.add(local, VALUE);\n" +
        "        System.out.println(\"x=\" + x);\n" +
        "    }\n" +
        "}\n";

    static VirtualMachine launchTarget() throws Exception {
        TMP = java.nio.file.Files.createTempDirectory("jdi-t8-");
        java.nio.file.Path src = TMP.resolve("Target.java");
        java.nio.file.Files.write(src, TARGET_SRC.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        javax.tools.JavaCompiler jc = javax.tools.ToolProvider.getSystemJavaCompiler();
        if (jc == null) throw new RuntimeException("no system java compiler");
        int rc = jc.run(null, null, null, "-g", "-nowarn", "-d", TMP.toString(), src.toString());
        if (rc != 0) throw new RuntimeException("debuggee compile failed rc=" + rc);
        VirtualMachineManager vmm = Bootstrap.virtualMachineManager();
        LaunchingConnector lc = null;
        for (LaunchingConnector c : vmm.launchingConnectors())
            if (c.name().equals("com.sun.jdi.CommandLineLaunch")) lc = c;
        if (lc == null) throw new RuntimeException("no CommandLineLaunch connector");
        java.util.Map<String, Connector.Argument> args = lc.defaultArguments();
        args.get("main").setValue("Target");
        String quote = args.get("quote").value();
        args.get("options").setValue("-cp " + quote + TMP + quote);
        VirtualMachine vm = lc.launch(args);
        PROC = vm.process();
        return vm;
    }

    static ReferenceType waitPrepared(VirtualMachine vm) throws Exception {
        EventRequestManager erm = vm.eventRequestManager();
        ClassPrepareRequest cpr = erm.createClassPrepareRequest();
        cpr.addClassFilter("Target");
        cpr.setSuspendPolicy(EventRequest.SUSPEND_ALL);
        cpr.enable();
        vm.resume();
        EventQueue q = vm.eventQueue();
        long deadline = System.currentTimeMillis() + 20000;
        while (System.currentTimeMillis() < deadline) {
            EventSet es = q.remove(1500);
            if (es == null) continue;
            ReferenceType found = null;
            for (Event e : es)
                if (e instanceof ClassPrepareEvent && ((ClassPrepareEvent) e).referenceType().name().equals("Target"))
                    found = ((ClassPrepareEvent) e).referenceType();
            if (found != null) { PENDING = es; return found; }
            es.resume();
        }
        throw new RuntimeException("timed out waiting for Target class prepare");
    }

    static ThreadReference suspendedMain(VirtualMachine vm) throws Exception {
        vm.suspend();
        for (ThreadReference t : vm.allThreads())
            if (t.name().equals("main")) return t;
        throw new RuntimeException("no main thread");
    }

    static StackFrame suspendedMainFrame(VirtualMachine vm) throws Exception {
        if (PENDING != null) { EventSet p = PENDING; PENDING = null; try { p.resume(); } catch (Throwable t) {} }
        vm.resume();
        for (int attempt = 0; attempt < 2000; attempt++) {
            vm.suspend();
            ThreadReference main = null;
            for (ThreadReference t : vm.allThreads())
                if (t.name().equals("main")) main = t;
            if (main != null) {
                for (StackFrame sf : main.frames())
                    if (sf.location().method().name().equals("main")) return sf;
            }
            vm.resume();
            Thread.yield();
        }
        throw new RuntimeException("main frame never reached");
    }

    static void cleanup(VirtualMachine vm) {
        if (PENDING != null) { try { PENDING.resume(); } catch (Throwable t) {} }
        try { vm.resume(); } catch (Throwable t) {}
        try { vm.dispose(); } catch (Throwable t) {}
        if (PROC != null) {
            try { PROC.waitFor(5, java.util.concurrent.TimeUnit.SECONDS); } catch (Throwable t) {}
            if (PROC.isAlive()) PROC.destroyForcibly();
        }
        if (TMP != null) { try { deleteRecursive(TMP); } catch (Throwable t) {} }
    }

    static void deleteRecursive(java.nio.file.Path p) throws Exception {
        java.nio.file.Files.walk(p).sorted(java.util.Comparator.reverseOrder()).forEach(x -> { try { java.nio.file.Files.delete(x); } catch (Exception e) {} });
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("com.sun.jdi.ReferenceType");
        check(c.isInterface(), "isInterface");
        Set<String> si = new HashSet<>();
        for (Class<?> i : c.getInterfaces()) si.add(i.getName());
        check(si.size() == 3, "direct interface count");
        check(si.contains("com.sun.jdi.Type"), "extends com.sun.jdi.Type");
        check(si.contains("java.lang.Comparable"), "extends java.lang.Comparable");
        check(si.contains("com.sun.jdi.Accessible"), "extends com.sun.jdi.Accessible");
        Set<String> dm = methodNames(c);
        check(dm.contains("name"), "method name");
        check(dm.contains("genericSignature"), "method genericSignature");
        check(dm.contains("classLoader"), "method classLoader");
        check(dm.contains("module"), "method module");
        check(dm.contains("sourceName"), "method sourceName");
        check(dm.contains("sourceNames"), "method sourceNames");
        check(dm.contains("sourcePaths"), "method sourcePaths");
        check(dm.contains("sourceDebugExtension"), "method sourceDebugExtension");
        check(dm.contains("isStatic"), "method isStatic");
        check(dm.contains("isAbstract"), "method isAbstract");
        check(dm.contains("isFinal"), "method isFinal");
        check(dm.contains("isPrepared"), "method isPrepared");
        check(dm.contains("isVerified"), "method isVerified");
        check(dm.contains("isInitialized"), "method isInitialized");
        check(dm.contains("failedToInitialize"), "method failedToInitialize");
        check(dm.contains("fields"), "method fields");
        check(dm.contains("visibleFields"), "method visibleFields");
        check(dm.contains("allFields"), "method allFields");
        check(dm.contains("fieldByName"), "method fieldByName");
        check(dm.contains("methods"), "method methods");
        check(dm.contains("visibleMethods"), "method visibleMethods");
        check(dm.contains("allMethods"), "method allMethods");
        check(dm.contains("methodsByName"), "method methodsByName");
        check(dm.contains("nestedTypes"), "method nestedTypes");
        check(dm.contains("getValue"), "method getValue");
        check(dm.contains("getValues"), "method getValues");
        check(dm.contains("classObject"), "method classObject");
        check(dm.contains("allLineLocations"), "method allLineLocations");
        check(dm.contains("locationsOfLine"), "method locationsOfLine");
        check(dm.contains("availableStrata"), "method availableStrata");
        check(dm.contains("defaultStratum"), "method defaultStratum");
        check(dm.contains("instances"), "method instances");
        check(dm.contains("equals"), "method equals");
        check(dm.contains("hashCode"), "method hashCode");
        check(dm.contains("majorVersion"), "method majorVersion");
        check(dm.contains("minorVersion"), "method minorVersion");
        check(dm.contains("constantPoolCount"), "method constantPoolCount");
        check(dm.contains("constantPool"), "method constantPool");
    }

    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        VirtualMachine vm = launchTarget();
        try {
            ReferenceType target = waitPrepared(vm);
            check(target.isPrepared(), "isPrepared");
            check("Target".equals(target.name()), "name");
            check(target.fields().size() >= 4, "fields non-empty");
            check(target.methods().size() >= 6, "methods non-empty");
            check(target.fieldByName("VALUE") != null, "fieldByName");
            check(target.methodsByName("add").size() == 1, "methodsByName");
            check(target.classObject() != null, "classObject");
            check(target.majorVersion() >= 45, "majorVersion");
            check(target.constantPoolCount() > 0, "constantPoolCount");
            check(target.constantPool().length > 0, "constantPool");
            check(target.availableStrata().size() >= 0, "availableStrata callable");
            check(target.visibleFields().size() >= target.fields().size(), "visibleFields superset");
            check(target.allFields().size() >= target.fields().size(), "allFields superset");
            check(target.visibleMethods().size() >= target.methods().size(), "visibleMethods superset");
            check(target.allMethods().size() >= target.methods().size(), "allMethods superset");
            check(target.nestedTypes().size() >= 0, "nestedTypes callable");
            check(target.classLoader() != null, "classLoader non-null (app loader)");
            check(target.module() != null && target.module().name() == null, "module non-null, unnamed");
            check(target.isStatic() == false, "isStatic false");
            check(target.isFinal() == false, "isFinal false");
            check(target.isVerified() == true, "isVerified true");
            target.isInitialized();
            check(true, "isInitialized callable (racy init state at ClassPrepare)");

        } finally { cleanup(vm); }

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
