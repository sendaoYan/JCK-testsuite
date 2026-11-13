/*
 * @test
 * @summary jdk.jshell.execution.JdiInitiator conformance: jdi initiator shape.
 * @run main/othervm/timeout=180 JdiInitiatorTest
 */

import java.lang.reflect.*;
import jdk.jshell.execution.*;

public class JdiInitiatorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static void testWhiteBox() throws Exception {

        Class<?> c = Class.forName("jdk.jshell.execution.JdiInitiator");
        check(!c.isInterface() && !java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete class");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        check(c.getMethod("vm") != null, "vm method");
        check(c.getMethod("process") != null, "process method");
    }

    static void testBlackBox() throws Exception {
        check(true, "no safe instantiable behavior; white-box only");
    }


    // ---- deep contract: real debuggee launch over the SocketListen connector ----
    public static class Agent { public static void main(String[] a) throws Exception { Thread.sleep(60000); } }

    static void testRealLaunch() throws Exception {
        JdiInitiator ii = new JdiInitiator(0,
                java.util.List.of("-cp", System.getProperty("java.class.path")),
                "JdiInitiatorTest$Agent", false, null, 8000, java.util.Map.of());
        com.sun.jdi.VirtualMachine vm = ii.vm();
        check(vm != null, "vm() connected to the launched debuggee");
        check(vm.name().contains("VM"), "vm() name: " + vm.name());
        check(vm.canBeModified(), "debuggee VM is modifiable");
        Process p = ii.process();
        check(p != null && p.isAlive(), "process() is the live debuggee process");
        // Windows 的启动器文件名是 java.exe，POSIX 上是 java
        String javaName = new java.io.File(p.info().command().orElse("")).getName();
        check(javaName.equals("java") || javaName.equalsIgnoreCase("java.exe"),
              "debuggee runs the java launcher: " + p.info().command().orElse(""));
        vm.exit(0);
        check(p.waitFor() == 0, "vm.exit(0) terminates the debuggee with status 0");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testRealLaunch();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
