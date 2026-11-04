/*
 * @test
 * @summary javax.management.relation.RoleUnresolvedList conformance: add/asList.
 * @run main RoleUnresolvedListTest
 */

import javax.management.*;
import javax.management.relation.*;
public class RoleUnresolvedListTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        RoleUnresolvedList rul = new RoleUnresolvedList();
        RoleUnresolved ru = new RoleUnresolved("r", new java.util.ArrayList<ObjectName>(), 1);
        rul.add(ru);
        check(rul.size() == 1, "size");
        check(rul.asList().get(0) == ru, "asList");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
