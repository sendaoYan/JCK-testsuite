/*
 * @test
 * @summary javax.management.relation.RoleUnresolved conformance: name/value/problem.
 * @run main RoleUnresolvedTest
 */

import javax.management.*;
import javax.management.relation.*;
public class RoleUnresolvedTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        java.util.List<ObjectName> l = new java.util.ArrayList<ObjectName>();
        l.add(new ObjectName("d:type=x"));
        RoleUnresolved ru = new RoleUnresolved("rname", l, RoleStatus.NO_ROLE_WITH_NAME);
        check("rname".equals(ru.getRoleName()), "role name");
        check(ru.getRoleValue().size() == 1, "role value");
        check(ru.getProblemType() == RoleStatus.NO_ROLE_WITH_NAME, "problem type");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
