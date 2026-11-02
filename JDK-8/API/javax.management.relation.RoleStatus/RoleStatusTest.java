/*
 * @test
 * @summary javax.management.relation.RoleStatus conformance: isRoleStatus.
 * @run main RoleStatusTest
 */

import javax.management.relation.*;
public class RoleStatusTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        check(RoleStatus.isRoleStatus(RoleStatus.NO_ROLE_WITH_NAME), "valid status");
        check(RoleStatus.isRoleStatus(RoleStatus.REF_MBEAN_NOT_REGISTERED), "valid status 2");
        check(!RoleStatus.isRoleStatus(99999), "invalid status");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
