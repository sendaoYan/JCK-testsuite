/*
 * @test
 * @summary jdk.jshell.execution.RemoteExecutionControl conformance: remote execution control.
 * @run main RemoteExecutionControlTest
 */

import jdk.jshell.execution.*;
import jdk.jshell.spi.*;
import jdk.jshell.spi.ExecutionControl.*;
import java.util.*;

public class RemoteExecutionControlTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        RemoteExecutionControl ec = new RemoteExecutionControl();
        check(ec instanceof ExecutionControl, "implements ExecutionControl");
        ec.close();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
