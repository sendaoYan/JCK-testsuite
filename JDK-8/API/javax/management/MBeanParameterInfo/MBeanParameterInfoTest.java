/*
 * @test
 * @summary javax.management.MBeanParameterInfo conformance: full contract
 *          coverage. name/type/description, descriptor ctor, clone/equals.
 * @run main MBeanParameterInfoTest
 */

import javax.management.ImmutableDescriptor;
import javax.management.MBeanParameterInfo;

public class MBeanParameterInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- original coverage (kept) ------------------------------------------

    static void testBasic() {
        MBeanParameterInfo p = new MBeanParameterInfo("x", "int", "param");
        check("x".equals(p.getName()), "name");
        check("int".equals(p.getType()), "type");
        check("param".equals(p.getDescription()), "description");
    }

    static void testDescriptorAndEquals() {
        MBeanParameterInfo p = new MBeanParameterInfo("x", "int", "param",
            new ImmutableDescriptor(new String[]{"units"}, new Object[]{"ms"}));
        check("ms".equals(p.getDescriptor().getFieldValue("units")), "descriptor");
        MBeanParameterInfo q = new MBeanParameterInfo("x", "int", "param");
        MBeanParameterInfo r = new MBeanParameterInfo("x", "int", "param");
        check(q.equals(r) && q.hashCode() == r.hashCode(), "equals/hashCode");
        check(!q.equals(p), "different descriptor -> not equal");
        MBeanParameterInfo c = (MBeanParameterInfo) q.clone();
        check(q.equals(c), "clone equals");
    }

    public static void main(String[] args) throws Exception {
        testBasic();
        testDescriptorAndEquals();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
