/*
 * @test
 * @summary jdk.jshell.spi.ExecutionControl.NotImplementedException conformance: not implemented exception.
 * @run main NotImplementedExceptionTest
 */

import jdk.jshell.execution.*;
import jdk.jshell.spi.*;
import jdk.jshell.spi.ExecutionControl.*;
import java.util.*;

public class NotImplementedExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        NotImplementedException e = new NotImplementedException("msg");
        check(e.getMessage().equals("msg"), "message");
        check(e instanceof InternalException, "extends InternalException");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
