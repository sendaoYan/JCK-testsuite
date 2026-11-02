/*
 * @test
 * @summary javax.xml.crypto.dsig.spec.XPathFilter2ParameterSpec conformance: xpath filter2 parameter spec.
 * @run main XPathFilter2ParameterSpecTest
 */


public class XPathFilter2ParameterSpecTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        java.util.List<javax.xml.crypto.dsig.spec.XPathType> l = new java.util.ArrayList<javax.xml.crypto.dsig.spec.XPathType>();
        l.add(new javax.xml.crypto.dsig.spec.XPathType("//x", javax.xml.crypto.dsig.spec.XPathType.Filter.INTERSECT));
        javax.xml.crypto.dsig.spec.XPathFilter2ParameterSpec s = new javax.xml.crypto.dsig.spec.XPathFilter2ParameterSpec(l);
        check(s.getXPathList().equals(l), "xpath list");
        check(s instanceof javax.xml.crypto.dsig.spec.TransformParameterSpec, "is TransformParameterSpec");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
