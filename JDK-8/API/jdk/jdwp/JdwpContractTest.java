/*
 * @test
 * @summary JDWP / JDI contract: launch a server=y JDWP debuggee, attach with
 *          com.sun.jdi.SocketAttach, observe VMStartEvent and ClassPrepare, hit
 *          a breakpoint in a known method, and read stack frames + local vars.
 * @library /lib
 * @run main/othervm JdwpContractTest
 */

import com.sun.jdi.Bootstrap;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VirtualMachineManager;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventQueue;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.VMStartEvent;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Conformance test for the JDWP debugging path via the JDI client.
 *
 * A child debuggee is launched with the raw JDWP agent
 * (-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=localhost:0);
 * its ephemeral port is read from stdout and the test attaches with
 * com.sun.jdi.SocketAttach.  The test then walks VMStartEvent, ClassPrepare,
 * a breakpoint hit, and stack-frame/local-variable reads.
 */
public class JdwpContractTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean jdiAvailable() {
        try {
            Class.forName("com.sun.jdi.Bootstrap");
            Bootstrap.virtualMachineManager();
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    static final String DEBUGGEE_SRC =
        "public class Debuggee {\n" +
        "    public static void main(String[] args) {\n" +
        "        Debuggee d = new Debuggee();\n" +
        "        int r = d.compute(6, 7);\n" +
        "        System.out.println(\"RESULT=\" + r);\n" +
        "    }\n" +
        "    public int compute(int a, int b) {\n" +
        "        int sum = a + b;\n" +
        "        return sum;\n" +
        "    }\n" +
        "}\n";

    // ------------------------------------------------------------------
    // Build helpers
    // ------------------------------------------------------------------
    static Path jdkHome() {
        Path p = Paths.get(System.getProperty("java.home"));
        return p.endsWith("jre") ? p.getParent() : p;
    }

    static Path javaBinary() {
        return Paths.get(System.getProperty("java.home")).resolve("bin").resolve("java");
    }

    static Path javacBinary() {
        return jdkHome().resolve("bin").resolve("javac");
    }

    static byte[] readAll(java.io.InputStream in) throws java.io.IOException {
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
        return out.toByteArray();
    }

    static String runCollect(ProcessBuilder pb) throws Exception {
        pb.redirectErrorStream(true);
        Process p = pb.start();
        byte[] all = readAll(p.getInputStream());
        if (!p.waitFor(120, TimeUnit.SECONDS)) {
            p.destroyForcibly();
            throw new AssertionError("process did not finish within 120s");
        }
        String out = new String(all, "UTF-8");
        if (p.exitValue() != 0)
            throw new AssertionError("process failed rc=" + p.exitValue() + " output: " + out);
        return out;
    }

    // Reads the debuggee's stdout until the JDWP "Listening ... address: <port>"
    // line appears, and returns the parsed port.
    static int readListenPort(Process p, StringBuilder captured) throws Exception {
        java.io.InputStream in = p.getInputStream();
        long deadline = System.currentTimeMillis() + 20000;
        while (System.currentTimeMillis() < deadline) {
            while (in.available() > 0) {
                int c = in.read();
                if (c < 0) return -1;
                captured.append((char) c);
            }
            int idx = captured.indexOf("Listening for transport dt_socket at address: ");
            if (idx >= 0) {
                String rest = captured.substring(
                        idx + "Listening for transport dt_socket at address: ".length()).trim();
                int sp = rest.indexOf(' ');
                if (sp >= 0) rest = rest.substring(0, sp);
                try { return Integer.parseInt(rest.trim()); } catch (NumberFormatException e) { return -1; }
            }
            Thread.sleep(40);
        }
        return -1;
    }

    // ------------------------------------------------------------------
    public static void main(String[] args) throws Exception {
        if (!jdiAvailable())
            throw new jtreg.SkippedException("JDI (com.sun.jdi) unavailable on this JDK");

        Path dir = Paths.get(System.getProperty("user.dir"));
        Path debuggeeFile = dir.resolve("Debuggee.java");
        Files.write(debuggeeFile, DEBUGGEE_SRC.getBytes("UTF-8"));
        runCollect(new ProcessBuilder(javacBinary().toString(),
                "-g", "-d", dir.toString(), debuggeeFile.toString()));

        Process debuggee = new ProcessBuilder(javaBinary().toString(),
                "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=localhost:0",
                "-cp", dir.toString(), "Debuggee").redirectErrorStream(true).start();
        StringBuilder captured = new StringBuilder();
        VirtualMachine vm = null;
        try {
            int port = readListenPort(debuggee, captured);
            check(port > 0, "JDWP agent reported a listen port (got " + port + ")");

            VirtualMachineManager vmm = Bootstrap.virtualMachineManager();
            AttachingConnector socketAttach = null;
            for (AttachingConnector c : vmm.attachingConnectors())
                if (c.name().equals("com.sun.jdi.SocketAttach")) socketAttach = c;
            if (socketAttach == null)
                throw new AssertionError("no com.sun.jdi.SocketAttach connector");
            Map<String, Connector.Argument> cargs = socketAttach.defaultArguments();
            cargs.get("hostname").setValue("localhost");
            cargs.get("port").setValue(String.valueOf(port));
            vm = socketAttach.attach(cargs);
            check(vm != null, "SocketAttach connected to the debuggee");

            EventQueue queue = vm.eventQueue();
            EventRequestManager erm = vm.eventRequestManager();
            // 先在挂起状态下登记 ClassPrepareRequest，再恢复 VMStart 事件集：
            // 恢复之后调试目标可能在请求生效前就完成加载并退出。
            ClassPrepareRequest cpr = erm.createClassPrepareRequest();
            cpr.addClassFilter("Debuggee");
            cpr.setSuspendPolicy(EventRequest.SUSPEND_ALL);
            cpr.enable();

            boolean sawVmStart = false;
            EventSet initial = queue.remove(3000);
            if (initial != null) {
                for (Event e : initial)
                    if (e instanceof VMStartEvent) sawVmStart = true;
                initial.resume();
            }
            check(sawVmStart, "attach delivered a VMStartEvent");
            // 不要再补一次 VM 级 resume：initial.resume() 已恢复 VMStart 事件集，
            // 而 VM.Resume 会清掉所有线程的挂起计数；目标若在此空隙触发
            // ClassPrepare 的 SUSPEND_ALL 就会被放行跑完，后续 methods() 抛
            // VMDisconnectedException（-Xcomp 下实测稳定命中）。

            ReferenceType debuggeeType = waitClassPrepared(queue);
            check(debuggeeType != null, "Debuggee class prepared");

            Method compute = null;
            for (Method m : debuggeeType.methods())
                if (m.name().equals("compute")) compute = m;
            check(compute != null, "Debuggee.compute(int,int) found");
            Location firstLine = compute.allLineLocations().get(0);

            BreakpointRequest bpr = erm.createBreakpointRequest(firstLine);
            bpr.setSuspendPolicy(EventRequest.SUSPEND_ALL);
            bpr.enable();
            vm.resume();

            ThreadReference suspended = waitBreakpoint(queue);
            check(suspended != null, "breakpoint in compute() hit");

            List<StackFrame> frames = suspended.frames();
            check(frames.size() >= 2, "call stack has compute + main (size=" + frames.size() + ")");
            StackFrame top = frames.get(0);
            check("compute".equals(top.location().method().name()), "top frame is compute");
            check("main".equals(frames.get(1).location().method().name()), "next frame is main");

            LocalVariable aVar = null, bVar = null;
            for (LocalVariable lv : top.visibleVariables()) {
                if ("a".equals(lv.name())) aVar = lv;
                if ("b".equals(lv.name())) bVar = lv;
            }
            check(aVar != null, "local variable a visible in compute");
            check(bVar != null, "local variable b visible in compute");
            int aValue = ((IntegerValue) top.getValue(aVar)).value();
            int bValue = ((IntegerValue) top.getValue(bVar)).value();
            check(aValue == 6, "local a == 6");
            check(bValue == 7, "local b == 7");

            vm.resume();
        } finally {
            if (vm != null) {
                try { vm.resume(); } catch (Throwable ignore) { }
                try { vm.dispose(); } catch (Throwable ignore) { }
            }
        }

        if (!debuggee.waitFor(30, TimeUnit.SECONDS)) debuggee.destroyForcibly();
        captured.append(new String(readAll(debuggee.getInputStream()), "UTF-8"));
        check(debuggee.exitValue() == 0, "debuggee exited cleanly (rc=" + debuggee.exitValue() + ")");
        check(captured.toString().contains("RESULT=13"), "debuggee printed RESULT=13 after resume");

        System.out.println("PASS (" + passed + " assertions)");
    }

    static ReferenceType waitClassPrepared(EventQueue queue) throws Exception {
        long deadline = System.currentTimeMillis() + 20000;
        while (System.currentTimeMillis() < deadline) {
            EventSet es = queue.remove(1500);
            if (es == null) continue;
            ReferenceType found = null;
            for (Event e : es)
                if (e instanceof ClassPrepareEvent
                        && "Debuggee".equals(((ClassPrepareEvent) e).referenceType().name()))
                    found = ((ClassPrepareEvent) e).referenceType();
            if (found != null) return found;
            es.resume();
        }
        return null;
    }

    static ThreadReference waitBreakpoint(EventQueue queue) throws Exception {
        long deadline = System.currentTimeMillis() + 20000;
        while (System.currentTimeMillis() < deadline) {
            EventSet es = queue.remove(1500);
            if (es == null) continue;
            ThreadReference found = null;
            for (Event e : es)
                if (e instanceof BreakpointEvent) found = ((BreakpointEvent) e).thread();
            if (found != null) return found;
            es.resume();
        }
        return null;
    }
}
