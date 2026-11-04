/*
 * @test
 * @summary javax.management.MBeanConstructorInfo conformance: signature.
 * @run main MBeanConstructorInfoTest
 */

import javax.management.*;
public class MBeanConstructorInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        MBeanParameterInfo p = new MBeanParameterInfo("x", "int", "param");
        MBeanConstructorInfo c = new MBeanConstructorInfo("ctor", "desc", new MBeanParameterInfo[]{p});
        check("ctor".equals(c.getName()), "name");
        check(c.getSignature().length == 1 && "x".equals(c.getSignature()[0].getName()), "signature");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
