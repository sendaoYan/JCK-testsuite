/*
 * @test
 * @summary java.security.UnresolvedPermission conformance: white-box structure + black-box behavior.
 * @run main UnresolvedPermissionTest
 */

import java.security.UnresolvedPermission;
import java.security.*;
import java.util.*;

public class UnresolvedPermissionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        UnresolvedPermission up = new UnresolvedPermission("java.io.FilePermission", "read", "read", null);
        check("java.io.FilePermission".equals(up.getUnresolvedType()), "getUnresolvedType");
        check("read".equals(up.getUnresolvedName()), "getUnresolvedName");
        check("read".equals(up.getUnresolvedActions()), "getUnresolvedActions");
        check(up.getUnresolvedCerts() == null, "getUnresolvedCerts null");
        check("".equals(up.getActions()), "getActions empty");
        UnresolvedPermission up2 = new UnresolvedPermission("java.io.FilePermission", "read", "read", null);
        check(up.equals(up2), "equals");
        check(up.hashCode() == up2.hashCode(), "hashCode");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        UnresolvedPermission up = new UnresolvedPermission(
            "java.security.SecurityPermission", "getPolicy", "read,write", null);
        check(up.getName().equals("java.security.SecurityPermission"), "name=type");
        check(up.getActions().isEmpty(), "empty actions");
        check(!up.implies(up) && !up.implies(new SecurityPermission("getPolicy")), "implies false");
        check(up.getUnresolvedType().equals("java.security.SecurityPermission"), "type");
        check(up.getUnresolvedName().equals("getPolicy"), "unresolved name");
        check(up.getUnresolvedActions().equals("read,write"), "unresolved actions");
        check(up.getUnresolvedCerts() == null, "null certs");
        check(up.toString().contains("java.security.SecurityPermission"), "toString");
        check(up.newPermissionCollection() != null, "npc");
        check(up.equals(new UnresolvedPermission("java.security.SecurityPermission", "getPolicy", "read,write", null)),
            "equals");
        check(up.hashCode() == new UnresolvedPermission("java.security.SecurityPermission", "getPolicy", "read,write", null).hashCode(),
            "hashCode");
        // Permissions resolves a matching UnresolvedPermission on the fly
        Permissions perms = new Permissions();
        perms.add(up);
        check(perms.implies(new SecurityPermission("getPolicy")), "matching unresolved resolved");
        check(!perms.implies(new SecurityPermission("other.name")), "non-matching unresolved not implied");
        Enumeration<Permission> e = perms.elements();
        check(e.hasMoreElements() && e.nextElement() == up, "elements contains unresolved");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
