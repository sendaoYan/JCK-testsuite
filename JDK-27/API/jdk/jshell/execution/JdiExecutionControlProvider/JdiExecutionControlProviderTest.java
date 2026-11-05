/*
 * @test
 * @summary jdk.jshell.execution.JdiExecutionControlProvider conformance: provider metadata and params.
 * @run main/othervm/timeout=300 JdiExecutionControlProviderTest
 */

import jdk.jshell.*;
import jdk.jshell.execution.*;
import jdk.jshell.spi.*;
import jdk.jshell.spi.ExecutionControl.*;
import java.util.*;

public class JdiExecutionControlProviderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jshell.execution.JdiExecutionControlProvider");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("name")) f=true; check(f, "has name"); }
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("defaultParameters")) f=true; check(f, "has defaultParameters"); }
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("generate")) f=true; check(f, "has generate"); }
    }

    static void testBlackBox() throws Exception {

        JdiExecutionControlProvider p = new JdiExecutionControlProvider();
        check(p.name() != null && !p.name().isEmpty(), "name");
        check(p.defaultParameters() != null, "defaultParameters");
        check(JdiExecutionControlProvider.PARAM_REMOTE_AGENT != null, "PARAM_REMOTE_AGENT");
        check(JdiExecutionControlProvider.PARAM_TIMEOUT != null, "PARAM_TIMEOUT");
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

    // ---- deep contract: exact parameter table and a real remote-JVM eval closed loop ----
    static void testParameterTable() {
        JdiExecutionControlProvider p = new JdiExecutionControlProvider();
        check(p.name().equals("jdi"), "name is 'jdi'");
        check(JdiExecutionControlProvider.PARAM_REMOTE_AGENT.equals("remoteAgent"), "PARAM_REMOTE_AGENT value");
        check(JdiExecutionControlProvider.PARAM_HOST_NAME.equals("hostname"), "PARAM_HOST_NAME value");
        check(JdiExecutionControlProvider.PARAM_TIMEOUT.equals("timeout"), "PARAM_TIMEOUT value");
        check(JdiExecutionControlProvider.PARAM_LAUNCH.equals("launch"), "PARAM_LAUNCH value");
        Map<String, String> dp = p.defaultParameters();
        check(dp.size() == 4, "four default parameters");
        check(dp.get("remoteAgent").equals("jdk.jshell.execution.RemoteExecutionControl"), "remote agent main class");
        check(dp.get("timeout").equals("5000"), "default timeout 5000ms");
        check(dp.get("hostname").isEmpty(), "default hostname empty (loopback)");
        check(dp.get("launch").equals("false"), "default launch=false (listen mode)");
        check(p.defaultParameters() != dp, "defaultParameters returns a fresh map");
    }

    static void testRealRemoteLoop() throws Throwable {
        // generate() really launches a debuggee JVM and wires the execution channel
        JdiExecutionControlProvider p = new JdiExecutionControlProvider();
        ExecutionControl ec = ExecutionControl.generate(envFixture(), "jdi");
        check(ec instanceof jdk.jshell.execution.JdiDefaultExecutionControl,
              "generate() returns JdiDefaultExecutionControl: " + ec.getClass().getSimpleName());
        ec.close();
    }

    static void testJShellInjectionLoop() throws Throwable {
        // the full flagship loop: inject the provider into JShell and evaluate remotely
        JdiExecutionControlProvider jp = new JdiExecutionControlProvider();
        try (JShell js = JShell.builder().executionEngine(jp, new HashMap<>(jp.defaultParameters())).build()) {
            List<SnippetEvent> evs = js.eval("2 + 3");
            check(evs.get(0).value().equals("5"), "remote eval 2+3 -> 5");
            VarSnippet v = (VarSnippet) js.eval("String jdiv = \"remote\";").get(0).snippet();
            check(js.varValue(v).equals("\"remote\""), "remote varValue round-trip");
            js.eval("void jdim() { System.out.print(\"side\"); }");
            SnippetEvent call = js.eval("jdim()").get(0);
            check(call.snippet().kind() == Snippet.Kind.STATEMENT, "void call is a statement");
            check(call.value().isEmpty(), "void call reports no value");
            // drop works through the remote channel too
            js.drop(v);
            check(js.status(v) == Snippet.Status.DROPPED, "remote drop lifecycle");
        }
    }

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        testParameterTable();
        testRealRemoteLoop();
        testJShellInjectionLoop();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
