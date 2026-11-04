/*
 * @test
 * @summary java.security.SecurityPermission conformance: white-box structure + black-box behavior.
 * @run main SecurityPermissionTest
 */

import java.security.SecurityPermission;
import java.security.*;
import java.util.*;

public class SecurityPermissionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        SecurityPermission sp = new SecurityPermission("createAccessControlContext");
        check("createAccessControlContext".equals(sp.getName()), "getName");
        check("".equals(sp.getActions()), "getActions");
        check(sp.implies(new SecurityPermission("createAccessControlContext")), "implies");
        SecurityPermission sp2 = new SecurityPermission("setPolicy", "write");
        check("setPolicy".equals(sp2.getName()), "getName with actions");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        SecurityPermission sp = new SecurityPermission("getPolicy");
        check("getPolicy".equals(sp.getName()), "name");
        check(sp.getActions().isEmpty(), "empty actions");
        check(sp.implies(new SecurityPermission("getPolicy")), "implies");
        check(!sp.implies(new SecurityPermission("setPolicy")), "no implies");
        check(new SecurityPermission("a.b", "read").getActions().isEmpty(),
            "two-arg ctor empty actions");
        check(sp.equals(new SecurityPermission("getPolicy"))
            && sp.hashCode() == new SecurityPermission("getPolicy").hashCode(), "equals/hashCode");
        check(sp.toString().contains("getPolicy"), "toString");
        // hierarchical wildcard
        SecurityPermission wild = new SecurityPermission("insertProvider.*");
        check(wild.implies(new SecurityPermission("insertProvider.JCK")), "wildcard");
        PermissionCollection pc = sp.newPermissionCollection();
        pc.add(new SecurityPermission("getPolicy"));
        pc.add(new SecurityPermission("setPolicy"));
        check(pc.implies(new SecurityPermission("getPolicy")) && pc.implies(new SecurityPermission("setPolicy")),
            "pc implies both");
        try { new SecurityPermission(null); check(false, "NPE"); } catch (NullPointerException e) { check(true, "NPE"); }
        check(new SecurityPermission("bad.name.").getName().equals("bad.name."), "trailing dot accepted by JDK-27");
        check(!new SecurityPermission("bad.name.").implies(new SecurityPermission("bad.name.x")), "trailing dot no wildcard");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
