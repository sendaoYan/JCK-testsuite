/*
 * @test
 * @summary jdk.jshell.spi.ExecutionControl.InternalException conformance: internal exception.
 * @run main InternalExceptionTest
 */

import jdk.jshell.execution.*;
import jdk.jshell.spi.*;
import jdk.jshell.spi.ExecutionControl.*;
import java.util.*;

public class InternalExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        InternalException e = new InternalException("msg");
        check(e.getMessage().equals("msg"), "message");
        check(e instanceof ExecutionControlException, "hierarchy");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
