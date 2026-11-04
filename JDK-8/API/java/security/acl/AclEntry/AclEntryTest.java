/*
 * @test
 * @summary java.security.acl.AclEntry conformance: exact method set (extends
 *          Cloneable), signature precision (no checked exceptions, clone returns
 *          Object), and recording-implementation semantics: setPrincipal true
 *          only before assignment, setNegativePermissions one-way, isNegative,
 *          addPermission/removePermission boolean contract, checkPermission,
 *          permissions enumeration, clone availability, principal nullability.
 * @run main AclEntryTest
 */

import java.lang.reflect.Method;
import java.security.Principal;
import java.security.acl.AclEntry;
import java.security.acl.Permission;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class AclEntryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
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
        private Set<Permission> perms = new LinkedHashSet<Permission>();
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
                c.perms = new LinkedHashSet<Permission>(perms); // independent copy
                return c;
            } catch (CloneNotSupportedException e) { throw new AssertionError(e); }
        }
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.acl.AclEntry");
        check(c.isInterface(), "is interface");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("addPermission"), "declares addPermission");
        check(ms.contains("checkPermission"), "declares checkPermission");
        check(ms.contains("clone"), "declares clone");
        check(ms.contains("getPrincipal"), "declares getPrincipal");
        check(ms.contains("isNegative"), "declares isNegative");
        check(ms.contains("permissions"), "declares permissions");
        check(ms.contains("removePermission"), "declares removePermission");
        check(ms.contains("setNegativePermissions"), "declares setNegativePermissions");
        check(ms.contains("setPrincipal"), "declares setPrincipal");
        check(ms.contains("toString"), "declares toString");

        // ---- exact method-set + signature precision ----
        Set<String> expect = new HashSet<String>();
        for (String n : new String[]{"addPermission", "checkPermission", "clone", "getPrincipal",
                "isNegative", "permissions", "removePermission", "setNegativePermissions",
                "setPrincipal", "toString"}) expect.add(n);
        check(ms.equals(expect), "declared method set exactly the 10 javadoc methods: " + ms);
        check(c.getInterfaces().length == 1 && c.getInterfaces()[0] == Cloneable.class,
                "extends Cloneable only");
        check(Cloneable.class.isAssignableFrom(c), "Cloneable assignable");
        for (Method m : c.getDeclaredMethods()) {
            check(m.getExceptionTypes().length == 0,
                    m.getName() + " declares no checked exceptions");
            if (m.getName().equals("clone"))
                check(m.getReturnType() == Object.class, "clone returns Object");
            if (m.getName().equals("setPrincipal") || m.getName().equals("addPermission")
                    || m.getName().equals("removePermission") || m.getName().equals("checkPermission"))
                check(m.getReturnType() == boolean.class, m.getName() + " returns boolean");
        }

        // ---- recording implementation: full behavioural contract ----
        RecEntry e = new RecEntry();
        check(e.getPrincipal() == null, "principal initially null");
        check(!e.isNegative(), "entry initially positive");
        Principal u1 = new RecPrincipal("u1");
        Principal u2 = new RecPrincipal("u2");
        check(e.setPrincipal(u1), "setPrincipal first assignment true");
        check(!e.setPrincipal(u2), "setPrincipal second assignment false");
        check(e.getPrincipal() == u1, "principal unchanged after rejected re-set");
        // permissions add/remove/contains contract
        RecPerm read = new RecPerm("read");
        RecPerm write = new RecPerm("write");
        check(e.addPermission(read), "addPermission new true");
        check(!e.addPermission(read), "addPermission duplicate false");
        check(e.addPermission(write), "addPermission second true");
        check(e.checkPermission(read), "checkPermission present true");
        check(!e.checkPermission(new RecPerm("exec")), "checkPermission absent false");
        int cnt = 0;
        boolean sawRead = false, sawWrite = false;
        Enumeration<Permission> en = e.permissions();
        while (en.hasMoreElements()) {
            String s = en.nextElement().toString();
            cnt++;
            if (s.equals("RecPerm[read]")) sawRead = true;
            if (s.equals("RecPerm[write]")) sawWrite = true;
        }
        check(cnt == 2 && sawRead && sawWrite, "permissions enumerates exactly added set");
        check(e.removePermission(read), "removePermission present true");
        check(!e.removePermission(read), "removePermission absent false");
        check(!e.checkPermission(read), "removed permission no longer checks true");
        // negative is one-way
        e.setNegativePermissions();
        check(e.isNegative(), "setNegativePermissions makes entry negative");
        e.setNegativePermissions();
        check(e.isNegative(), "still negative after repeat call (one-way)");
        // clone availability + independence
        RecEntry cl = (RecEntry) e.clone();
        check(cl != e && cl.getClass() == e.getClass(), "clone distinct same-class instance");
        check(cl.isNegative() == e.isNegative() && cl.getPrincipal().equals(e.getPrincipal()),
                "clone carries state");
        cl.addPermission(new RecPerm("extra"));
        check(!e.checkPermission(new RecPerm("extra")), "clone mutation does not leak into original");
        check(e.toString().contains("u1"), "toString carries principal");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
