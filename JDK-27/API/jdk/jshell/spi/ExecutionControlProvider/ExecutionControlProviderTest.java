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


    // ---- deep contract: every installed provider's parameter table ----
    static void testProviderRegistry() throws Throwable {
        java.util.Map<String, java.util.Map<String, String>> table = new java.util.TreeMap<>();
        for (jdk.jshell.spi.ExecutionControlProvider p : java.util.ServiceLoader.load(jdk.jshell.spi.ExecutionControlProvider.class))
            table.put(p.name(), p.defaultParameters());
        check(table.keySet().equals(new java.util.TreeSet<>(java.util.Arrays.asList("failover", "jdi", "local"))),
              "exact installed provider set: " + table.keySet());
        check(table.get("local").isEmpty(), "local: no parameters");
        check(table.get("jdi").keySet().equals(new java.util.TreeSet<>(java.util.Arrays.asList(
              "hostname", "launch", "remoteAgent", "timeout"))), "jdi parameter keys");
        check(table.get("failover").size() == 10, "failover has ten slots");
        check(table.get("failover").get("0").equals("jdi"), "failover slot 0 defaults to jdi");
        // null parameters fall back to defaults for every provider
        for (jdk.jshell.spi.ExecutionControlProvider p : java.util.ServiceLoader.load(jdk.jshell.spi.ExecutionControlProvider.class))
            check(p.defaultParameters() != null, "defaultParameters never null: " + p.name());
    }

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        testProviderRegistry();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
