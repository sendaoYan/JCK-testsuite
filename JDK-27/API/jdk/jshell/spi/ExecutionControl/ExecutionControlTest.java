/*
 * @test
 * @summary jdk.jshell.spi.ExecutionControl conformance: execution control interface.
 * @modules jdk.jshell
 * @run main ExecutionControlTest
 */

import java.lang.reflect.*;
import java.util.*;

public class ExecutionControlTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static void testWhiteBox() throws Exception {

        Class<?> c = Class.forName("jdk.jshell.spi.ExecutionControl");
        check(c.isInterface(), "is interface");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        check(java.lang.AutoCloseable.class.isAssignableFrom(c), "extends AutoCloseable");
    }

    static jdk.jshell.spi.ExecutionEnv envFixture() {
        return new jdk.jshell.spi.ExecutionEnv() {
            public java.io.InputStream userIn() { return java.io.InputStream.nullInputStream(); }
            public java.io.PrintStream userOut() { return new java.io.PrintStream(java.io.OutputStream.nullOutputStream()); }
            public java.io.PrintStream userErr() { return new java.io.PrintStream(java.io.OutputStream.nullOutputStream()); }
            public List<String> extraRemoteVMOptions() { return List.of(); }
            public void closeDown() { }
        };
    }

    static void testBlackBox() throws Throwable {
        // Real JDK implementation of the ExecutionControl SPI, invoked through the
        // interface type so each call-site targets jdk.jshell.spi.ExecutionControl.
        jdk.jshell.spi.ExecutionControl ec = new jdk.jshell.execution.LocalExecutionControl();
        check(ec instanceof jdk.jshell.execution.LocalExecutionControl, "real LocalExecutionControl instance");

        // load() with an empty array is a no-op success.
        ec.load(new jdk.jshell.spi.ExecutionControl.ClassBytecodes[0]);

        // redefine() is not supported by DirectExecutionControl -> NotImplementedException.
        try {
            ec.redefine(new jdk.jshell.spi.ExecutionControl.ClassBytecodes[0]);
            check(false, "redefine() should throw NotImplementedException");
        } catch (jdk.jshell.spi.ExecutionControl.NotImplementedException expected) {
            check(true, "redefine() throws NotImplementedException");
        }

        // invoke() on an unknown method of a real class -> InternalException.
        try {
            ec.invoke("java.lang.String", "noSuchMethod");
            check(false, "invoke() should throw InternalException");
        } catch (jdk.jshell.spi.ExecutionControl.InternalException expected) {
            check(true, "invoke() throws InternalException");
        }

        // varValue() on an unknown field -> InternalException.
        try {
            ec.varValue("java.lang.String", "noSuchField");
            check(false, "varValue() should throw InternalException");
        } catch (jdk.jshell.spi.ExecutionControl.InternalException expected) {
            check(true, "varValue() throws InternalException");
        }

        // addToClasspath() completes on a non-existent path (URL formed from File, no IO).
        ec.addToClasspath("/tmp" + java.io.File.separator + "jck-ec");

        // stop() with no running user code returns normally.
        ec.stop();

        // extensionCommand() on an unknown command -> NotImplementedException.
        try {
            ec.extensionCommand("noSuchCommand", null);
            check(false, "extensionCommand() should throw NotImplementedException");
        } catch (jdk.jshell.spi.ExecutionControl.NotImplementedException expected) {
            check(true, "extensionCommand() throws NotImplementedException");
        }

        ec.close();

        // Static generate() resolves the registered "local" provider via ServiceLoader.
        jdk.jshell.spi.ExecutionEnv env = envFixture();
        jdk.jshell.spi.ExecutionControl g1 = jdk.jshell.spi.ExecutionControl.generate(env, "local");
        check(g1 != null, "generate(env, spec) returns an engine");
        g1.close();
        jdk.jshell.spi.ExecutionControl g2 =
            jdk.jshell.spi.ExecutionControl.generate(env, "local", new HashMap<String, String>());
        check(g2 != null, "generate(env, name, params) returns an engine");
        g2.close();
    }


    // ---- deep contract: provider resolution and spec-string grammar ----
    static void testGenerateSpecGrammar() throws Throwable {
        final Class<?> EC = jdk.jshell.spi.ExecutionControl.class;
        check(EC != null, "ExecutionControl resolved");
        jdk.jshell.spi.ExecutionEnv env = envFixture();
        // a bare name resolves the registered provider
        check(jdk.jshell.spi.ExecutionControl.generate(env, "local") instanceof jdk.jshell.execution.LocalExecutionControl,
              "spec 'local' resolves the local provider");
        // a trailing colon with no parameters is tolerated
        check(jdk.jshell.spi.ExecutionControl.generate(env, "local:") != null, "spec 'local:' accepted (empty parameter list)");
        // whitespace around separators is skipped
        check(jdk.jshell.spi.ExecutionControl.generate(env, " local : ") != null, "whitespace skipped in spec");
        // unknown provider name -> IllegalArgumentException
        try {
            jdk.jshell.spi.ExecutionControl.generate(env, "nosuchxyz");
            check(false, "unknown provider must throw");
        } catch (IllegalArgumentException expected) {
            check(expected.getMessage().contains("nosuchxyz"), "unknown provider message names it");
        }
        // parameter key not offered by the provider -> IllegalArgumentException
        try {
            jdk.jshell.spi.ExecutionControl.generate(env, "local", java.util.Map.of("bogusKey", "v"));
            check(false, "unknown parameter key must throw");
        } catch (IllegalArgumentException expected) {
            check(expected.getMessage().contains("bogusKey"), "unknown key message names it");
        }
        // malformed spec strings
        try { jdk.jshell.spi.ExecutionControl.generate(env, "=x"); check(false, "spec '=x' must throw"); }
        catch (IllegalArgumentException expected) { check(expected.getMessage().contains("Expected identifier"), "=x -> Expected identifier"); }
        try { jdk.jshell.spi.ExecutionControl.generate(env, "local:bad"); check(false, "spec 'local:bad' must throw"); }
        catch (IllegalArgumentException expected) { check(expected.getMessage().contains("Expected '('"), "missing '(' reported"); }
    }

    static void testRegisteredProviders() throws Throwable {
        java.util.Set<String> names = new java.util.TreeSet<>();
        for (jdk.jshell.spi.ExecutionControlProvider p : java.util.ServiceLoader.load(jdk.jshell.spi.ExecutionControlProvider.class))
            names.add(p.name());
        check(names.contains("local"), "local provider registered");
        check(names.contains("jdi"), "jdi provider registered");
        check(names.contains("failover"), "failover provider registered");
        check(names.size() >= 3, "at least three providers installed: " + names);
    }

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        testGenerateSpecGrammar();
        testRegisteredProviders();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
