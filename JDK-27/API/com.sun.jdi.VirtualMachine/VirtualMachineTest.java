/*
 * @test
 * @summary com.sun.jdi.VirtualMachine conformance: white-box structure + black-box behavior.
 * @run main VirtualMachineTest
 */

import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.connect.spi.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;
import java.util.*;

public class VirtualMachineTest {
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
        Class<?> c = Class.forName("com.sun.jdi.VirtualMachine");
        check(c.isInterface(), "isInterface");
        Set<String> si = new HashSet<>();
        for (Class<?> i : c.getInterfaces()) si.add(i.getName());
        check(si.size() == 1, "direct interface count");
        check(si.contains("com.sun.jdi.Mirror"), "extends com.sun.jdi.Mirror");
        Set<String> dm = methodNames(c);
        check(dm.contains("allModules"), "method allModules");
        check(dm.contains("classesByName"), "method classesByName");
        check(dm.contains("allClasses"), "method allClasses");
        check(dm.contains("redefineClasses"), "method redefineClasses");
        check(dm.contains("allThreads"), "method allThreads");
        check(dm.contains("suspend"), "method suspend");
        check(dm.contains("resume"), "method resume");
        check(dm.contains("topLevelThreadGroups"), "method topLevelThreadGroups");
        check(dm.contains("eventQueue"), "method eventQueue");
        check(dm.contains("eventRequestManager"), "method eventRequestManager");
        check(dm.contains("mirrorOf"), "method mirrorOf");
        check(dm.contains("mirrorOfVoid"), "method mirrorOfVoid");
        check(dm.contains("process"), "method process");
        check(dm.contains("dispose"), "method dispose");
        check(dm.contains("exit"), "method exit");
        check(dm.contains("canWatchFieldModification"), "method canWatchFieldModification");
        check(dm.contains("canWatchFieldAccess"), "method canWatchFieldAccess");
        check(dm.contains("canGetBytecodes"), "method canGetBytecodes");
        check(dm.contains("canGetSyntheticAttribute"), "method canGetSyntheticAttribute");
        check(dm.contains("canGetOwnedMonitorInfo"), "method canGetOwnedMonitorInfo");
        check(dm.contains("canGetCurrentContendedMonitor"), "method canGetCurrentContendedMonitor");
        check(dm.contains("canGetMonitorInfo"), "method canGetMonitorInfo");
        check(dm.contains("canUseInstanceFilters"), "method canUseInstanceFilters");
        check(dm.contains("canRedefineClasses"), "method canRedefineClasses");
        check(dm.contains("canAddMethod"), "method canAddMethod");
        check(dm.contains("canUnrestrictedlyRedefineClasses"), "method canUnrestrictedlyRedefineClasses");
        check(dm.contains("canPopFrames"), "method canPopFrames");
        check(dm.contains("canGetSourceDebugExtension"), "method canGetSourceDebugExtension");
        check(dm.contains("canRequestVMDeathEvent"), "method canRequestVMDeathEvent");
        check(dm.contains("canGetMethodReturnValues"), "method canGetMethodReturnValues");
        check(dm.contains("canGetInstanceInfo"), "method canGetInstanceInfo");
        check(dm.contains("canUseSourceNameFilters"), "method canUseSourceNameFilters");
        check(dm.contains("canForceEarlyReturn"), "method canForceEarlyReturn");
        check(dm.contains("canBeModified"), "method canBeModified");
        check(dm.contains("canRequestMonitorEvents"), "method canRequestMonitorEvents");
        check(dm.contains("canGetMonitorFrameInfo"), "method canGetMonitorFrameInfo");
        check(dm.contains("canGetClassFileVersion"), "method canGetClassFileVersion");
        check(dm.contains("canGetConstantPool"), "method canGetConstantPool");
        check(dm.contains("canGetModuleInfo"), "method canGetModuleInfo");
        check(dm.contains("setDefaultStratum"), "method setDefaultStratum");
        check(dm.contains("getDefaultStratum"), "method getDefaultStratum");
        check(dm.contains("instanceCounts"), "method instanceCounts");
        check(dm.contains("description"), "method description");
        check(dm.contains("version"), "method version");
        check(dm.contains("name"), "method name");
        check(dm.contains("setDebugTraceMode"), "method setDebugTraceMode");
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
            check(vm.name() != null && vm.name().length() > 0, "name non-empty");
            check(vm.version() != null && vm.version().contains("27"), "version contains 27");
            check(vm.description() != null && vm.description().length() > 0, "description non-empty");
            vm.setDefaultStratum("Java");
            check("Java".equals(vm.getDefaultStratum()), "setDefaultStratum/getDefaultStratum round-trip");
            check(vm.allThreads().size() > 0, "allThreads non-empty");
            check(vm.topLevelThreadGroups().size() > 0, "topLevelThreadGroups non-empty");
            List<ReferenceType> all = vm.allClasses();
            check(all.size() > 0, "allClasses non-empty");
            check(vm.classesByName("java.lang.String").size() >= 1, "classesByName(java.lang.String)");
            check(vm.mirrorOf(true).value() == true, "mirrorOf(boolean)");
            check(vm.mirrorOf((byte) 7).value() == 7, "mirrorOf(byte)");
            check(vm.mirrorOf('x').value() == 'x', "mirrorOf(char)");
            check(vm.mirrorOf((short) 5).value() == 5, "mirrorOf(short)");
            check(vm.mirrorOf(123).value() == 123, "mirrorOf(int)");
            check(vm.mirrorOf(123L).value() == 123L, "mirrorOf(long)");
            check(vm.mirrorOf(1.5f).value() == 1.5f, "mirrorOf(float)");
            check(vm.mirrorOf(2.5d).value() == 2.5d, "mirrorOf(double)");
            check("hello".equals(vm.mirrorOf("hello").value()), "mirrorOf(String)");
            check(vm.mirrorOfVoid() != null, "mirrorOfVoid");
            check(vm.eventQueue() != null, "eventQueue non-null");
            check(vm.eventRequestManager() != null, "eventRequestManager non-null");
            check(vm.process() != null, "process non-null");
            vm.setDebugTraceMode(VirtualMachine.TRACE_NONE);
            List<ReferenceType> strs = vm.classesByName("java.lang.String");
            long[] counts = vm.instanceCounts(strs);
            check(counts.length == strs.size(), "instanceCounts length matches");
            boolean sawBase = false;
            for (ModuleReference mr : vm.allModules()) if ("java.base".equals(mr.name())) sawBase = true;
            check(sawBase, "allModules contains java.base");
            vm.suspend();
            vm.resume();
            check(true, "suspend/resume round-trip");

        } finally { cleanup(vm); }

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
