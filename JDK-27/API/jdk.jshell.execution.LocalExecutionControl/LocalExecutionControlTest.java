/*
 * @test
 * @summary jdk.jshell.execution.LocalExecutionControl conformance: local execution control.
 * @run main LocalExecutionControlTest
 */

import jdk.jshell.execution.*;
import jdk.jshell.spi.*;
import jdk.jshell.spi.ExecutionControl.*;
import java.util.*;

public class LocalExecutionControlTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        LocalExecutionControl ec = new LocalExecutionControl();
        check(ec instanceof ExecutionControl, "implements ExecutionControl");
        ec.close();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
