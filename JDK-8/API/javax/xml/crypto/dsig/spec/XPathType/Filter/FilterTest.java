/*
 * @test
 * @summary javax.xml.crypto.dsig.spec.XPathType.Filter conformance.
 * @run main FilterTest
 */

import javax.xml.crypto.dsig.spec.XPathType;

public class FilterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.xml.crypto.dsig.spec.XPathType$Filter");
        // typesafe-enum pattern: final class, NOT a java.lang.Enum
        check(!java.lang.Enum.class.isAssignableFrom(c), "not java.lang.Enum");
        check(c.getConstructors().length == 0, "no public constructors (singleton instances)");
        java.lang.reflect.Field inter = c.getField("INTERSECT");
        java.lang.reflect.Field sub = c.getField("SUBTRACT");
        java.lang.reflect.Field uni = c.getField("UNION");
        check(java.lang.reflect.Modifier.isStatic(inter.getModifiers())
           && java.lang.reflect.Modifier.isFinal(inter.getModifiers())
           && inter.getType() == c, "INTERSECT public static final Filter");
        check(sub.getType() == c && uni.getType() == c, "SUBTRACT/UNION typed Filter");
    }

    static void testSemantics() {
        check(XPathType.Filter.INTERSECT.toString().equals("intersect"), "INTERSECT toString intersect");
        check(XPathType.Filter.SUBTRACT.toString().equals("subtract"), "SUBTRACT toString subtract");
        check(XPathType.Filter.UNION.toString().equals("union"), "UNION toString union");
        // exactly three distinct canonical instances
        check(XPathType.Filter.INTERSECT != XPathType.Filter.SUBTRACT, "INTERSECT != SUBTRACT");
        check(XPathType.Filter.SUBTRACT != XPathType.Filter.UNION, "SUBTRACT != UNION");
        check(XPathType.Filter.INTERSECT != XPathType.Filter.UNION, "INTERSECT != UNION");
        // the three display names are pairwise distinct
        check(!XPathType.Filter.INTERSECT.toString().equals(XPathType.Filter.SUBTRACT.toString()), "names distinct 1");
        check(!XPathType.Filter.SUBTRACT.toString().equals(XPathType.Filter.UNION.toString()), "names distinct 2");
        // toString is stable across calls
        check(XPathType.Filter.INTERSECT.toString().equals(XPathType.Filter.INTERSECT.toString()), "stable toString");
        // constants usable in XPathType round-trip for every filter
        check(new javax.xml.crypto.dsig.spec.XPathType("i", XPathType.Filter.INTERSECT).getFilter()
            == XPathType.Filter.INTERSECT, "INTERSECT usable in XPathType");
        check(new javax.xml.crypto.dsig.spec.XPathType("s", XPathType.Filter.SUBTRACT).getFilter()
            == XPathType.Filter.SUBTRACT, "SUBTRACT usable in XPathType");
        check(new javax.xml.crypto.dsig.spec.XPathType("u", XPathType.Filter.UNION).getFilter()
            == XPathType.Filter.UNION, "UNION usable in XPathType");
        // equals inherited from Object: identity semantics
        check(XPathType.Filter.INTERSECT.equals(XPathType.Filter.INTERSECT), "self equals");
        check(!XPathType.Filter.INTERSECT.equals(XPathType.Filter.UNION), "cross unequal");
        check(XPathType.Filter.INTERSECT.hashCode() == XPathType.Filter.INTERSECT.hashCode(), "hashCode stable");
        // not null-safe comparisons: constants never null
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
