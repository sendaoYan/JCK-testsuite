/*
 * @test
 * @summary java.security.PrivilegedActionException conformance: white-box structure + black-box behavior.
 * @run main PrivilegedActionExceptionTest
 */

import java.security.PrivilegedActionException;
import java.security.*;
import java.util.*;

public class PrivilegedActionExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        Exception cause = new Exception("root");
        PrivilegedActionException e = new PrivilegedActionException(cause);
        check(cause == e.getException(), "getException");
        check(e.getCause() == cause, "getCause");
        check(e instanceof Exception, "extends Exception");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        Exception cause = new java.io.IOException("root-cause");
        PrivilegedActionException pae = new PrivilegedActionException(cause);
        check(pae.getException() == cause, "getException");
        check(pae.getCause() == cause, "getCause");
        check(pae.toString().contains("java.io.IOException"), "toString");
        // real wrapping by doPrivileged produces the same shape
        try {
            AccessController.doPrivileged((PrivilegedExceptionAction<Object>) () -> { throw new CloneNotSupportedException("cn"); });
            check(false, "expected PAE");
        } catch (PrivilegedActionException real) {
            check(real.getException() instanceof CloneNotSupportedException, "real cause type");
            check("cn".equals(real.getException().getMessage()), "real cause message");
            check(real instanceof Exception, "is Exception");
        }
        // JDK-27 accepts a null exception
        PrivilegedActionException nullCause = new PrivilegedActionException(null);
        check(nullCause.getException() == null, "null exception retained");
        // JDK-27 constructs with a null message (it does not mirror the cause message)
        check(pae.getMessage() == null, "message null by design");
        check(pae.toString().contains("root-cause"), "toString includes cause");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
