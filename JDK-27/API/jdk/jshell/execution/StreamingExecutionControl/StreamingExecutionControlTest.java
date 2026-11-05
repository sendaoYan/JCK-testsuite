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


    // ---- deep contract: channel-type requirement and a real two-thread wire loop ----
    static class DefiningDelegate extends ClassLoader implements jdk.jshell.execution.LoaderDelegate {
        final java.util.List<String> log = new java.util.ArrayList<>();
        final java.util.Map<String, Class<?>> defined = new java.util.HashMap<>();
        DefiningDelegate() { super(StreamingExecutionControlTest.class.getClassLoader()); }
        public void load(jdk.jshell.spi.ExecutionControl.ClassBytecodes[] cbcs) {
            // log the wire payload only: real class definition happens for the cancel class
            for (jdk.jshell.spi.ExecutionControl.ClassBytecodes c : cbcs) {
                log.add("load:" + c.name() + ":" + c.bytecodes()[0]);
            }
        }
        public void classesRedefined(jdk.jshell.spi.ExecutionControl.ClassBytecodes[] cbcs) { log.add("redef"); }
        public void addToClasspath(String p) { log.add("cp:" + p); }
        public Class<?> findClass(String name) throws ClassNotFoundException {
            Class<?> c = defined.get(name);
            if (c != null) return c;
            return Class.forName(name, false, StreamingExecutionControlTest.class.getClassLoader());
        }
    }

    public static int wireSeven() { return 7; }

    static void testConstructorRequiresObjectInputStream() throws Exception {
        java.io.ObjectOutput out = new java.io.ObjectOutputStream(new java.io.ByteArrayOutputStream());
        java.io.ObjectInput fakeIn = (java.io.ObjectInput) java.lang.reflect.Proxy.newProxyInstance(
                StreamingExecutionControlTest.class.getClassLoader(),
                new Class<?>[]{java.io.ObjectInput.class}, (p, m, a) -> null);
        try {
            new jdk.jshell.execution.StreamingExecutionControl(out, fakeIn);
            check(false, "non-ObjectInputStream input must be rejected");
        } catch (IllegalArgumentException expected) {
            check(expected.getMessage().contains("must be an ObjectInputStream"),
                  "constructor rejects non-ObjectInputStream: " + expected.getMessage());
        }
    }

    static void testRealWireLoop() throws Exception {
        java.io.PipedInputStream cmdPi = new java.io.PipedInputStream();
        java.io.PipedOutputStream cmdPo = new java.io.PipedOutputStream(cmdPi);
        java.io.PipedInputStream respPi = new java.io.PipedInputStream();
        java.io.PipedOutputStream respPo = new java.io.PipedOutputStream(respPi);
        java.io.ObjectOutputStream cmdOut = new java.io.ObjectOutputStream(cmdPo);
        java.io.ObjectOutputStream respOut = new java.io.ObjectOutputStream(respPo);
        java.io.ObjectInputStream cmdIn = new java.io.ObjectInputStream(cmdPi);
        java.io.ObjectInputStream respIn = new java.io.ObjectInputStream(respPi);
        DefiningDelegate del = new DefiningDelegate();
        Thread back = new Thread(() -> jdk.jshell.execution.Util.forwardExecutionControl(
                new jdk.jshell.execution.DirectExecutionControl(del), cmdIn, respOut));
        back.setDaemon(true);
        back.start();
        jdk.jshell.execution.StreamingExecutionControl front =
                new jdk.jshell.execution.StreamingExecutionControl(cmdOut, respIn);
        String self = StreamingExecutionControlTest.class.getName();
        check(front.invoke(self, "wireSeven").equals("7"), "wire invoke returns the user value");
        // ClassBytecodes travel over the wire intact (name and first byte)
        front.load(new jdk.jshell.spi.ExecutionControl.ClassBytecodes[]{
                new jdk.jshell.spi.ExecutionControl.ClassBytecodes("Wire.Fake", new byte[]{42}) });
        check(del.log.contains("load:Wire.Fake:42"), "wire load delivers bytecodes verbatim");
        front.addToClasspath("/tmp/jck-sec-wire");
        check(del.log.contains("cp:/tmp/jck-sec-wire"), "wire addToClasspath reaches the delegate");
        front.stop(); // fire-and-forget: no response is read
        check(true, "wire stop() does not wait for a response");
        try {
            front.extensionCommand("cmd", "arg");
            check(false, "front extensionCommand must throw NotImplementedException");
        } catch (jdk.jshell.spi.ExecutionControl.NotImplementedException expected) {
            check(expected.getMessage().equals("extensionCommand not implemented."), "front extensionCommand message exact");
        }
        try {
            front.invoke(self, "nope");
            check(false, "wire unknown method must throw InternalException");
        } catch (jdk.jshell.spi.ExecutionControl.InternalException expected) {
            check(expected.getMessage().contains("NoSuchMethodException"), "wire unknown method -> InternalException");
        }
        front.close();
        back.join(5000);
        check(!back.isAlive(), "CMD_CLOSE terminates the command loop");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testConstructorRequiresObjectInputStream();
        testRealWireLoop();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
