/*
 * @test
 * @summary javax.xml.crypto.dsig.spec.XSLTTransformParameterSpec conformance.
 * @run main XSLTTransformParameterSpecTest
 */

import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.spec.XSLTTransformParameterSpec;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;

public class XSLTTransformParameterSpecTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static Document newDoc(String xml) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        return dbf.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.xml.crypto.dsig.spec.XSLTTransformParameterSpec");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        boolean tp = false;
        for (String i : ifs) if (i.equals("javax.xml.crypto.dsig.spec.TransformParameterSpec")) tp = true;
        check(tp, "implements TransformParameterSpec");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getStylesheet"), "has getStylesheet");
    }

    static void testSemantics() throws Exception {
        Document d = newDoc("<r/>");
        Element ss = d.createElementNS("http://www.w3.org/1999/XSL/Transform", "stylesheet");
        ss.setAttribute("version", "1.0");
        DOMStructure wrapper = new DOMStructure(ss);
        XSLTTransformParameterSpec spec = new XSLTTransformParameterSpec(wrapper);
        check(spec.getStylesheet() == wrapper, "stylesheet identity round-trip");
        check(((DOMStructure) spec.getStylesheet()).getNode() == ss, "wrapped node identity");
        // null stylesheet rejected
        try { new XSLTTransformParameterSpec(null); check(false, "null stylesheet"); }
        catch (NullPointerException e) { check(true, "null stylesheet NPE"); }
        // usable to build an XSLT transform
        XMLSignatureFactory sf = XMLSignatureFactory.getInstance("DOM");
        javax.xml.crypto.dsig.Transform tr = sf.newTransform(javax.xml.crypto.dsig.Transform.XSLT, spec);
        check(tr.getAlgorithm().equals(javax.xml.crypto.dsig.Transform.XSLT), "XSLT transform algorithm");
        check(tr.getParameterSpec() == spec, "XSLT transform spec identity round-trip");
        // XSLT without params rejected
        try { sf.newTransform(javax.xml.crypto.dsig.Transform.XSLT, (javax.xml.crypto.dsig.spec.TransformParameterSpec) null); check(false, "XSLT requires spec"); }
        catch (java.security.InvalidAlgorithmParameterException e) { check(true, "XSLT requires spec IAE"); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
