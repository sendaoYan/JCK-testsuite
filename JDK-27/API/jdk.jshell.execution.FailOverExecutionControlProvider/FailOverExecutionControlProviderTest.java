/*
 * @test
 * @summary jdk.jshell.execution.FailOverExecutionControlProvider conformance: provider metadata.
 * @run main FailOverExecutionControlProviderTest
 */

import jdk.jshell.execution.*;
import jdk.jshell.spi.*;
import jdk.jshell.spi.ExecutionControl.*;
import java.util.*;

public class FailOverExecutionControlProviderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        FailOverExecutionControlProvider p = new FailOverExecutionControlProvider();
        check(p.name() != null && !p.name().isEmpty(), "name");
        check(p.defaultParameters() != null, "defaultParameters");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
