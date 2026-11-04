/*
 * @test
 * @summary javax.management.MBeanConstructorInfo conformance: full contract
 *          coverage. signature, null signature -> empty, defensive getter,
 *          clone/equals.
 * @run main MBeanConstructorInfoTest
 */

import javax.management.MBeanConstructorInfo;
import javax.management.MBeanParameterInfo;

public class MBeanConstructorInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- original coverage (kept) ------------------------------------------

    static void testBasic() {
        MBeanParameterInfo p = new MBeanParameterInfo("x", "int", "param");
        MBeanConstructorInfo c = new MBeanConstructorInfo("ctor", "desc", new MBeanParameterInfo[]{p});
        check("ctor".equals(c.getName()), "name");
        check(c.getSignature().length == 1 && "x".equals(c.getSignature()[0].getName()), "signature");
    }

    static void testNullSignatureAndDefensiveGetter() {
        MBeanConstructorInfo c = new MBeanConstructorInfo("ctor", "desc", null);
        check(c.getSignature().length == 0, "null signature -> empty");

        MBeanConstructorInfo withParam = new MBeanConstructorInfo("ctor", "desc",
            new MBeanParameterInfo[]{new MBeanParameterInfo("p", "int", "d")});
        withParam.getSignature()[0] = null;
        check(withParam.getSignature()[0] != null, "defensive copy");
    }

    static void testCloneEquals() {
        MBeanConstructorInfo c = new MBeanConstructorInfo("ctor", "desc",
            new MBeanParameterInfo[]{new MBeanParameterInfo("p", "int", "d")});
        MBeanConstructorInfo copy = (MBeanConstructorInfo) c.clone();
        check(c.equals(copy) && c.hashCode() == copy.hashCode(), "clone equals/hashCode");
    }

    public static void main(String[] args) throws Exception {
        testBasic();
        testNullSignatureAndDefensiveGetter();
        testCloneEquals();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
