/*
 * @test
 * @summary java.security.acl.NotOwnerException conformance: sole no-arg
 *          constructor, Exception hierarchy outside GeneralSecurityException,
 *          null message/cause, initCause-once, serialVersionUID stability,
 *          serialization round-trip, and the semantic trigger: owner-gated
 *          Acl setName/addEntry operations throw it for non-owner callers.
 * @run main NotOwnerExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.security.Principal;
import java.security.acl.Acl;
import java.security.acl.AclEntry;
import java.security.acl.NotOwnerException;

public class NotOwnerExceptionTest {
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

    static class OwnerGatedAcl implements Acl {
        private final Principal owner;
        private String name;
        OwnerGatedAcl(String name, Principal owner) { this.name = name; this.owner = owner; }
        public void setName(Principal caller, String name) throws NotOwnerException {
            if (!caller.equals(owner)) throw new NotOwnerException();
            this.name = name;
        }
        public String getName() { return name; }
        public boolean addEntry(Principal caller, AclEntry e) throws NotOwnerException {
            if (!caller.equals(owner)) throw new NotOwnerException();
            return true;
        }
        public boolean removeEntry(Principal caller, AclEntry e) throws NotOwnerException {
            if (!caller.equals(owner)) throw new NotOwnerException();
            return false;
        }
        public java.util.Enumeration<java.security.acl.Permission> getPermissions(Principal user) {
            return java.util.Collections.emptyEnumeration();
        }
        public java.util.Enumeration<AclEntry> entries() {
            return java.util.Collections.emptyEnumeration();
        }
        public boolean checkPermission(Principal user, java.security.acl.Permission perm) { return false; }
        public boolean addOwner(Principal caller, Principal newOwner) { return false; }
        public boolean deleteOwner(Principal caller, Principal owner) { return false; }
        public boolean isOwner(Principal p) { return p.equals(this.owner); }
        public String toString() { return "OwnerGatedAcl[" + name + "]";
        }
    }

    public static void main(String[] args) throws Exception {
        Class<NotOwnerException> c = NotOwnerException.class;
        NotOwnerException e = new NotOwnerException();
        check(e instanceof java.lang.Exception, "extends java.lang.Exception");
        check(!java.security.GeneralSecurityException.class.isInstance(e),
                "outside GeneralSecurityException tree");
        NotOwnerException e4 = new NotOwnerException();
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
        check(e.toString().startsWith("java.security.acl.NotOwnerException"),
                "toString carries class name");

        // ---- semantic trigger: owner-gated Acl operations ----
        Principal owner = new P("owner");
        Principal stranger = new P("stranger");
        OwnerGatedAcl acl = new OwnerGatedAcl("acl", owner);
        acl.setName(owner, "renamed");
        check(acl.getName().equals("renamed"), "owner setName succeeds");
        checkThrows(NotOwnerException.class, () -> acl.setName(stranger, "hacked"),
                "non-owner setName -> NotOwnerException");
        checkThrows(NotOwnerException.class, () -> acl.addEntry(stranger, null),
                "non-owner addEntry -> NotOwnerException");
        checkThrows(NotOwnerException.class, () -> acl.removeEntry(stranger, null),
                "non-owner removeEntry -> NotOwnerException");
        check(acl.getName().equals("renamed"), "rejected operations leave state intact");

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
        check(((NotOwnerException) back).getMessage() == null, "round-trip message still null");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
