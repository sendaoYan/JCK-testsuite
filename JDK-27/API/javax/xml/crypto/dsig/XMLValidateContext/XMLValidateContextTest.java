/*
 * @test
 * @summary javax.xml.crypto.dsig.XMLValidateContext conformance.
 * @run main XMLValidateContextTest
 */

public class XMLValidateContextTest {
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
        Class<?> c = Class.forName("javax.xml.crypto.dsig.XMLValidateContext");
        check(c.isInterface(), "is interface");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.crypto.XMLCryptoContext"), "extends javax.xml.crypto.XMLCryptoContext");
        check(c.getDeclaredMethods().length == 0, "no additional declared methods");
        check(javax.xml.crypto.dsig.dom.DOMValidateContext.class
            .getInterfaces()[0].getName().equals("javax.xml.crypto.dsig.XMLValidateContext"), "DOMValidateContext implements it");
        // a validate context drives unmarshal + validate end to end
        javax.xml.crypto.dsig.XMLSignatureFactory sf = javax.xml.crypto.dsig.XMLSignatureFactory.getInstance("DOM");
        org.w3c.dom.Document d = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        org.w3c.dom.Node root = d.createElement("root");
        d.appendChild(root);
        javax.crypto.SecretKey hmacKey = new javax.crypto.spec.SecretKeySpec(new byte[32], "HmacSHA256");
        javax.xml.crypto.dsig.SignedInfo si = sf.newSignedInfo(
            sf.newCanonicalizationMethod(javax.xml.crypto.dsig.CanonicalizationMethod.INCLUSIVE,
                (javax.xml.crypto.dsig.spec.C14NMethodParameterSpec) null),
            sf.newSignatureMethod("http://www.w3.org/2001/04/xmldsig-more#hmac-sha256", null),
            java.util.Collections.singletonList(sf.newReference("#o",
                sf.newDigestMethod(javax.xml.crypto.dsig.DigestMethod.SHA256, null))));
        sf.newXMLSignature(si, null,
            java.util.Collections.singletonList(sf.newXMLObject(null, "o", null, null)), null, null)
            .sign(new javax.xml.crypto.dsig.dom.DOMSignContext(hmacKey, root));
        org.w3c.dom.Element sigEl = (org.w3c.dom.Element)
            ((org.w3c.dom.Element) root).getElementsByTagNameNS(javax.xml.crypto.dsig.XMLSignature.XMLNS, "Signature").item(0);
        javax.xml.crypto.dsig.XMLValidateContext vc = new javax.xml.crypto.dsig.dom.DOMValidateContext(
            javax.xml.crypto.KeySelector.singletonKeySelector(hmacKey), sigEl);
        check(vc instanceof javax.xml.crypto.XMLCryptoContext, "validate context is an XMLCryptoContext");
        javax.xml.crypto.dsig.XMLSignature vs = sf.unmarshalXMLSignature(vc);
        check(vs.validate(vc), "validate(ctx) verifies the hmac signature");
        // property channel on the validate context
        vc.setProperty("javax.xml.crypto.dsig.cacheReference", Boolean.TRUE);
        check(Boolean.TRUE.equals(vc.getProperty("javax.xml.crypto.dsig.cacheReference")), "cacheReference property round-trip");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
