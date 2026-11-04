/*
 * @test
 * @summary java.security.BasicPermission conformance: white-box structure + black-box behavior.
 * @run main BasicPermissionTest
 */

import java.security.BasicPermission;
import java.security.PermissionCollection;
import java.security.*;
import java.util.*;

public class BasicPermissionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static class MyPerm extends BasicPermission {
        MyPerm(String name) { super(name); }
        MyPerm(String name, String actions) { super(name, actions); }
    }
    static void testWhiteBox() throws Exception {
        MyPerm p = new MyPerm("java.home");
        check("java.home".equals(p.getName()), "getName");
        check("".equals(p.getActions()), "getActions empty");
        check(p.implies(new MyPerm("java.home")), "implies exact");
        check(!p.implies(new MyPerm("java.*")), "no implies wildcard parent");
        MyPerm wild = new MyPerm("java.*");
        check(wild.implies(new MyPerm("java.home")), "wildcard implies child");
        PermissionCollection pc = p.newPermissionCollection();
        check(pc != null, "newPermissionCollection");
        pc.add(new MyPerm("java.home"));
        check(pc.implies(p), "collection implies");
        check(p.equals(new MyPerm("java.home")), "equals");
        check(p.hashCode() == new MyPerm("java.home").hashCode(), "hashCode");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        // BasicPermission semantics exercised through its concrete subclass SecurityPermission
        SecurityPermission sp = new SecurityPermission("jck.node");
        check(sp.getActions().isEmpty(), "empty actions");
        check(sp.getName().equals("jck.node"), "name");
        check(sp.implies(new SecurityPermission("jck.node")), "implies exact");
        check(!sp.implies(new SecurityPermission("jck.node.sub")), "no implies child");
        check(!sp.implies(new SecurityPermission("jck")), "no implies parent");
        SecurityPermission wild = new SecurityPermission("jck.*");
        check(wild.implies(new SecurityPermission("jck.a")), "wildcard child");
        check(wild.implies(new SecurityPermission("jck.a.b.c")), "wildcard deep child");
        check(!wild.implies(new SecurityPermission("jck")), "wildcard no bare parent");
        check(!wild.implies(new SecurityPermission("jcx.a")), "wildcard sibling");
        SecurityPermission star = new SecurityPermission("*");
        check(star.implies(new SecurityPermission("any.name.here")), "star implies");
        check(!sp.implies(new RuntimePermission("jck.node")), "cross-class implies false");
        // equals/hashCode contract
        check(sp.equals(new SecurityPermission("jck.node")), "equals");
        check(!sp.equals(new SecurityPermission("jck.other")), "not equals");
        check(sp.hashCode() == new SecurityPermission("jck.node").hashCode(), "hashCode");
        // Illegal constructions
        try { new SecurityPermission(null); check(false, "null name should NPE"); }
        catch (NullPointerException npe) { check(true, "null name NPE"); }
        try { new SecurityPermission(""); check(false, "empty name should IAE"); }
        catch (IllegalArgumentException iae) { check(true, "empty name IAE"); }
        check(new SecurityPermission("jck..bad").getName().equals("jck..bad"), "'..' name accepted by JDK-27");
        check(!new SecurityPermission("jck..bad").implies(new SecurityPermission("jck.a")), "'..' name no wildcard");
        // newPermissionCollection from BasicPermission family
        check(sp.newPermissionCollection() != null, "newPermissionCollection non-null");
        PermissionCollection pc = sp.newPermissionCollection();
        pc.add(sp);
        check(pc.implies(new SecurityPermission("jck.node")), "pc implies");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
