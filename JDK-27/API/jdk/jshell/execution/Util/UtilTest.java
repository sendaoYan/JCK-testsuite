/*
 * @test
 * @summary jdk.jshell.execution.Util conformance: util static methods.
 * @run main/othervm UtilTest
 */

import java.lang.reflect.*;

public class UtilTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static void testWhiteBox() throws Exception {

        Class<?> c = Class.forName("jdk.jshell.execution.Util");
        check(!c.isInterface() && !java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete class");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        check(c.getMethod("forwardExecutionControl", jdk.jshell.spi.ExecutionControl.class, java.io.ObjectInput.class, java.io.ObjectOutput.class) != null, "forwardExecutionControl");
    }

    static void testBlackBox() throws Exception {
        check(true, "no safe instantiable behavior; white-box only");
    }


    // ---- deep contract: forwardExecutionControl implements the documented wire protocol ----
    static final int COMMAND_PREFIX = 0xC03DC03D;
    static final int RESULT_SUCCESS = 100, RESULT_TERMINATED = 101, RESULT_NOT_IMPLEMENTED = 102,
                     RESULT_INTERNAL_PROBLEM = 103, RESULT_USER_EXCEPTION = 104,
                     RESULT_USER_EXCEPTION_CHAINED = 108;

    public static int utilSeven() { return 7; }
    public static String utilBoom() { throw new IllegalStateException("boom"); }

    static class RecordingDelegate extends ClassLoader implements jdk.jshell.execution.LoaderDelegate {
        final java.util.List<String> log = new java.util.ArrayList<>();
        public void load(jdk.jshell.spi.ExecutionControl.ClassBytecodes[] cbcs) {
            log.add("load:" + cbcs.length + ":" + cbcs[0].name() + ":" + cbcs[0].bytecodes()[0]);
        }
        public void classesRedefined(jdk.jshell.spi.ExecutionControl.ClassBytecodes[] cbcs) { log.add("classesRedefined"); }
        public void addToClasspath(String p) { log.add("cp:" + p); }
        public Class<?> findClass(String name) throws ClassNotFoundException {
            return Class.forName(name, false, UtilTest.class.getClassLoader());
        }
    }

    static java.io.ObjectOutputStream buf(java.io.ByteArrayOutputStream b) throws Exception {
        return new java.io.ObjectOutputStream(b);
    }

    static void testFullMethodSurface() throws Exception {
        Class<?> c = Class.forName("jdk.jshell.execution.Util");
        // Util is a static-only holder: a single private constructor
        java.lang.reflect.Constructor<?>[] ctors = c.getDeclaredConstructors();
        check(ctors.length == 1 && java.lang.reflect.Modifier.isPrivate(ctors[0].getModifiers()),
              "single private constructor (never instantiated)");
        for (String n : new String[]{"forwardExecutionControl", "forwardExecutionControlAndIO",
                                     "remoteInputOutput", "detectJdiExitEvent"})
            { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals(n)) f=true; check(f, "has " + n); }
    }

    static void testCommandDispatchProtocol() throws Exception {
        // craft a deterministic command buffer: every command plus its failure channels
        java.io.ByteArrayOutputStream cmdBuf = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream co = new java.io.ObjectOutputStream(cmdBuf)) {
            co.writeInt(COMMAND_PREFIX); co.writeUTF("CMD_INVOKE"); co.writeUTF(UtilTest.class.getName()); co.writeUTF("utilSeven");
            co.writeInt(COMMAND_PREFIX); co.writeUTF("CMD_ADD_CLASSPATH"); co.writeUTF("/tmp/jck-util-cp");
            co.writeInt(COMMAND_PREFIX); co.writeUTF("CMD_LOAD");
            co.writeObject(new jdk.jshell.spi.ExecutionControl.ClassBytecodes[]{
                    new jdk.jshell.spi.ExecutionControl.ClassBytecodes("Util.Fake", new byte[]{5}) });
            co.writeInt(COMMAND_PREFIX); co.writeUTF("CMD_INVOKE"); co.writeUTF(UtilTest.class.getName()); co.writeUTF("nope");
            co.writeInt(COMMAND_PREFIX); co.writeUTF("CMD_INVOKE"); co.writeUTF(UtilTest.class.getName()); co.writeUTF("utilBoom");
            co.writeInt(COMMAND_PREFIX); co.writeUTF("CMD_REDEFINE");
            co.writeObject(new jdk.jshell.spi.ExecutionControl.ClassBytecodes[0]);
            co.writeInt(COMMAND_PREFIX); co.writeUTF("CMD_WEIRD"); co.writeObject("arg");
        }
        RecordingDelegate del = new RecordingDelegate();
        java.io.ByteArrayOutputStream respBuf = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream ro = new java.io.ObjectOutputStream(respBuf)) {
            // EOF after the buffered commands ends the loop cleanly
            jdk.jshell.execution.Util.forwardExecutionControl(
                    new jdk.jshell.execution.DirectExecutionControl(del),
                    new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(cmdBuf.toByteArray())), ro);
        }
        java.io.ObjectInputStream ri = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(respBuf.toByteArray()));
        check(ri.readInt() == RESULT_SUCCESS, "invoke success status");
        check(ri.readUTF().equals("7"), "invoke result string");
        check(ri.readInt() == RESULT_SUCCESS, "addToClasspath success status");
        check(ri.readInt() == RESULT_SUCCESS, "load success status");
        check(ri.readInt() == RESULT_INTERNAL_PROBLEM, "unknown method -> RESULT_INTERNAL_PROBLEM");
        check(ri.readUTF().contains("NoSuchMethodException"), "internal-problem message carries the cause");
        int chained = ri.readInt();
        check(chained == RESULT_USER_EXCEPTION_CHAINED, "user exception -> chained status");
        check(ri.readInt() == RESULT_USER_EXCEPTION, "chain head is RESULT_USER_EXCEPTION");
        check(ri.readUTF().equals("boom"), "user exception message");
        check(ri.readUTF().equals("java.lang.IllegalStateException"), "user exception cause class");
        check(((Object[]) ri.readObject()).length > 0, "user exception stack trace serialized");
        check(ri.readInt() == RESULT_SUCCESS, "chain terminator is RESULT_SUCCESS");
        check(ri.readInt() == RESULT_NOT_IMPLEMENTED, "redefine -> RESULT_NOT_IMPLEMENTED");
        check(ri.readUTF().equals("redefine not supported"), "redefine message");
        check(ri.readInt() == RESULT_NOT_IMPLEMENTED, "unknown command -> RESULT_NOT_IMPLEMENTED");
        check(ri.readUTF().equals("Unknown command: CMD_WEIRD"), "unknown command message names it");
        check(del.log.contains("load:1:Util.Fake:5"), "CMD_LOAD delivered bytecodes verbatim");
        check(del.log.contains("cp:/tmp/jck-util-cp"), "CMD_ADD_CLASSPATH delivered the path");
        check(ri.available() == 0, "all response bytes consumed");
    }

    static void testBadPrefixTerminates() throws Exception {
        java.io.ByteArrayOutputStream cmdBuf = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream co = new java.io.ObjectOutputStream(cmdBuf)) {
            co.writeInt(123); co.writeUTF("CMD_INVOKE"); co.writeUTF("a"); co.writeUTF("b");
        }
        java.io.ByteArrayOutputStream respBuf = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream ro = new java.io.ObjectOutputStream(respBuf)) {
            jdk.jshell.execution.Util.forwardExecutionControl(
                    new jdk.jshell.execution.DirectExecutionControl(new RecordingDelegate()),
                    new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(cmdBuf.toByteArray())), ro);
        }
        java.io.ObjectInputStream r = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(respBuf.toByteArray()));
        check(r.readInt() == RESULT_TERMINATED, "bad prefix -> RESULT_TERMINATED");
        check(r.readUTF().equals("Invalid command prefix: 123"), "bad prefix message exact");
        check(r.available() == 0, "engine reports termination only once");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testFullMethodSurface();
        testCommandDispatchProtocol();
        testBadPrefixTerminates();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
