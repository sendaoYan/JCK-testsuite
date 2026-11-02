/*
 * @test
 * @summary javax.management.relation.RoleList conformance: add/asList.
 * @run main RoleListTest
 */

import javax.management.*;
import javax.management.relation.*;
public class RoleListTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        RoleList rl = new RoleList();
        Role r = new Role("r", new java.util.ArrayList<ObjectName>());
        rl.add(r);
        check(rl.size() == 1, "size");
        check(rl.asList().get(0) == r, "asList");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
