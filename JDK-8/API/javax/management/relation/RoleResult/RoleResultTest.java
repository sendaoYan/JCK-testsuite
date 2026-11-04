/*
 * @test
 * @summary javax.management.relation.RoleResult conformance: roles + unresolved.
 * @run main RoleResultTest
 */

import javax.management.relation.*;
public class RoleResultTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        RoleResult rr = new RoleResult(new RoleList(), new RoleUnresolvedList());
        check(rr.getRoles() != null, "roles");
        check(rr.getRolesUnresolved() != null, "unresolved");
        rr.setRoles(new RoleList());
        rr.setRolesUnresolved(new RoleUnresolvedList());
        check(rr.getRoles() != null && rr.getRolesUnresolved() != null, "setters");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
