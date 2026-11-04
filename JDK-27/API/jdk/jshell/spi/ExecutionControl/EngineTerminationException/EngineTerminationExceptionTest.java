/*
 * @test
 * @summary jdk.jshell.spi.ExecutionControl.EngineTerminationException conformance: engine termination exception.
 * @run main EngineTerminationExceptionTest
 */

import jdk.jshell.execution.*;
import jdk.jshell.spi.*;
import jdk.jshell.spi.ExecutionControl.*;
import java.util.*;

public class EngineTerminationExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        EngineTerminationException e = new EngineTerminationException("msg");
        check(e.getMessage().equals("msg"), "message");
        check(e instanceof ExecutionControlException, "hierarchy");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
