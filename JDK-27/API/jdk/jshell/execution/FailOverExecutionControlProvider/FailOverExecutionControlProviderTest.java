/*
 * @test
 * @summary jdk.jshell.execution.FailOverExecutionControlProvider conformance: provider metadata.
 * @run main/othervm FailOverExecutionControlProviderTest
 */

import jdk.jshell.execution.*;
import jdk.jshell.spi.*;
import jdk.jshell.spi.ExecutionControl.*;
import java.util.*;

public class FailOverExecutionControlProviderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jshell.execution.FailOverExecutionControlProvider");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("name")) f=true; check(f, "has name"); }
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("defaultParameters")) f=true; check(f, "has defaultParameters"); }
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("generate")) f=true; check(f, "has generate"); }
    }

    static void testBlackBox() throws Exception {

        FailOverExecutionControlProvider p = new FailOverExecutionControlProvider();
        check(p.name() != null && !p.name().isEmpty(), "name");
        check(p.defaultParameters() != null, "defaultParameters");
    }

    static ExecutionEnv envFixture() {
        return new ExecutionEnv() {
            public java.io.InputStream userIn() { return java.io.InputStream.nullInputStream(); }
            public java.io.PrintStream userOut() { return new java.io.PrintStream(java.io.OutputStream.nullOutputStream()); }
            public java.io.PrintStream userErr() { return new java.io.PrintStream(java.io.OutputStream.nullOutputStream()); }
            public List<String> extraRemoteVMOptions() { return List.of(); }
            public void closeDown() { }
        };
    }

    // ---- deep contract: ordered failover table and failure reporting ----
    static void testFailoverTable() throws Throwable {
        FailOverExecutionControlProvider p = new FailOverExecutionControlProvider();
        check(p.name().equals("failover"), "name is 'failover'");
        Map<String, String> dp = p.defaultParameters();
        // ten ordered slots: slot 0 tries jdi first, the rest are empty (skipped)
        check(dp.size() == 10, "ten parameter slots");
        check(dp.get("0").equals("jdi"), "slot 0 defaults to jdi");
        for (int i = 1; i <= 9; i++) check(dp.get("" + i).isEmpty(), "slot " + i + " defaults to empty");
        // the parameter map is a fresh copy each call
        check(p.defaultParameters() != dp, "defaultParameters returns a fresh map");
    }

    static void testFailoverSemantics() throws Throwable {
        FailOverExecutionControlProvider p = new FailOverExecutionControlProvider();
        ExecutionEnv env = envFixture();
        // first spec fails, second succeeds -> the failover works
        Map<String, String> m = new HashMap<>();
        m.put("0", "no.such.provider:9");
        m.put("1", "local");
        ExecutionControl ec = ExecutionControl.generate(env, "failover", m);
        check(ec instanceof jdk.jshell.execution.LocalExecutionControl, "failed first spec falls through to local");
        ec.close();
        // a slot naming a real provider wins immediately
        Map<String, String> m2 = new HashMap<>();
        m2.put("3", "local");
        ExecutionControl ec2 = ExecutionControl.generate(env, "failover", m2);
        check(ec2 != null, "populated later slot is used");
        ec2.close();
        // no slot set at all -> IllegalArgumentException
        try {
            ExecutionControl.generate(env, "failover", new HashMap<String, String>());
            check(false, "empty parameters must throw");
        } catch (IllegalArgumentException expected) {
            check(expected.getMessage().contains("least one parameter"), "empty-parameters message (JDK typo retained): " + expected.getMessage());
        }
        // every slot fails -> RuntimeException with a dump naming the failed spec and last cause
        try {
            ExecutionControl.generate(env, "failover", Map.of("0", "nosuchxyz"));
            check(false, "all-fail must throw");
        } catch (RuntimeException expected) {
            check(expected.getMessage().contains("FAILED: 0:nosuchxyz"), "failure dump names the spec: " + expected.getMessage());
            check(expected.getCause() instanceof IllegalArgumentException, "cause is the last provider failure");
        }
    }

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        testFailoverTable();
        testFailoverSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
