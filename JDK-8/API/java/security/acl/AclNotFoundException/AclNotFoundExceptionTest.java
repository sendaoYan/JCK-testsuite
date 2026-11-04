/*
 * @test
 * @summary java.security.acl.AclNotFoundException conformance: sole no-arg
 *          constructor (no message variant), Exception hierarchy outside the
 *          GeneralSecurityException tree, null message/cause, initCause-once,
 *          fillInStackTrace identity, suppressed exceptions, serialVersionUID
 *          stability, and serialization round-trip preserving class and message.
 * @run main AclNotFoundExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;

public class AclNotFoundExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName() + " but nothing was thrown");
    }

    public static void main(String[] args) throws Exception {
        Class<java.security.acl.AclNotFoundException> c = java.security.acl.AclNotFoundException.class;
        java.security.acl.AclNotFoundException e = new java.security.acl.AclNotFoundException();
        check(e instanceof java.lang.Exception, "extends java.lang.Exception");
        check(!java.security.GeneralSecurityException.class.isInstance(e),
                "outside GeneralSecurityException tree");
        check(!java.io.IOException.class.isInstance(e), "not an IOException");
        java.security.acl.AclNotFoundException e4 = new java.security.acl.AclNotFoundException();
        check(e4.getMessage() == null, "no-arg ctor message null");

        // ---- sole-constructor contract: no (String)/(Throwable) variants ----
        check(c.getConstructors().length == 1, "exactly one public constructor");
        check(c.getConstructor().getParameterTypes().length == 0, "the sole ctor is no-arg");

        // ---- message/cause/suppressed contract ----
        check(e.getMessage() == null, "message always null");
        check(e.getCause() == null, "cause defaults null");
        check(e.getSuppressed().length == 0, "no suppressed initially");
        check(e.fillInStackTrace() == e, "fillInStackTrace returns this");
        RuntimeException cause = new RuntimeException("root");
        e.initCause(cause);
        check(e.getCause() == cause, "initCause installs cause");
        checkThrows(IllegalStateException.class, () -> e.initCause(new RuntimeException()),
                "second initCause -> IllegalStateException");
        e.addSuppressed(new ArithmeticException());
        check(e.getSuppressed().length == 1, "addSuppressed visible");
        check(e.getStackTrace() != null && e.getStackTrace().length >= 0, "stack trace readable");
        check(e.toString().startsWith("java.security.acl.AclNotFoundException"),
                "toString carries class name");

        // ---- serialVersionUID stability (white-box read of declared field) ----
        java.lang.reflect.Field svuid = c.getDeclaredField("serialVersionUID");
        svuid.setAccessible(true);
        long declared = svuid.getLong(null);
        check(declared == ObjectStreamClass.lookup(c).getSerialVersionUID(),
                "declared serialVersionUID matches serialization form");

        // ---- serialization round-trip ----
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) { oos.writeObject(e); }
        Object back = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())).readObject();
        check(back instanceof java.security.acl.AclNotFoundException, "round-trip class preserved");
        check(((java.security.acl.AclNotFoundException) back).getMessage() == null,
                "round-trip message still null");
        check(back.getClass() == c, "round-trip exact class");
        check(!back.equals("x"), "equals irrelevant types false");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
