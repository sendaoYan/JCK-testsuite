/*
 * @test
 * @summary java.security.acl.Group conformance: exact method signatures
 *          (addMember/removeMember/isMember boolean, members Enumeration of
 *          Principal), Group extends Principal inheritance, and recording-
 *          implementation semantics: membership add/remove/idempotence,
 *          members enumeration reflects the live set, null-arg behavior,
 *          equals/hashCode/getName inherited Principal contract.
 * @run main GroupTest
 */

import java.lang.reflect.Method;
import java.security.Principal;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class GroupTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class P implements Principal {
        private final String n;
        P(String n) { this.n = n; }
        public String getName() { return n; }
        public boolean equals(Object o) { return o instanceof P && ((P) o).n.equals(n); }
        public int hashCode() { return n.hashCode(); }
        public String toString() { return "P[" + n + "]"; }
    }

    static class RecGroup implements Group {
        private final String name;
        private final Set<Principal> members = new LinkedHashSet<Principal>();
        RecGroup(String name) { this.name = name; }
        public boolean addMember(Principal p) { return members.add(p); }
        public boolean removeMember(Principal p) { return members.remove(p); }
        public boolean isMember(Principal p) { return members.contains(p); }
        public Enumeration<? extends Principal> members() {
            return Collections.enumeration(new ArrayList<Principal>(members));
        }
        public String getName() { return name; }
        public boolean equals(Object o) { return o instanceof RecGroup && ((RecGroup) o).name.equals(name); }
        public int hashCode() { return name.hashCode(); }
        public String toString() { return "RecGroup[" + name + "," + members.size() + "]"; }
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.acl.Group");
        check(c.isInterface(), "is interface");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("addMember"), "declares addMember");
        check(ms.contains("isMember"), "declares isMember");
        check(ms.contains("removeMember"), "declares removeMember");

        // ---- exact method set + signature precision ----
        Set<String> expect = new HashSet<String>();
        for (String n : new String[]{"addMember", "isMember", "members", "removeMember"}) expect.add(n);
        check(ms.equals(expect), "declared method set exactly 4 methods: " + ms);
        check(c.getInterfaces().length == 1 && c.getInterfaces()[0] == Principal.class,
                "extends java.security.Principal only");
        check(Principal.class.isAssignableFrom(c), "Principal assignable from Group");
        for (Method m : c.getDeclaredMethods()) {
            String mn = m.getName();
            check(m.getExceptionTypes().length == 0, mn + " declares no checked exceptions");
            if (mn.equals("members")) {
                check(m.getReturnType() == java.util.Enumeration.class, "members returns Enumeration");
                check(m.getParameterTypes().length == 0, "members takes no args");
            } else {
                check(m.getReturnType() == boolean.class, mn + " returns boolean");
                check(m.getParameterTypes().length == 1
                        && m.getParameterTypes()[0] == Principal.class, mn + " takes one Principal");
            }
        }

        // ---- recording implementation: membership semantics ----
        RecGroup g = new RecGroup("admins");
        check(g.getName().equals("admins"), "getName via Principal contract");
        Principal a = new P("alice");
        Principal b = new P("bob");
        check(!g.isMember(a), "empty group has no members");
        check(g.addMember(a), "addMember new -> true");
        check(!g.addMember(a), "addMember duplicate -> false");
        check(g.addMember(b), "addMember second -> true");
        check(g.isMember(a) && g.isMember(b), "isMember true for both");
        check(!g.isMember(new P("carol")), "isMember false for non-member");
        check(!g.isMember(null), "isMember(null) false");
        int cnt = 0;
        boolean sawA = false, sawB = false;
        Enumeration<? extends Principal> en = g.members();
        while (en.hasMoreElements()) {
            String nm = en.nextElement().getName();
            cnt++;
            if (nm.equals("alice")) sawA = true;
            if (nm.equals("bob")) sawB = true;
        }
        check(cnt == 2 && sawA && sawB, "members enumerates exactly the member set");
        check(g.removeMember(a), "removeMember present -> true");
        check(!g.removeMember(a), "removeMember absent -> false");
        check(!g.isMember(a) && g.isMember(b), "membership updated after remove");
        cnt = 0;
        en = g.members();
        while (en.hasMoreElements()) { en.nextElement(); cnt++; }
        check(cnt == 1, "members reflects removal");
        // Principal contract on the group itself
        check(g.equals(new RecGroup("admins")), "equals by name across instances");
        check(!g.equals(new RecGroup("users")), "different name unequal");
        check(g.hashCode() == new RecGroup("admins").hashCode(), "hashCode consistent with equals");
        Principal asPrincipal = g;
        check(asPrincipal.getName().equals("admins"), "group usable as Principal");
        check(g.toString().contains("admins"), "toString carries group name");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
