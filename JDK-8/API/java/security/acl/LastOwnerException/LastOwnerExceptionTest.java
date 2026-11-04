/*
 * @test
 * @summary java.security.acl.LastOwnerException conformance: sole no-arg
 *          constructor, Exception hierarchy outside GeneralSecurityException,
 *          null message/cause, initCause-once, serialVersionUID stability,
 *          serialization round-trip, and the semantic trigger: an Owner
 *          implementation deleting the final owner throws it.
 * @run main LastOwnerExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.security.Principal;
import java.security.acl.LastOwnerException;
import java.security.acl.Owner;

public class LastOwnerExceptionTest {
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

    static class P implements Principal {
        private final String n;
        P(String n) { this.n = n; }
        public String getName() { return n; }
        public boolean equals(Object o) { return o instanceof P && ((P) o).n.equals(n); }
        public int hashCode() { return n.hashCode(); }
    }

    static class RecOwner implements Owner {
        private final java.util.Set<Principal> owners = new java.util.LinkedHashSet<Principal>();
        RecOwner(Principal first) { owners.add(first); }
        public boolean addOwner(Principal caller, Principal newOwner) {
            return owners.add(newOwner);
        }
        public boolean deleteOwner(Principal caller, Principal owner) throws LastOwnerException {
            if (!owners.contains(owner)) return false;
            if (owners.size() == 1) throw new LastOwnerException();
            return owners.remove(owner);
        }
        public boolean isOwner(Principal p) { return owners.contains(p); }
    }

    public static void main(String[] args) throws Exception {
        Class<LastOwnerException> c = LastOwnerException.class;
        LastOwnerException e = new LastOwnerException();
        check(e instanceof java.lang.Exception, "extends java.lang.Exception");
        check(!java.security.GeneralSecurityException.class.isInstance(e),
                "outside GeneralSecurityException tree");
        LastOwnerException e4 = new LastOwnerException();
        check(e4.getMessage() == null, "no-arg ctor message null");

        // ---- sole-constructor contract ----
        check(c.getConstructors().length == 1, "exactly one public constructor");
        check(c.getConstructor().getParameterTypes().length == 0, "the sole ctor is no-arg");

        // ---- message/cause contract ----
        check(e.getMessage() == null, "message always null");
        check(e.getCause() == null, "cause defaults null");
        check(e.getSuppressed().length == 0, "no suppressed initially");
        check(e.fillInStackTrace() == e, "fillInStackTrace returns this");
        RuntimeException cause = new RuntimeException("root");
        e.initCause(cause);
        check(e.getCause() == cause, "initCause installs cause");
        checkThrows(IllegalStateException.class, () -> e.initCause(new RuntimeException()),
                "second initCause -> IllegalStateException");
        check(e.toString().startsWith("java.security.acl.LastOwnerException"),
                "toString carries class name");

        // ---- semantic trigger via Owner implementation ----
        Principal o1 = new P("o1");
        RecOwner ro = new RecOwner(o1);
        check(ro.isOwner(o1), "owner registered");
        checkThrows(LastOwnerException.class, () -> ro.deleteOwner(o1, o1),
                "deleting the only owner -> LastOwnerException");
        Principal o2 = new P("o2");
        ro.addOwner(o1, o2);
        check(ro.deleteOwner(o1, o1), "with two owners delete succeeds");
        checkThrows(LastOwnerException.class, () -> ro.deleteOwner(o2, o2),
                "back to one owner, delete again -> LastOwnerException");

        // ---- serialVersionUID stability (white-box read of declared field) ----
        java.lang.reflect.Field svuid = c.getDeclaredField("serialVersionUID");
        svuid.setAccessible(true);
        check(svuid.getLong(null) == ObjectStreamClass.lookup(c).getSerialVersionUID(),
                "declared serialVersionUID matches serialization form");

        // ---- serialization round-trip ----
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) { oos.writeObject(e); }
        Object back = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())).readObject();
        check(back.getClass() == c, "round-trip exact class");
        check(((LastOwnerException) back).getMessage() == null, "round-trip message still null");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
