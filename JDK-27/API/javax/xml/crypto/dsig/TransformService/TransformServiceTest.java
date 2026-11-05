/*
 * @test
 * @summary javax.xml.crypto.dsig.TransformService conformance.
 * @run main TransformServiceTest
 */

import javax.xml.crypto.KeySelector;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.TransformService;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.crypto.dom.DOMStructure;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

public class TransformServiceTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static KeyPair KP;
    static KeyPair rsaKP() throws Exception {
        if (KP == null) {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            KP = kpg.generateKeyPair();
        }
        return KP;
    }

    static Document newDoc(String xml) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        return dbf.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.xml.crypto.dsig.TransformService");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        for (String n : new String[] {"getInstance", "getProvider", "getAlgorithm", "getMechanismType",
                "init", "marshalParams", "transform"})
            check(names.contains(n), "has method " + n);
    }

    static void testInstanceMatrix() throws Exception {
        // every registered transform algorithm reachable via TransformService for DOM
        String[] algs = {CanonicalizationMethod.INCLUSIVE, CanonicalizationMethod.INCLUSIVE_WITH_COMMENTS,
            CanonicalizationMethod.EXCLUSIVE, CanonicalizationMethod.EXCLUSIVE_WITH_COMMENTS,
            javax.xml.crypto.dsig.Transform.BASE64, javax.xml.crypto.dsig.Transform.ENVELOPED,
            javax.xml.crypto.dsig.Transform.XPATH, javax.xml.crypto.dsig.Transform.XPATH2, javax.xml.crypto.dsig.Transform.XSLT};
        for (String alg : algs) {
            TransformService ts = TransformService.getInstance(alg, "DOM");
            check(ts != null, "instance for " + alg);
            check(ts.getAlgorithm().equals(alg), "getAlgorithm " + alg);
            check("DOM".equals(ts.getMechanismType()), "mechanism DOM for " + alg);
            check(ts.getProvider() != null, "provider for " + alg);
            check(ts.getProvider().getName().equals("XMLDSig"), "served by XMLDSig provider for " + alg);
        }
        // negatives: unknown algorithm / unknown mechanism
        try { TransformService.getInstance("http://example.com/ts-bogus", "DOM"); check(false, "unknown algorithm"); }
        catch (java.security.NoSuchAlgorithmException e) {
            check(e.getMessage().contains("http://example.com/ts-bogus"), "unknown algorithm message contains URI");
        }
        try { TransformService.getInstance(CanonicalizationMethod.EXCLUSIVE, "BogusMech"); check(false, "unknown mechanism"); }
        catch (java.security.NoSuchAlgorithmException e) {
            check(e.getMessage().contains("BogusMech"), "unknown mechanism message contains mechanism");
        }
    }

    static void testInitLifecycle() throws Exception {
        KeyPair kp = rsaKP();
        Document d = newDoc("<r/>");
        TransformService ts = TransformService.getInstance(javax.xml.crypto.dsig.Transform.ENVELOPED, "DOM");
        // null parent -> NPE
        try { ts.init((javax.xml.crypto.XMLStructure) null, new DOMValidateContext(KeySelector.singletonKeySelector(kp.getPublic()), d)); check(false, "null parent"); }
        catch (NullPointerException e) { check(true, "init(null) NPE"); }
        // init with a real Transform element parent (built by marshalling through the factory)
        XMLSignatureFactory sf = XMLSignatureFactory.getInstance("DOM");
        javax.xml.crypto.dsig.DigestMethod dm = sf.newDigestMethod(javax.xml.crypto.dsig.DigestMethod.SHA256, null);
        javax.xml.crypto.dsig.Reference ref = sf.newReference("", dm,
            java.util.Collections.singletonList(sf.newTransform(javax.xml.crypto.dsig.Transform.ENVELOPED, (TransformParameterSpec) null)),
            null, null);
        javax.xml.crypto.dsig.SignedInfo si = sf.newSignedInfo(
            sf.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (javax.xml.crypto.dsig.spec.C14NMethodParameterSpec) null),
            sf.newSignatureMethod("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256", null),
            java.util.Collections.singletonList(ref));
        sf.newXMLSignature(si, null).sign(new DOMSignContext(kp.getPrivate(), d.getDocumentElement()));
        Element sigEl = (Element) d.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature").item(0);
        Element trEl = (Element) sigEl.getElementsByTagNameNS(XMLSignature.XMLNS, "Transform").item(0);
        check(trEl.getAttribute("Algorithm").equals(javax.xml.crypto.dsig.Transform.ENVELOPED), "marshalled transform element algorithm");
        DOMValidateContext vc = new DOMValidateContext(KeySelector.singletonKeySelector(kp.getPublic()), sigEl);
        ts.init(new DOMStructure(trEl), vc);
        check(true, "init(transform parent, validate context) succeeds");
        // marshalParams on a parameterless transform is a no-op that must not throw
        ts.marshalParams(new DOMStructure(trEl), new DOMSignContext(kp.getPrivate(), d.getDocumentElement()));
        check(true, "marshalParams no-op for parameterless transform");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testInstanceMatrix();
        testInitLifecycle();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
