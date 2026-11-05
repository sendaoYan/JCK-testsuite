/*
 * @test
 * @summary javax.xml.crypto.dsig.spec.TransformParameterSpec conformance: interface hierarchy and methods.
 * @run main TransformParameterSpecTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.ExcC14NParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.crypto.dsig.spec.XPathFilter2ParameterSpec;
import javax.xml.crypto.dsig.spec.XPathFilterParameterSpec;
import javax.xml.crypto.dsig.spec.XPathType;
import javax.xml.crypto.dsig.spec.XSLTTransformParameterSpec;
import javax.xml.crypto.dom.DOMStructure;
import java.util.Collections;


public class TransformParameterSpecTest {
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
        Class<?> c = Class.forName("javax.xml.crypto.dsig.spec.TransformParameterSpec");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(ifs.length == 1 && ifs[0].equals("java.security.spec.AlgorithmParameterSpec"),
            "extends java.security.spec.AlgorithmParameterSpec only");
        // pure marker
        check(c.getDeclaredMethods().length == 0, "marker interface, no declared methods");
        check(java.lang.reflect.Modifier.isInterface(c.getModifiers()), "interface modifier");
        // the three transform spec implementations + C14N sub-interface
        check(new XPathFilterParameterSpec("e") instanceof TransformParameterSpec, "XPathFilterParameterSpec implements it");
        check(new XPathFilter2ParameterSpec(Collections.singletonList(new XPathType("e", XPathType.Filter.UNION)))
            instanceof TransformParameterSpec, "XPathFilter2ParameterSpec implements it");
        org.w3c.dom.Document d = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        check(new XSLTTransformParameterSpec(new DOMStructure(d.createElement("s"))) instanceof TransformParameterSpec,
            "XSLTTransformParameterSpec implements it");
        check(new ExcC14NParameterSpec(Collections.singletonList("p")) instanceof TransformParameterSpec,
            "ExcC14NParameterSpec implements it transitively via C14NMethodParameterSpec");
        // accepted by the factory with identity round-trip
        XMLSignatureFactory sf = XMLSignatureFactory.getInstance("DOM");
        TransformParameterSpec xps = new XPathFilterParameterSpec("here()");
        check(sf.newTransform(Transform.XPATH, xps).getParameterSpec() == xps,
            "XPATH accepts TransformParameterSpec with identity round-trip");
        // anonymous implementation rejected by ENVELOPED with the precise message
        try {
            sf.newTransform(Transform.ENVELOPED, new TransformParameterSpec() { });
            check(false, "anonymous spec should be rejected for ENVELOPED");
        } catch (java.security.InvalidAlgorithmParameterException e) {
            check(e.getMessage() != null, "anonymous spec rejection message: " + e.getMessage());
        }
        // null params legal for ENVELOPED
        check(sf.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null).getParameterSpec() == null,
            "null params legal for ENVELOPED");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
