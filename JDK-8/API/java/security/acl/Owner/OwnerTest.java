/*
 * @test
 * @summary java.security.acl.Owner conformance: exact method signatures with
 *          precise throws clauses (addOwner -> NotOwnerException; deleteOwner ->
 *          NotOwnerException + LastOwnerException), marker-free interface (no
 *          super-interfaces), and recording-implementation semantics: owner
 *          gating, add/delete boolean contract, last-owner protection,
 *          isOwner, polymorphic use through the interface type.
 * @run main OwnerTest
 */

import java.lang.reflect.Method;
import java.security.Principal;
import java.security.acl.LastOwnerException;
import java.security.acl.NotOwnerException;
import java.security.acl.Owner;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class OwnerTest {
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
        public String toString() { return "P[" + n + "]"; }
    }

    static class RecOwner implements Owner {
        private final Set<Principal> owners = new LinkedHashSet<Principal>();
        RecOwner(Principal first) { owners.add(first); }
        public boolean addOwner(Principal caller, Principal newOwner) throws NotOwnerException {
            if (!owners.contains(caller)) throw new NotOwnerException();
            return owners.add(newOwner);
        }
        public boolean deleteOwner(Principal caller, Principal owner)
                throws NotOwnerException, LastOwnerException {
            if (!owners.contains(caller)) throw new NotOwnerException();
            if (!owners.contains(owner)) return false;
            if (owners.size() == 1) throw new LastOwnerException();
            return owners.remove(owner);
        }
        public boolean isOwner(Principal p) { return owners.contains(p); }
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.acl.Owner");
        check(c.isInterface(), "is interface");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("addOwner"), "declares addOwner");
        check(ms.contains("deleteOwner"), "declares deleteOwner");
        check(ms.contains("isOwner"), "declares isOwner");

        // ---- exact method set, signatures and throws clauses ----
        Set<String> expect = new HashSet<String>();
        for (String n : new String[]{"addOwner", "deleteOwner", "isOwner"}) expect.add(n);
        check(ms.equals(expect), "declared method set exactly 3 methods: " + ms);
        check(c.getInterfaces().length == 0, "no super-interfaces");
        for (Method m : c.getDeclaredMethods()) {
            Set<String> exn = new HashSet<String>();
            for (Class<?> e : m.getExceptionTypes()) exn.add(e.getName());
            String mn = m.getName();
            if (mn.equals("addOwner"))
                check(exn.equals(new HashSet<String>(java.util.Arrays.asList("java.security.acl.NotOwnerException"))),
                        "addOwner throws exactly NotOwnerException");
            else if (mn.equals("deleteOwner"))
                check(exn.equals(new HashSet<String>(java.util.Arrays.asList(
                        "java.security.acl.NotOwnerException", "java.security.acl.LastOwnerException"))),
                        "deleteOwner throws NotOwnerException+LastOwnerException");
            else
                check(exn.isEmpty(), "isOwner declares no checked exceptions");
            check(mn.equals("isOwner") || m.getParameterTypes().length == 2,
                    mn + " takes 2 Principal params");
            if (!mn.equals("isOwner"))
                check(m.getParameterTypes()[0] == Principal.class && m.getParameterTypes()[1] == Principal.class,
                        mn + " params are Principal,Principal");
            else
                check(m.getParameterTypes().length == 1 && m.getParameterTypes()[0] == Principal.class,
                        "isOwner takes one Principal param");
            check(m.getReturnType() == boolean.class, mn + " returns boolean");
        }

        // ---- recording implementation: full behavioural contract ----
        Principal o1 = new P("o1");
        Principal o2 = new P("o2");
        Principal stranger = new P("stranger");
        Owner ow = new RecOwner(o1); // polymorphic through the interface type
        check(ow.isOwner(o1), "initial owner recognized");
        check(!ow.isOwner(stranger), "stranger not owner");
        check(!ow.isOwner(null), "null not owner");
        check(ow.addOwner(o1, o2), "owner adds second owner -> true");
        check(!ow.addOwner(o1, o2), "re-adding existing owner -> false");
        checkThrows(NotOwnerException.class, () -> ow.addOwner(stranger, stranger),
                "addOwner by non-owner -> NotOwnerException");
        checkThrows(NotOwnerException.class, () -> ow.deleteOwner(stranger, o1),
                "deleteOwner by non-owner -> NotOwnerException");
        check(ow.deleteOwner(o1, o2), "owner deletes other owner -> true");
        check(!ow.deleteOwner(o1, o2), "deleting non-member owner -> false");
        checkThrows(LastOwnerException.class, () -> ow.deleteOwner(o1, o1),
                "deleting the last remaining owner -> LastOwnerException");
        check(ow.isOwner(o1), "last owner retained after LastOwnerException");
        // ownership transfer pattern
        check(ow.addOwner(o1, o2), "re-add second owner");
        check(ow.deleteOwner(o2, o1), "new owner deletes old owner (self-delete allowed with others present)");
        check(!ow.isOwner(o1) && ow.isOwner(o2), "ownership fully transferred");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
