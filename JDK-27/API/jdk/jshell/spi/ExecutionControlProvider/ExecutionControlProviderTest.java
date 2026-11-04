/*
 * @test
 * @summary jdk.jshell.spi.ExecutionControlProvider conformance: execution control provider interface.
 * @modules jdk.jshell
 * @run main ExecutionControlProviderTest
 */

import java.lang.reflect.*;
import java.util.*;

public class ExecutionControlProviderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static void testWhiteBox() throws Exception {

        Class<?> c = Class.forName("jdk.jshell.spi.ExecutionControlProvider");
        check(c.isInterface(), "is interface");
        check(Modifier.isPublic(c.getModifiers()), "is public");
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
        // Real JDK implementation of the provider SPI, invoked through the
        // interface type so each call-site targets jdk.jshell.spi.ExecutionControlProvider.
        jdk.jshell.spi.ExecutionControlProvider p =
            new jdk.jshell.execution.LocalExecutionControlProvider();

        check("local".equals(p.name()), "name() returns 'local'");
        check(p.defaultParameters().isEmpty(), "defaultParameters() returns empty map");

        jdk.jshell.spi.ExecutionControl ec =
            p.generate(envFixture(), new HashMap<String, String>());
        check(ec != null, "generate() returns an engine");
        check(ec instanceof jdk.jshell.spi.ExecutionControl, "engine implements ExecutionControl");
        ec.close();
    }

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
