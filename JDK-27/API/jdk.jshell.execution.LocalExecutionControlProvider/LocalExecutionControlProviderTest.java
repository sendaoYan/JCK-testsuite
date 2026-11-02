/*
 * @test
 * @summary jdk.jshell.execution.LocalExecutionControlProvider conformance: local provider.
 * @run main LocalExecutionControlProviderTest
 */

import jdk.jshell.execution.*;
import jdk.jshell.spi.*;
import jdk.jshell.spi.ExecutionControl.*;
import java.util.*;

public class LocalExecutionControlProviderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        LocalExecutionControlProvider p = new LocalExecutionControlProvider();
        check(p.name().equals("local"), "name");
        check(p.defaultParameters() != null, "defaultParameters");
        check(p.createExecutionControl(null, p.defaultParameters()) != null, "createExecutionControl");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
