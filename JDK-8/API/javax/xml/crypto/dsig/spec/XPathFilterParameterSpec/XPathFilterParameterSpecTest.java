/*
 * @test
 * @summary javax.xml.crypto.dsig.spec.XPathFilterParameterSpec conformance: xpath filter parameter spec.
 * @run main XPathFilterParameterSpecTest
 */


public class XPathFilterParameterSpecTest {
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
        javax.xml.crypto.dsig.spec.XPathFilterParameterSpec s = new javax.xml.crypto.dsig.spec.XPathFilterParameterSpec("//x");
        check("//x".equals(s.getXPath()), "xpath");
        java.util.Map<String, String> ns = new java.util.HashMap<String, String>();
        ns.put("p", "urn:p");
        javax.xml.crypto.dsig.spec.XPathFilterParameterSpec s2 = new javax.xml.crypto.dsig.spec.XPathFilterParameterSpec("//p:x", ns);
        check(ns.equals(s2.getNamespaceMap()), "ns map");
        check(s2 instanceof javax.xml.crypto.dsig.spec.TransformParameterSpec, "is TransformParameterSpec");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
