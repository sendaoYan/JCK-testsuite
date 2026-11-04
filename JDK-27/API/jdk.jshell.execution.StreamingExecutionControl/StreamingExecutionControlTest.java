/*
 * @test
 * @summary jdk.jshell.execution.StreamingExecutionControl conformance: streaming execution control.
 * @modules jdk.jshell
 * @run main StreamingExecutionControlTest
 */

import java.lang.reflect.*;

public class StreamingExecutionControlTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static void testWhiteBox() throws Exception {

        Class<?> c = Class.forName("jdk.jshell.execution.StreamingExecutionControl");
        check(!c.isInterface() && !java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete class");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        check(jdk.jshell.spi.ExecutionControl.class.isAssignableFrom(c), "implements ExecutionControl");
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static boolean throwsEngineTermination(ThrowingRunnable r) {
        try {
            r.run();
            return false;
        } catch (jdk.jshell.spi.ExecutionControl.EngineTerminationException expected) {
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    static void testBlackBox() throws Exception {
        // Command sink and an empty response channel (only the serialization stream
        // header, so the ObjectInput is constructible). Every command that expects a
        // response observes EOF and deterministically reports EngineTerminationException.
        java.io.ByteArrayOutputStream bout = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutput out = new java.io.ObjectOutputStream(bout);
        byte[] header = new byte[]{(byte) 0xAC, (byte) 0xED, 0x00, 0x05};
        java.io.ObjectInput in = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(header));

        jdk.jshell.execution.StreamingExecutionControl sec =
            new jdk.jshell.execution.StreamingExecutionControl(out, in);
        check(sec instanceof jdk.jshell.spi.ExecutionControl, "implements ExecutionControl");

        // stop() and close() only write the command and flush; no response is read.
        sec.stop();
        sec.close();

        jdk.jshell.spi.ExecutionControl.ClassBytecodes[] empty =
            new jdk.jshell.spi.ExecutionControl.ClassBytecodes[0];

        check(throwsEngineTermination(() -> sec.load(empty)),
              "load() -> EngineTerminationException with no responder");
        check(throwsEngineTermination(() -> sec.redefine(empty)),
              "redefine() -> EngineTerminationException with no responder");
        check(throwsEngineTermination(() -> sec.addToClasspath("/tmp/jck-sec")),
              "addToClasspath() -> EngineTerminationException with no responder");
        check(throwsEngineTermination(() -> sec.invoke("a.C", "m")),
              "invoke() -> EngineTerminationException with no responder");
        check(throwsEngineTermination(() -> sec.varValue("a.C", "v")),
              "varValue() -> EngineTerminationException with no responder");
        // In JDK-27 extensionCommand() is not streamed; it directly reports
        // NotImplementedException.
        try {
            sec.extensionCommand("cmd", "arg");
            check(false, "extensionCommand() should throw NotImplementedException");
        } catch (jdk.jshell.spi.ExecutionControl.NotImplementedException expected) {
            check(true, "extensionCommand() throws NotImplementedException");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
