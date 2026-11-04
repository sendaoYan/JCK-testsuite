/*
 * @test
 * @summary java.security.acl.Acl conformance: interface hierarchy, exact method
 *          signatures incl. throws clauses (setName/addEntry/removeEntry throw
 *          NotOwnerException), Owner inheritance, and recording-implementation
 *          semantics (owner-gated mutation, addEntry/removeEntry boolean
 *          contract, permission aggregation, entries enumeration).
 * @run main AclTest
 */

import java.lang.reflect.Method;
import java.security.Principal;
import java.security.acl.Acl;
import java.security.acl.AclEntry;
import java.security.acl.LastOwnerException;
import java.security.acl.NotOwnerException;
import java.security.acl.Owner;
import java.security.acl.Permission;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class AclTest {
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

    static class RecPrincipal implements Principal {
        private final String n;
        RecPrincipal(String n) { this.n = n; }
        public String getName() { return n; }
        public boolean equals(Object o) { return o instanceof RecPrincipal && ((RecPrincipal) o).n.equals(n); }
        public int hashCode() { return n.hashCode(); }
        public String toString() { return "RecPrincipal[" + n + "]"; }
    }

    static class RecPerm implements Permission {
        private final String n;
        RecPerm(String n) { this.n = n; }
        public boolean equals(Object o) { return o instanceof RecPerm && ((RecPerm) o).n.equals(n); }
        public int hashCode() { return n.hashCode(); }
        public String toString() { return "RecPerm[" + n + "]"; }
    }

    static class RecEntry implements AclEntry {
        private Principal principal;
        private boolean negative;
        private final Set<Permission> perms = new LinkedHashSet<Permission>();
        public boolean setPrincipal(Principal p) { if (principal != null) return false; principal = p; return true; }
        public Principal getPrincipal() { return principal; }
        public void setNegativePermissions() { negative = true; }
        public boolean isNegative() { return negative; }
        public boolean addPermission(Permission p) { return perms.add(p); }
        public boolean removePermission(Permission p) { return perms.remove(p); }
        public boolean checkPermission(Permission p) { return perms.contains(p); }
        public Enumeration<Permission> permissions() { return Collections.enumeration(new ArrayList<Permission>(perms)); }
        public String toString() { return "RecEntry[" + principal + ", neg=" + negative + ", " + perms.size() + " perms]"; }
        public Object clone() {
            try {
                RecEntry c = (RecEntry) super.clone();
                return c; // shallow: perms shared, documented Cloneable shallow contract
            } catch (CloneNotSupportedException e) { throw new AssertionError(e); }
        }
    }

    static class RecAcl implements Acl {
        private final Set<Principal> owners = new LinkedHashSet<Principal>();
        private String name;
        private final List<AclEntry> entries = new ArrayList<AclEntry>();
        RecAcl(String name, Principal owner) { this.name = name; owners.add(owner); }
        public void setName(Principal caller, String name) throws NotOwnerException {
            if (!owners.contains(caller)) throw new NotOwnerException();
            this.name = name;
        }
        public String getName() { return name; }
        public boolean addEntry(Principal caller, AclEntry e) throws NotOwnerException {
            if (!owners.contains(caller)) throw new NotOwnerException();
            if (entries.contains(e)) return false;
            entries.add(e);
            return true;
        }
        public boolean removeEntry(Principal caller, AclEntry e) throws NotOwnerException {
            if (!owners.contains(caller)) throw new NotOwnerException();
            return entries.remove(e);
        }
        public Enumeration<Permission> getPermissions(Principal user) {
            List<Permission> out = new ArrayList<Permission>();
            for (AclEntry e : entries)
                if (user.equals(e.getPrincipal())) {
                    Enumeration<Permission> p = e.permissions();
                    while (p.hasMoreElements()) out.add(p.nextElement());
                }
            return Collections.enumeration(out);
        }
        public Enumeration<AclEntry> entries() { return Collections.enumeration(new ArrayList<AclEntry>(entries)); }
        public boolean checkPermission(Principal user, Permission perm) {
            Enumeration<Permission> ps = getPermissions(user);
            while (ps.hasMoreElements()) if (ps.nextElement().equals(perm)) return true;
            return false;
        }
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
        public String toString() { return "RecAcl[" + name + "," + entries.size() + " entries]"; }
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.acl.Acl");
        check(c.isInterface(), "is interface");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("addEntry"), "declares addEntry");
        check(ms.contains("checkPermission"), "declares checkPermission");
        check(ms.contains("entries"), "declares entries");
        check(ms.contains("getName"), "declares getName");
        check(ms.contains("getPermissions"), "declares getPermissions");
        check(ms.contains("removeEntry"), "declares removeEntry");
        check(ms.contains("setName"), "declares setName");
        check(ms.contains("toString"), "declares toString");

        // ---- exact method-set + signature precision (no extra, no missing) ----
        Set<String> expect = new HashSet<String>();
        for (String n : new String[]{"addEntry", "checkPermission", "entries", "getName",
                "getPermissions", "removeEntry", "setName", "toString"}) expect.add(n);
        check(ms.equals(expect), "declared method set exactly the 8 javadoc methods: " + ms);
        check(in.equals(new HashSet<String>(java.util.Arrays.asList("java.security.acl.Owner"))),
                "extends Owner and nothing else");
        check(Owner.class.isAssignableFrom(c), "Owner assignable from Acl");
        for (Method m : c.getDeclaredMethods()) {
            String mn = m.getName();
            Class<?>[] ex = m.getExceptionTypes();
            Set<String> exn = new HashSet<String>();
            for (Class<?> e : ex) exn.add(e.getName());
            if (mn.equals("setName") || mn.equals("addEntry") || mn.equals("removeEntry"))
                check(exn.equals(new HashSet<String>(java.util.Arrays.asList("java.security.acl.NotOwnerException"))),
                        mn + " throws exactly NotOwnerException");
            else
                check(exn.isEmpty(), mn + " declares no checked exceptions");
            if (mn.equals("getPermissions") || mn.equals("entries"))
                check(m.getReturnType() == java.util.Enumeration.class, mn + " returns Enumeration");
            if (mn.equals("addEntry") || mn.equals("removeEntry") || mn.equals("checkPermission"))
                check(m.getParameterTypes().length == 2, mn + " takes 2 params");
        }

        // ---- recording implementation: owner-gated semantics ----
        Principal owner = new RecPrincipal("owner");
        Principal stranger = new RecPrincipal("stranger");
        Principal user = new RecPrincipal("user");
        RecAcl acl = new RecAcl("acl1", owner);
        check(acl.getName().equals("acl1"), "recording getName");
        acl.setName(owner, "acl2");
        check(acl.getName().equals("acl2"), "owner setName takes effect");
        checkThrows(NotOwnerException.class, () -> acl.setName(stranger, "acl3"),
                "setName by non-owner -> NotOwnerException");
        check(acl.getName().equals("acl2"), "failed setName leaves name unchanged");
        // entries: first add true, duplicate add false, remove present true / absent false
        RecEntry e1 = new RecEntry();
        check(e1.setPrincipal(user), "setPrincipal first time true");
        check(e1.addPermission(new RecPerm("read")), "addPermission true");
        RecEntry e2 = new RecEntry();
        e2.setPrincipal(new RecPrincipal("user2"));
        e2.addPermission(new RecPerm("write"));
        check(acl.addEntry(owner, e1), "addEntry first true");
        check(!acl.addEntry(owner, e1), "addEntry duplicate false");
        check(acl.addEntry(owner, e2), "addEntry second entry true");
        checkThrows(NotOwnerException.class, () -> acl.addEntry(stranger, new RecEntry()),
                "addEntry by non-owner -> NotOwnerException");
        int cnt = 0;
        Enumeration<AclEntry> en = acl.entries();
        while (en.hasMoreElements()) { en.nextElement(); cnt++; }
        check(cnt == 2, "entries enumeration counts both entries");
        // permission aggregation by principal
        Enumeration<Permission> ps = acl.getPermissions(user);
        check(ps.hasMoreElements() && ps.nextElement().toString().equals("RecPerm[read]"),
                "getPermissions aggregates entry permissions for the principal");
        check(acl.checkPermission(user, new RecPerm("read")), "checkPermission hit");
        check(!acl.checkPermission(user, new RecPerm("write")), "checkPermission miss (other principal's perm)");
        check(acl.removeEntry(owner, e1), "removeEntry present true");
        check(!acl.removeEntry(owner, e1), "removeEntry absent false");
        checkThrows(NotOwnerException.class, () -> acl.removeEntry(stranger, e2),
                "removeEntry by non-owner -> NotOwnerException");
        // inherited Owner behaviour through the Acl reference
        check(acl.isOwner(owner), "isOwner(owner) true via Acl");
        check(!acl.isOwner(stranger), "isOwner(stranger) false");
        check(acl.addOwner(owner, stranger), "addOwner adds new owner");
        checkThrows(LastOwnerException.class, () -> {
            RecAcl single = new RecAcl("s", owner);
            single.deleteOwner(owner, owner);
        }, "deleteOwner on last owner -> LastOwnerException");
        check(acl.toString().contains("acl2"), "toString carries acl name");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
