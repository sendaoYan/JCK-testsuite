/*
 * @test
 * @summary java.security.Permissions conformance: white-box structure + black-box behavior.
 * @run main PermissionsTest
 */

import java.security.Permissions;
import java.security.AllPermission;
import java.security.SecurityPermission;
import java.security.*;
import java.util.*;

public class PermissionsTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        Permissions p = new Permissions();
        check(!p.implies(new SecurityPermission("a")), "empty implies false");
        p.add(new SecurityPermission("a"));
        check(p.implies(new SecurityPermission("a")), "implies added");
        check(!p.implies(new SecurityPermission("b")), "not implies absent");
        java.util.Enumeration<java.security.Permission> e = p.elements();
        check(e.hasMoreElements(), "elements has more");
        e.nextElement();
        check(!e.hasMoreElements(), "elements exhausted");
        p.add(new AllPermission());
        check(p.implies(new SecurityPermission("anything")), "all permission implies");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        Permissions perms = new Permissions();
        check(!perms.implies(new SecurityPermission("a")), "empty implies");
        check(!perms.elements().hasMoreElements(), "empty elements");
        perms.add(new SecurityPermission("jck.security"));
        perms.add(new PropertyPermission("jck.*", "read"));
        check(!perms.implies(new SecurityPermission("jck.other")), "no implies absent permission");
        check(!perms.implies(new PropertyPermission("jck.sub.key", "write")), "no implies write");
        perms.add(new AllPermission());
        check(perms.implies(new RuntimePermission("exitVM.42")), "AllPermission implies");
        check(perms.implies(new SecurityPermission("jck.security")), "SecurityPermission implies");
        check(perms.implies(new SecurityPermission("jck.other")), "AllPermission implies everything");
        check(perms.implies(new PropertyPermission("jck.sub.key", "write")), "AllPermission implies write");
        check(perms.implies(new PropertyPermission("jck.sub.key", "read")), "implies read after AllPermission");
        Enumeration<Permission> e = perms.elements();
        int n = 0;
        List<String> names = new ArrayList<>();
        while (e.hasMoreElements()) { Permission p = e.nextElement(); names.add(p.getName()); n++; }
        Collections.sort(names);
        check(n == 3, "elements count 3");
        check(names.get(0).equals("<all permissions>") && names.get(1).equals("jck.*") && names.get(2).equals("jck.security"),
            "sorted element names");
        // Permissions collection itself is writable and read-only toggle works
        check(!perms.isReadOnly(), "writable");
        perms.setReadOnly();
        try {
            perms.add(new SecurityPermission("late"));
            check(false, "add on readOnly should throw");
        } catch (SecurityException se) {
            check(true, "readOnly add throws");
        }
        // implies still holds after read-only
        check(perms.implies(new SecurityPermission("jck.security")), "implies after readOnly");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
