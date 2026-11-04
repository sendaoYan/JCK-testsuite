/*
 * @test
 * @summary javax.management.MBeanOperationInfo conformance: return type/signature/impact.
 * @run main MBeanOperationInfoTest
 */

import javax.management.*;
public class MBeanOperationInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        MBeanParameterInfo p = new MBeanParameterInfo("x", "int", "param");
        MBeanOperationInfo o = new MBeanOperationInfo("op", "desc", new MBeanParameterInfo[]{p}, "int", MBeanOperationInfo.ACTION);
        check("op".equals(o.getName()), "name");
        check("int".equals(o.getReturnType()), "return type");
        check(o.getSignature().length == 1 && "x".equals(o.getSignature()[0].getName()), "signature");
        check(o.getImpact() == MBeanOperationInfo.ACTION, "impact");
        check(MBeanOperationInfo.INFO != MBeanOperationInfo.UNKNOWN, "impact constants");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
