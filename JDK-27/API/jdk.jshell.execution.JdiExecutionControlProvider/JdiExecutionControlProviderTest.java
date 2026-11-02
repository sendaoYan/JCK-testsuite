/*
 * @test
 * @summary jdk.jshell.execution.JdiExecutionControlProvider conformance: provider metadata and params.
 * @run main JdiExecutionControlProviderTest
 */

import jdk.jshell.execution.*;
import jdk.jshell.spi.*;
import jdk.jshell.spi.ExecutionControl.*;
import java.util.*;

public class JdiExecutionControlProviderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        JdiExecutionControlProvider p = new JdiExecutionControlProvider();
        check(p.name() != null && !p.name().isEmpty(), "name");
        check(p.defaultParameters() != null, "defaultParameters");
        check(JdiExecutionControlProvider.PARAM_REMOTE_AGENT != null, "PARAM_REMOTE_AGENT");
        check(JdiExecutionControlProvider.PARAM_TIMEOUT != null, "PARAM_TIMEOUT");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
