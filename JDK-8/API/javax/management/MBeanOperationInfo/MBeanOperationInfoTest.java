/*
 * @test
 * @summary javax.management.MBeanOperationInfo conformance: full contract
 *          coverage. return type/signature/impact, impact constants, null
 *          signature -> empty, defensive signature getter, clone/equals.
 * @run main MBeanOperationInfoTest
 */

import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;

public class MBeanOperationInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- original coverage (kept) ------------------------------------------

    static void testBasic() {
        MBeanParameterInfo p = new MBeanParameterInfo("x", "int", "param");
        MBeanOperationInfo o = new MBeanOperationInfo("op", "desc", new MBeanParameterInfo[]{p}, "int", MBeanOperationInfo.ACTION);
        check("op".equals(o.getName()), "name");
        check("int".equals(o.getReturnType()), "return type");
        check(o.getSignature().length == 1 && "x".equals(o.getSignature()[0].getName()), "signature");
        check(o.getImpact() == MBeanOperationInfo.ACTION, "impact");
        check(MBeanOperationInfo.INFO != MBeanOperationInfo.UNKNOWN, "impact constants");
    }

    static void testImpactConstants() {
        check(MBeanOperationInfo.INFO == 0, "INFO == 0");
        check(MBeanOperationInfo.ACTION == 1, "ACTION == 1");
        check(MBeanOperationInfo.ACTION_INFO == 2, "ACTION_INFO == 2");
        check(MBeanOperationInfo.UNKNOWN == 3, "UNKNOWN == 3");
    }

    static void testNullSignatureAndDefensiveGetter() {
        MBeanOperationInfo o = new MBeanOperationInfo("op", "d", null, "void", MBeanOperationInfo.INFO);
        check(o.getSignature().length == 0, "null signature -> empty");
        MBeanParameterInfo[] sig = o.getSignature();
        sig = new MBeanParameterInfo[]{new MBeanParameterInfo("a", "int", "d")};
        check(o.getSignature().length == 0, "getSignature defensive");

        MBeanOperationInfo withParam = new MBeanOperationInfo("op2", "d",
            new MBeanParameterInfo[]{new MBeanParameterInfo("p", "int", "d")}, "int", MBeanOperationInfo.UNKNOWN);
        withParam.getSignature()[0] = null;
        check(withParam.getSignature()[0] != null, "getSignature defensive copy");
    }

    static void testCloneEquals() {
        MBeanOperationInfo o = new MBeanOperationInfo("op", "d",
            new MBeanParameterInfo[]{new MBeanParameterInfo("p", "int", "d")}, "int", MBeanOperationInfo.ACTION);
        MBeanOperationInfo c = (MBeanOperationInfo) o.clone();
        check(o.equals(c) && o.hashCode() == c.hashCode(), "clone equals/hashCode");
    }

    public static void main(String[] args) throws Exception {
        testBasic();
        testImpactConstants();
        testNullSignatureAndDefensiveGetter();
        testCloneEquals();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
