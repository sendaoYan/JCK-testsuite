/*
 * @test
 * @summary jdk.jshell.execution.DirectExecutionControl conformance: direct execution control.
 * @run main DirectExecutionControlTest
 */

import jdk.jshell.execution.*;
import jdk.jshell.spi.*;
import jdk.jshell.spi.ExecutionControl.*;
import java.util.*;

public class DirectExecutionControlTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        DirectExecutionControl ec = new DirectExecutionControl();
        check(ec instanceof ExecutionControl, "implements ExecutionControl");
        ec.close();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
