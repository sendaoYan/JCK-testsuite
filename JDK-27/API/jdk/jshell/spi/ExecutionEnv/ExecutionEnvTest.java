/*
 * @test
 * @summary jdk.jshell.spi.ExecutionEnv conformance: execution env interface.
 * @modules jdk.jshell
 * @run main ExecutionEnvTest
 */

import java.lang.reflect.*;
import java.util.*;

public class ExecutionEnvTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static void testWhiteBox() throws Exception {

        Class<?> c = Class.forName("jdk.jshell.spi.ExecutionEnv");
        check(c.isInterface(), "is interface");
        check(Modifier.isPublic(c.getModifiers()), "is public");
    }

    static void testBlackBox() throws Exception {
        final java.io.ByteArrayInputStream in = new java.io.ByteArrayInputStream(new byte[]{1, 2, 3});
        final java.io.ByteArrayOutputStream outb = new java.io.ByteArrayOutputStream();
        final java.io.ByteArrayOutputStream errb = new java.io.ByteArrayOutputStream();
        final java.io.PrintStream out = new java.io.PrintStream(outb, true, java.nio.charset.StandardCharsets.UTF_8);
        final java.io.PrintStream err = new java.io.PrintStream(errb, true, java.nio.charset.StandardCharsets.UTF_8);
        final List<String> opts = List.of("-Xmx256m");
        final java.util.Optional<jdk.jshell.JShellConsole> consoleOpt = java.util.Optional.empty();
        final boolean[] closed = new boolean[]{false};

        jdk.jshell.spi.ExecutionEnv env = new jdk.jshell.spi.ExecutionEnv() {
            public java.io.InputStream userIn() { return in; }
            public java.io.PrintStream userOut() { return out; }
            public java.io.PrintStream userErr() { return err; }
            public List<String> extraRemoteVMOptions() { return opts; }
            public void closeDown() { closed[0] = true; }
            public java.util.Optional<jdk.jshell.JShellConsole> console() { return consoleOpt; }
        };

        check(env.userIn() == in, "userIn() returns the configured stream");
        check(env.userOut() == out, "userOut() returns the configured stream");
        check(env.userErr() == err, "userErr() returns the configured stream");
        check(env.extraRemoteVMOptions().equals(opts), "extraRemoteVMOptions() returns the configured list");
        check(env.console().equals(consoleOpt), "console() returns the configured optional");

        env.closeDown();
        check(closed[0], "closeDown() was invoked");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
