/*
 * @test
 * @summary java.security.acl.Permission conformance: exact two-method surface
 *          (equals + toString redeclared abstract, no other members),
 *          independence from java.security.Permission (different kind: an
 *          interface, not assignable to the class), and recording-implementation
 *          equals/hashCode/toString value semantics plus interop as the
 *          permission currency of AclEntry/Acl checkPermission.
 * @run main PermissionTest
 */

import java.lang.reflect.Method;
import java.security.acl.AclEntry;
import java.security.acl.Permission;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class PermissionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class RecPerm implements Permission {
        private final String n;
        RecPerm(String n) { this.n = n; }
        public boolean equals(Object o) { return o instanceof RecPerm && ((RecPerm) o).n.equals(n); }
        public int hashCode() { return n.hashCode(); }
        public String toString() { return "RecPerm[" + n + "]"; }
    }

    static class RecEntry implements AclEntry {
        private java.security.Principal principal;
        private boolean negative;
        private final Set<Permission> perms = new LinkedHashSet<Permission>();
        public boolean setPrincipal(java.security.Principal p) {
            if (principal != null) return false;
            principal = p;
            return true;
        }
        public java.security.Principal getPrincipal() { return principal; }
        public void setNegativePermissions() { negative = true; }
        public boolean isNegative() { return negative; }
        public boolean addPermission(Permission p) { return perms.add(p); }
        public boolean removePermission(Permission p) { return perms.remove(p); }
        public boolean checkPermission(Permission p) { return perms.contains(p); }
        public Enumeration<Permission> permissions() {
            return Collections.enumeration(new ArrayList<Permission>(perms));
        }
        public String toString() { return "RecEntry[" + principal + "]"; }
        public Object clone() { try { return super.clone(); } catch (CloneNotSupportedException e) { throw new AssertionError(e); } }
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.acl.Permission");
        check(c.isInterface(), "is interface");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("equals"), "declares equals");
        check(ms.contains("toString"), "declares toString");

        // ---- exact minimal method surface ----
        check(ms.equals(new HashSet<String>(java.util.Arrays.asList("equals", "toString"))),
                "declared methods exactly equals+toString: " + ms);
        check(c.getInterfaces().length == 0, "no super-interfaces");
        for (Method m : c.getDeclaredMethods()) {
            check(m.getExceptionTypes().length == 0, m.getName() + " declares no checked exceptions");
            if (!m.getName().equals("equals"))
                check(m.getParameterTypes().length == 0, m.getName() + " takes no parameters");
        }
        Method eq = c.getDeclaredMethod("equals", Object.class);
        check(eq.getReturnType() == boolean.class, "equals(Object) returns boolean");
        Method ts = c.getDeclaredMethod("toString");
        check(ts.getReturnType() == String.class, "toString returns String");

        // ---- independence from java.security.Permission ----
        Class<?> secPerm = java.security.Permission.class;
        check(!secPerm.isAssignableFrom(c), "java.security.Permission not assignable from acl.Permission");
        check(!c.isAssignableFrom(secPerm), "acl.Permission not assignable from java.security.Permission");
        check(secPerm.isInterface() == false && c.isInterface(), "acl.Permission is an interface, java.security.Permission a class");
        RecPerm rp = new RecPerm("read");
        check(!java.security.Permission.class.isInstance(rp),
                "recording acl permission is not a java.security.Permission");
        check(rp instanceof Permission, "recording impl is an acl.Permission");

        // ---- value semantics of a recording implementation ----
        check(rp.equals(new RecPerm("read")), "equals same name true");
        check(!rp.equals(new RecPerm("write")), "equals different name false");
        check(!rp.equals(null), "equals null false");
        check(!rp.equals("read"), "equals foreign type false");
        check(rp.hashCode() == new RecPerm("read").hashCode(), "hashCode consistent with equals");
        check(rp.toString().contains("read"), "toString carries permission name");
        check(rp.equals(rp), "equals reflexive");

        // ---- interop: the currency of AclEntry permission operations ----
        RecEntry entry = new RecEntry();
        entry.setPrincipal(null); // principal not needed for permission ops
        check(entry.addPermission(rp), "addPermission accepts acl permission");
        check(entry.addPermission(new RecPerm("write")), "second permission added");
        check(entry.checkPermission(new RecPerm("read")), "checkPermission by value (equal but distinct instance)");
        check(!entry.checkPermission(new RecPerm("exec")), "unrelated permission not granted");
        check(entry.removePermission(new RecPerm("read")), "removePermission by value");
        check(!entry.checkPermission(rp), "value removed even though identical instance remains registered");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
