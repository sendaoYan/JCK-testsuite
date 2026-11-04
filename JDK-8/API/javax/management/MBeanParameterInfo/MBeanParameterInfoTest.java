/*
 * @test
 * @summary javax.management.MBeanParameterInfo conformance: type/description.
 * @run main MBeanParameterInfoTest
 */

import javax.management.*;
public class MBeanParameterInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        MBeanParameterInfo p = new MBeanParameterInfo("x", "int", "param");
        check("x".equals(p.getName()), "name");
        check("int".equals(p.getType()), "type");
        check("param".equals(p.getDescription()), "description");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
