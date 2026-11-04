/*
 * @test
 * @summary javax.security.auth.PrivateCredentialPermission conformance: white-box structure + black-box behavior.
 * @run main PrivateCredentialPermissionTest
 */

import java.lang.reflect.Modifier;
import java.security.AllPermission;
import java.security.Permission;
import javax.security.auth.PrivateCredentialPermission;

public class PrivateCredentialPermissionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static final String CRED = "javax.security.auth.x500.X500PrivateCredential";
    static final String PC = "javax.security.auth.x500.X500Principal";

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.security.auth.PrivateCredentialPermission");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == Permission.class, "extends java.security.Permission");
        check(c.isAnnotationPresent(Deprecated.class), "deprecated");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        PrivateCredentialPermission p = new PrivateCredentialPermission(
                CRED + " " + PC + " \"CN=Alice\"", "read");
        check(CRED.equals(p.getCredentialClass()), "getCredentialClass");
        check("read".equals(p.getActions()), "getActions");

        String[][] ps = p.getPrincipals();
        check(ps.length == 1 && ps[0].length == 2 && PC.equals(ps[0][0]) && "CN=Alice".equals(ps[0][1]),
                "getPrincipals single pair");

        // actions are case-insensitive and must be "read"
        PrivateCredentialPermission upper = new PrivateCredentialPermission(
                CRED + " " + PC + " \"CN=Alice\"", "READ");
        check("read".equals(upper.getActions()), "READ accepted and normalized");

        boolean iae = false;
        try { new PrivateCredentialPermission(CRED + " " + PC + " \"CN=Alice\"", "write"); }
        catch (IllegalArgumentException e) { iae = true; }
        check(iae, "non-read actions throws IllegalArgumentException");

        // name with multiple principals + name containing spaces
        PrivateCredentialPermission multi = new PrivateCredentialPermission(
                CRED + " " + PC + " \"CN=Alice\" " + PC + " \"O=Org\"", "read");
        String[][] mp = multi.getPrincipals();
        check(mp.length == 2 && PC.equals(mp[0][0]) && "CN=Alice".equals(mp[0][1])
                && PC.equals(mp[1][0]) && "O=Org".equals(mp[1][1]), "getPrincipals two pairs");

        PrivateCredentialPermission spaced = new PrivateCredentialPermission(
                CRED + " " + PC + " \"CN=Alice  Smith\"", "read");
        check(spaced.getPrincipals()[0][1].equals("CN=Alice  Smith"), "principal name with spaces preserved");

        // invalid names
        checkIllegalName("", "empty name");
        checkIllegalName(null, "null name");
        checkIllegalName(CRED, "credential class only (no principal)");
        checkIllegalName(CRED + " " + PC + " CN=Alice", "unquoted principal name");

        // wildcard credential class implication
        PrivateCredentialPermission starCred = new PrivateCredentialPermission(
                "* " + PC + " \"CN=Alice\"", "read");
        check(starCred.implies(p), "wildcard credential class implies specific");
        check(!p.implies(starCred), "specific credential class does not imply wildcard");

        // principal subset implication
        PrivateCredentialPermission subset = new PrivateCredentialPermission(
                CRED + " " + PC + " \"CN=Alice\"", "read");
        check(subset.implies(multi), "subset principals implies superset");
        check(!multi.implies(subset), "superset principals does not imply subset");

        // wildcard principal name
        PrivateCredentialPermission starName = new PrivateCredentialPermission(
                CRED + " " + PC + " \"CN=Alice\" " + PC + " \"*\"", "read");
        check(starName.implies(multi), "wildcard principal name implies concrete");

        // non-PrivateCredentialPermission never implied
        check(!p.implies(new AllPermission()), "implies(AllPermission) false");

        // equals: principal order irrelevant
        PrivateCredentialPermission rev = new PrivateCredentialPermission(
                CRED + " " + PC + " \"O=Org\" " + PC + " \"CN=Alice\"", "read");
        check(multi.equals(rev) && rev.equals(multi), "equals ignores principal order");
        check(multi.hashCode() == rev.hashCode(), "equal perms equal hashCode");

        PrivateCredentialPermission otherName = new PrivateCredentialPermission(
                CRED + " " + PC + " \"CN=Bob\"", "read");
        check(!multi.equals(otherName), "different principal name not equal");

        // newPermissionCollection returns null per spec
        check(p.newPermissionCollection() == null, "newPermissionCollection returns null");

        // getName returns the original name string
        check(p.getName().equals(CRED + " " + PC + " \"CN=Alice\""), "getName returns original name");
    }

    static void checkIllegalName(String name, String label) {
        boolean iae = false;
        try { new PrivateCredentialPermission(name, "read"); }
        catch (IllegalArgumentException e) { iae = true; }
        check(iae, label + " throws IllegalArgumentException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
