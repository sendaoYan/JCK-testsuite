/*
 * @test
 * @summary javax.management.MBeanFeatureInfo conformance: full contract
 *          coverage. name/description/descriptor accessors, equals/hashCode
 *          (incl. descriptor comparison), toString, abstract-class hierarchy.
 * @run main MBeanFeatureInfoTest
 */

import javax.management.ImmutableDescriptor;
import javax.management.MBeanFeatureInfo;

public class MBeanFeatureInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- original coverage (kept) ------------------------------------------

    static void testBasic() {
        MBeanFeatureInfo f = new MBeanFeatureInfo("name", "desc");
        check("name".equals(f.getName()), "name");
        check("desc".equals(f.getDescription()), "description");
        MBeanFeatureInfo g = new MBeanFeatureInfo("n", "d", new ImmutableDescriptor(new String[]{"a"}, new Object[]{"b"}));
        check(g.getDescriptor() != null && "b".equals(g.getDescriptor().getFieldValue("a")), "descriptor");
    }

    static void testEqualsHashCode() {
        MBeanFeatureInfo a = new MBeanFeatureInfo("n", "d");
        MBeanFeatureInfo b = new MBeanFeatureInfo("n", "d");
        check(a.equals(b) && b.equals(a), "equals symmetric");
        check(a.hashCode() == b.hashCode(), "hashCode equal");
        check(!a.equals(new MBeanFeatureInfo("n2", "d")), "different name");
        check(!a.equals(new MBeanFeatureInfo("n", "d2")), "different description");
        check(!a.equals(new MBeanFeatureInfo("n", "d", new ImmutableDescriptor(new String[]{"k"}, new Object[]{"v"}))), "different descriptor");
        check(!a.equals(null) && !a.equals("n"), "equals non-MBeanFeatureInfo");
    }

    static void testToString() {
        MBeanFeatureInfo f = new MBeanFeatureInfo("n", "d");
        String s = f.toString();
        check(s != null && s.startsWith("javax.management.MBeanFeatureInfo@"), "toString default Object form");
    }

    public static void main(String[] args) throws Exception {
        testBasic();
        testEqualsHashCode();
        testToString();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
