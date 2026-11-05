/*
 * @test
 * @summary javax.xml.crypto.dsig.spec.XPathType conformance.
 * @run main XPathTypeTest
 */

import javax.xml.crypto.dsig.spec.XPathType;
import java.util.HashMap;
import java.util.Map;

public class XPathTypeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.xml.crypto.dsig.spec.XPathType");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getExpression"), "has getExpression");
        check(names.contains("getFilter"), "has getFilter");
        check(names.contains("getNamespaceMap"), "has getNamespaceMap");
        // Filter is a typesafe-enum class, not a java.lang.Enum
        Class<?> f = Class.forName("javax.xml.crypto.dsig.spec.XPathType$Filter");
        check(!java.lang.Enum.class.isAssignableFrom(f), "Filter is not java.lang.Enum (typesafe-enum pattern)");
        check(f.getConstructors().length == 0, "Filter has no public constructors");
    }

    static void testSemantics() {
        XPathType x1 = new XPathType("self::n", XPathType.Filter.INTERSECT);
        check(x1.getExpression().equals("self::n"), "expression round-trip");
        check(x1.getFilter() == XPathType.Filter.INTERSECT, "filter round-trip identity");
        check(x1.getNamespaceMap().isEmpty(), "default empty namespace map");
        // filter constants: three distinct instances with lowercase display names
        check(XPathType.Filter.INTERSECT.toString().equals("intersect"), "INTERSECT toString");
        check(XPathType.Filter.SUBTRACT.toString().equals("subtract"), "SUBTRACT toString");
        check(XPathType.Filter.UNION.toString().equals("union"), "UNION toString");
        check(XPathType.Filter.INTERSECT != XPathType.Filter.SUBTRACT, "distinct 1");
        check(XPathType.Filter.SUBTRACT != XPathType.Filter.UNION, "distinct 2");
        check(XPathType.Filter.INTERSECT != XPathType.Filter.UNION, "distinct 3");
        // namespace map
        Map<String, String> ns = new HashMap<String, String>();
        ns.put("a", "urn:a");
        XPathType x2 = new XPathType("a:n", XPathType.Filter.UNION, ns);
        check(x2.getNamespaceMap().size() == 1 && "urn:a".equals(x2.getNamespaceMap().get("a")), "namespace map round-trip");
        ns.put("b", "urn:b");
        check(x2.getNamespaceMap().size() == 1, "source map copied in");
        try { x2.getNamespaceMap().put("c", "urn:c"); check(false, "namespace map immutable"); }
        catch (UnsupportedOperationException e) { check(true, "namespace map immutable"); }
        // empty explicit map
        XPathType x3 = new XPathType("n", XPathType.Filter.SUBTRACT, new HashMap<String, String>());
        check(x3.getNamespaceMap().isEmpty(), "empty map stays empty");
        // filter application coverage: one XPathType per filter constant
        check(new XPathType("u", XPathType.Filter.UNION).getFilter() == XPathType.Filter.UNION, "UNION round-trip");
        check(new XPathType("s", XPathType.Filter.SUBTRACT).getFilter() == XPathType.Filter.SUBTRACT, "SUBTRACT round-trip");
        // negatives
        try { new XPathType(null, XPathType.Filter.INTERSECT); check(false, "null expression"); }
        catch (NullPointerException e) { check(true, "null expression NPE"); }
        try { new XPathType("e", null); check(false, "null filter"); }
        catch (NullPointerException e) { check(true, "null filter NPE"); }
        try { new XPathType("e", XPathType.Filter.UNION, null); check(false, "null map"); }
        catch (NullPointerException e) { check(true, "null map NPE"); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
