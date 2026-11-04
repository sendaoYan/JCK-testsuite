/*
 * @test
 * @summary jdk.jshell.spi.ExecutionControl.UserException conformance: user exception.
 * @run main UserExceptionTest
 */

import jdk.jshell.execution.*;
import jdk.jshell.spi.*;
import jdk.jshell.spi.ExecutionControl.*;
import java.util.*;

public class UserExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        UserException e = new UserException("msg", "java.lang.RuntimeException", new StackTraceElement[0]);
        check(e.getMessage().equals("msg"), "message");
        check(e.causeExceptionClass().equals("java.lang.RuntimeException"), "cause class");
        check(e instanceof RunException, "extends RunException");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
