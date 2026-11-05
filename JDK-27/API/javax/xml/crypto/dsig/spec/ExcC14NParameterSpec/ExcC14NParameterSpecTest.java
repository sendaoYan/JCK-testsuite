/*
 * @test
 * @summary javax.xml.crypto.dsig.spec.ExcC14NParameterSpec conformance.
 * @run main ExcC14NParameterSpecTest
 */

import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.ExcC14NParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ExcC14NParameterSpecTest {
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

    static final String RSA_SHA256 = "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256";

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.xml.crypto.dsig.spec.ExcC14NParameterSpec");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        boolean ci = false;
        for (String i : ifs) if (i.equals("javax.xml.crypto.dsig.spec.C14NMethodParameterSpec")) ci = true;
        check(ci, "implements C14NMethodParameterSpec");
        // also usable as a TransformParameterSpec for the EXC_C14N transform
        ExcC14NParameterSpec s = new ExcC14NParameterSpec(Arrays.asList("a", "b"));
        check(s instanceof TransformParameterSpec, "usable as TransformParameterSpec");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getPrefixList"), "has getPrefixList");
    }

    static void testPrefixListSemantics() {
        ExcC14NParameterSpec s = new ExcC14NParameterSpec(Arrays.asList("p1", "p2", "p3"));
        check(s.getPrefixList().size() == 3, "prefix count");
        check(s.getPrefixList().equals(Arrays.asList("p1", "p2", "p3")), "prefix order preserved");
        // input list copied in
        List<String> src = new ArrayList<String>(Arrays.asList("x"));
        ExcC14NParameterSpec s2 = new ExcC14NParameterSpec(src);
        src.add("y");
        check(s2.getPrefixList().size() == 1, "source list copied in");
        // returned list immutable
        try { s2.getPrefixList().add("z"); check(false, "prefix list immutable"); }
        catch (UnsupportedOperationException e) { check(true, "prefix list immutable UOE"); }
        // empty list legal
        check(new ExcC14NParameterSpec(Collections.<String>emptyList()).getPrefixList().isEmpty(), "empty prefix list");
        // null rejected
        try { new ExcC14NParameterSpec(null); check(false, "null prefix list"); }
        catch (NullPointerException e) { check(e.getMessage() != null, "null prefix list NPE with message"); }
        // JDK-27 difference: a null prefix element is accepted and preserved (JDK-8 rejects with CCE)
        ExcC14NParameterSpec withNull = new ExcC14NParameterSpec(Arrays.asList("ok", null));
        check(withNull.getPrefixList().size() == 2, "null prefix element accepted, size preserved");
        check(withNull.getPrefixList().contains(null), "null prefix element preserved in list");
    }

    /** Exclusive c14n behavior: PrefixList influences the canonical octets, hence the digest. */
    static void testExclusiveDigestEffect() throws Exception {
        KeyPair kp = rsaKP();
        XMLSignatureFactory sf = XMLSignatureFactory.getInstance("DOM");
        byte[] d1 = digestWith(sf, null);
        byte[] d2 = digestWith(sf, new ExcC14NParameterSpec(Arrays.asList("declared")));
        check(!Arrays.equals(d1, d2), "PrefixList changes the exclusive-c14n digest");
        // end-to-end validation with an ExcC14NParameterSpec transform
        Document doc = newDoc("<r xmlns:declared=\"urn:d\" xmlns:other=\"urn:o\"><t Id=\"tt\">X</t></r>");
        Element t = (Element) doc.getElementsByTagName("t").item(0); t.setIdAttribute("Id", true);
        DigestMethod dm = sf.newDigestMethod(DigestMethod.SHA256, null);
        Reference ref = sf.newReference("#tt", dm,
            Collections.singletonList(sf.newTransform(CanonicalizationMethod.EXCLUSIVE,
                (TransformParameterSpec) new ExcC14NParameterSpec(Arrays.asList("declared")))), null, null);
        SignedInfo si = sf.newSignedInfo(
            sf.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null),
            sf.newSignatureMethod(RSA_SHA256, null), Collections.singletonList(ref));
        sf.newXMLSignature(si, null).sign(new DOMSignContext(kp.getPrivate(), doc.getDocumentElement()));
        Element sigEl = (Element) doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature").item(0);
        DOMValidateContext vc = new DOMValidateContext(javax.xml.crypto.KeySelector.singletonKeySelector(kp.getPublic()), sigEl);
        check(sf.unmarshalXMLSignature(vc).validate(vc), "exclusive c14n with PrefixList validates end-to-end");
        // transform spec type round-trips through unmarshalled signature
        javax.xml.crypto.dsig.Transform vt = (javax.xml.crypto.dsig.Transform)
            ((Reference) sf.unmarshalXMLSignature(vc).getSignedInfo().getReferences().get(0)).getTransforms().get(0);
        check(vt.getParameterSpec() instanceof ExcC14NParameterSpec, "unmarshalled transform carries ExcC14NParameterSpec");
        check(((ExcC14NParameterSpec) vt.getParameterSpec()).getPrefixList().equals(Arrays.asList("declared")),
            "unmarshalled prefix list round-trip");
    }

    static byte[] digestWith(XMLSignatureFactory sf, ExcC14NParameterSpec spec) throws Exception {
        KeyPair kp = rsaKP();
        Document d = newDoc("<r xmlns:declared=\"urn:d\" xmlns:other=\"urn:o\"><t Id=\"tt\">X</t></r>");
        Element t = (Element) d.getElementsByTagName("t").item(0); t.setIdAttribute("Id", true);
        DigestMethod dm = sf.newDigestMethod(DigestMethod.SHA256, null);
        Reference ref = sf.newReference("#tt", dm,
            Collections.singletonList(sf.newTransform(CanonicalizationMethod.EXCLUSIVE,
                spec == null ? (TransformParameterSpec) null : (TransformParameterSpec) spec)), null, null);
        SignedInfo si = sf.newSignedInfo(
            sf.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null),
            sf.newSignatureMethod(RSA_SHA256, null), Collections.singletonList(ref));
        XMLSignature s = sf.newXMLSignature(si, null);
        s.sign(new DOMSignContext(kp.getPrivate(), d.getDocumentElement()));
        return ((Reference) s.getSignedInfo().getReferences().get(0)).getDigestValue();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testPrefixListSemantics();
        testExclusiveDigestEffect();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
