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

    // ---- deep contract: stack trace ownership and serialization ----
    static void testDeepSemantics() throws Exception {
        StackTraceElement[] st = new StackTraceElement[]{
            new StackTraceElement("Cls", "m", "Cls.java", 17), new StackTraceElement("Cls", "n", "Cls.java", 18) };
        UserException e = new UserException("m", "java.lang.RuntimeException", st);
        check(e.getStackTrace().length == 2, "stack trace installed");
        check(e.getStackTrace()[0].getMethodName().equals("m"), "stack frames carried");
        check(e.getStackTrace()[1].getLineNumber() == 18, "line numbers carried");
        check(new UserException("m", "c", new StackTraceElement[0]).getStackTrace().length == 0,
              "empty stack trace allowed");
        // serialization round-trip keeps message, class name and frames
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oo = new java.io.ObjectOutputStream(bos)) { oo.writeObject(e); }
        try (java.io.ObjectInputStream oi = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray()))) {
            UserException back = (UserException) oi.readObject();
            check(back.getMessage().equals("m"), "message survives serialization");
            check(back.causeExceptionClass().equals("java.lang.RuntimeException"), "cause class survives serialization");
            check(back.getStackTrace().length == 2, "stack trace survives serialization");
        }
    }

    public static void main(String[] args) throws Exception {
        UserException e = new UserException("msg", "java.lang.RuntimeException", new StackTraceElement[0]);
        check(e.getMessage().equals("msg"), "message");
        check(e.causeExceptionClass().equals("java.lang.RuntimeException"), "cause class");
        check(e instanceof RunException, "extends RunException");
        testDeepSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
