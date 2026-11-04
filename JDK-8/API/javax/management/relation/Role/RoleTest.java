/*
 * @test
 * @summary javax.management.relation.Role conformance: name/value.
 * @run main RoleTest
 */

import javax.management.*;
import javax.management.relation.*;
public class RoleTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        java.util.List<ObjectName> l = new java.util.ArrayList<ObjectName>();
        l.add(new ObjectName("d:type=x"));
        Role r = new Role("rname", l);
        check("rname".equals(r.getRoleName()), "role name");
        check(r.getRoleValue().size() == 1, "role value size");
        check(r.clone() != r, "clone");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
