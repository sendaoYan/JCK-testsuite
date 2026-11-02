/*
 * @test
 * @summary javax.xml.crypto.dsig.spec.XSLTTransformParameterSpec conformance: xslt transform parameter spec.
 * @run main XSLTTransformParameterSpecTest
 */


public class XSLTTransformParameterSpecTest {
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
        org.w3c.dom.Document doc = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        org.w3c.dom.Node node = doc.createElement("stylesheet");
        javax.xml.crypto.dom.DOMStructure ds = new javax.xml.crypto.dom.DOMStructure(node);
        javax.xml.crypto.dsig.spec.XSLTTransformParameterSpec s = new javax.xml.crypto.dsig.spec.XSLTTransformParameterSpec(ds);
        check(s.getStylesheet() == ds, "stylesheet");
        check(s instanceof javax.xml.crypto.dsig.spec.TransformParameterSpec, "is TransformParameterSpec");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
