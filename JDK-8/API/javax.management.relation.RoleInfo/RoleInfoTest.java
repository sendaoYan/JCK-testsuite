/*
 * @test
 * @summary javax.management.relation.RoleInfo conformance: name/degree/readable.
 * @run main RoleInfoTest
 */

import javax.management.relation.*;
public class RoleInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        RoleInfo ri = new RoleInfo("rname", "java.lang.String");
        check("rname".equals(ri.getName()), "name");
        check(ri.isReadable() && ri.isWritable(), "readable/writable");
        check(ri.getMinDegree() == 1 && ri.getMaxDegree() == 1, "degree");
        check(ri.checkMinDegree(1) && ri.checkMaxDegree(1), "check degree");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
