/*
 * @test
 * @summary javax.xml.crypto.dsig.spec.RSAPSSParameterSpec conformance: white-box structure + black-box behavior.
 * @run main RSAPSSParameterSpecTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;
import javax.xml.crypto.KeySelector;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.HMACParameterSpec;
import javax.xml.crypto.dsig.spec.RSAPSSParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilderFactory;

public class RSAPSSParameterSpecTest {
    static int passed;
    static void check(boolean c, String m) {
        if (!c) throw new AssertionError(m);
        passed++;
    }
    static boolean hasMethod(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getMethods()) {
            if (m.getName().equals(n)) return true;
        }
        return false;
    }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) {
            if (i.getName().equals(n)) return true;
        }
        return false;
    }

    static final String RSA_PSS = "http://www.w3.org/2007/05/xmldsig-more#rsa-pss";
    static final String SHA256_RSA_MGF1 = "http://www.w3.org/2007/05/xmldsig-more#sha256-rsa-MGF1";

    static KeyPair KP;
    static KeyPair rsaKP() throws Exception {
        if (KP == null) {
            KeyPairGenerator g = KeyPairGenerator.getInstance("RSA");
            g.initialize(2048);
            KP = g.generateKeyPair();
        }
        return KP;
    }

    static Document newDoc(String xml) throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        return f.newDocumentBuilder().parse(new java.io.ByteArrayInputStream(xml.getBytes("UTF-8")));
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        Class<?> c = Class.forName("javax.xml.crypto.dsig.spec.RSAPSSParameterSpec");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "is final class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasInterface(c, "javax.xml.crypto.dsig.spec.SignatureMethodParameterSpec"), "interface javax.xml.crypto.dsig.spec.SignatureMethodParameterSpec");
        check(hasMethod(c, "getPSSParameterSpec"), "method getPSSParameterSpec");

        java.security.spec.PSSParameterSpec pss = new java.security.spec.PSSParameterSpec("SHA-256", "MGF1",
            new java.security.spec.MGF1ParameterSpec("SHA-256"), 32, 1);
        javax.xml.crypto.dsig.spec.RSAPSSParameterSpec s = new javax.xml.crypto.dsig.spec.RSAPSSParameterSpec(pss);
        check(s.getPSSParameterSpec() == pss, "getPSSParameterSpec");

    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        java.security.spec.PSSParameterSpec inner = new java.security.spec.PSSParameterSpec("SHA-256", "MGF1",
            new java.security.spec.MGF1ParameterSpec("SHA-256"), 32, 1);
        javax.xml.crypto.dsig.spec.RSAPSSParameterSpec p = new javax.xml.crypto.dsig.spec.RSAPSSParameterSpec(inner);
        check(p.getPSSParameterSpec() == inner, "getPSSParameterSpec round-trip");
        check("SHA-256".equals(p.getPSSParameterSpec().getDigestAlgorithm()), "inner digest alg");
        check("MGF1".equals(p.getPSSParameterSpec().getDigestAlgorithm() == null ? "" : p.getPSSParameterSpec().getMGFAlgorithm()), "inner mgf alg");
        check(p.getPSSParameterSpec().getSaltLength() == 32, "inner salt length");
        check(p.getPSSParameterSpec().getTrailerField() == 1, "inner trailer field");
        check(p instanceof javax.xml.crypto.dsig.spec.SignatureMethodParameterSpec, "assignable to SignatureMethodParameterSpec");
        // alternate parameter content
        PSSParameterSpec sha512 = new PSSParameterSpec("SHA-512", "MGF1",
            new MGF1ParameterSpec("SHA-512"), 64, java.security.spec.PSSParameterSpec.TRAILER_FIELD_BC);
        RSAPSSParameterSpec p2 = new RSAPSSParameterSpec(sha512);
        check(p2.getPSSParameterSpec().getSaltLength() == 64, "SHA-512 salt length 64");
        check(p2.getPSSParameterSpec().getTrailerField() == java.security.spec.PSSParameterSpec.TRAILER_FIELD_BC, "trailer field BC");
        // null rejected
        try { new RSAPSSParameterSpec(null); check(false, "null ctor should throw"); }
        catch (NullPointerException npe) { check(true, "null ctor NPE"); }
    }

    // ---- Part 3: factory and signature integration ----
    static void testFactoryIntegration() throws Exception {
        XMLSignatureFactory sf = XMLSignatureFactory.getInstance("DOM");
        PSSParameterSpec inner = new PSSParameterSpec("SHA-256", "MGF1", new MGF1ParameterSpec("SHA-256"), 32, 1);
        RSAPSSParameterSpec spec = new RSAPSSParameterSpec(inner);
        // rsa-pss accepts the spec with identity round-trip
        SignatureMethod sm = sf.newSignatureMethod(RSA_PSS, spec);
        check(sm.getAlgorithm().equals(RSA_PSS), "rsa-pss algorithm URI");
        check(sm.getParameterSpec() == spec, "signature method preserves spec identity");
        // rsa-pss without params gets a default RSAPSSParameterSpec
        SignatureMethod smDefault = sf.newSignatureMethod(RSA_PSS, null);
        check(smDefault.getParameterSpec() instanceof RSAPSSParameterSpec, "default params are RSAPSSParameterSpec");
        check(((RSAPSSParameterSpec) smDefault.getParameterSpec()).getPSSParameterSpec() != null,
            "default params wrap a non-null PSSParameterSpec");
        // wrong spec type rejected with the precise message
        try { sf.newSignatureMethod(RSA_PSS, new HMACParameterSpec(256)); check(false, "wrong spec type should throw"); }
        catch (java.security.InvalidAlgorithmParameterException iae) {
            check(iae.getMessage().contains("RSAPSSParameterSpec"), "wrong spec type message names required type");
        }
    }

    // ---- Part 4: real rsa-pss sign -> validate -> tamper closed loop ----
    static void testSignValidateTamperLoop() throws Exception {
        KeyPair kp = rsaKP();
        XMLSignatureFactory sf = XMLSignatureFactory.getInstance("DOM");
        // explicit PSS parameters
        Document d = newDoc("<r>PSS</r>");
        Reference ref = sf.newReference("", sf.newDigestMethod(DigestMethod.SHA256, null),
            Collections.singletonList(sf.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null)), null, null);
        SignedInfo si = sf.newSignedInfo(
            sf.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null),
            sf.newSignatureMethod(RSA_PSS, new RSAPSSParameterSpec(
                new PSSParameterSpec("SHA-256", "MGF1", new MGF1ParameterSpec("SHA-256"), 32, 1))),
            Collections.singletonList(ref));
        sf.newXMLSignature(si, null).sign(new DOMSignContext(kp.getPrivate(), d.getDocumentElement()));
        Element sigEl = (Element) d.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature").item(0);
        DOMValidateContext vc = new DOMValidateContext(KeySelector.singletonKeySelector(kp.getPublic()), sigEl);
        check(sf.unmarshalXMLSignature(vc).validate(vc), "rsa-pss with explicit spec validates");
        // tamper the content: signature must stop validating
        d.getDocumentElement().getFirstChild().setNodeValue("TAMPERED");
        DOMValidateContext vc2 = new DOMValidateContext(KeySelector.singletonKeySelector(kp.getPublic()), sigEl);
        check(!sf.unmarshalXMLSignature(vc2).validate(vc2), "tampered content invalidates rsa-pss signature");
        // unmarshalled signature method exposes the PSS spec
        Document d3 = newDoc("<r>PSS</r>");
        Reference ref3 = sf.newReference("", sf.newDigestMethod(DigestMethod.SHA256, null),
            Collections.singletonList(sf.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null)), null, null);
        SignedInfo si3 = sf.newSignedInfo(
            sf.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null),
            sf.newSignatureMethod(RSA_PSS, new RSAPSSParameterSpec(
                new PSSParameterSpec("SHA-256", "MGF1", new MGF1ParameterSpec("SHA-256"), 32, 1))),
            Collections.singletonList(ref3));
        sf.newXMLSignature(si3, null).sign(new DOMSignContext(kp.getPrivate(), d3.getDocumentElement()));
        Element sigEl3 = (Element) d3.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature").item(0);
        DOMValidateContext vc4 = new DOMValidateContext(KeySelector.singletonKeySelector(kp.getPublic()), sigEl3);
        javax.xml.crypto.dsig.XMLSignature un = sf.unmarshalXMLSignature(vc4);
        SignatureMethod um = (SignatureMethod) un.getSignedInfo().getSignatureMethod();
        check(um.getAlgorithm().equals(RSA_PSS), "unmarshalled algorithm URI round-trip");
        check(um.getParameterSpec() instanceof RSAPSSParameterSpec, "unmarshalled spec type");
        check(((RSAPSSParameterSpec) um.getParameterSpec()).getPSSParameterSpec().getDigestAlgorithm().equals("SHA-256"),
            "unmarshalled inner digest round-trip");
        check(((RSAPSSParameterSpec) um.getParameterSpec()).getPSSParameterSpec().getSaltLength() == 32,
            "unmarshalled salt length round-trip");
        // implicit PSS family member sha256-rsa-MGF1
        Document d5 = newDoc("<r>MGF1</r>");
        Reference ref5 = sf.newReference("", sf.newDigestMethod(DigestMethod.SHA256, null),
            Collections.singletonList(sf.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null)), null, null);
        SignedInfo si5 = sf.newSignedInfo(
            sf.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null),
            sf.newSignatureMethod(SHA256_RSA_MGF1, null), Collections.singletonList(ref5));
        sf.newXMLSignature(si5, null).sign(new DOMSignContext(kp.getPrivate(), d5.getDocumentElement()));
        Element sigEl5 = (Element) d5.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature").item(0);
        DOMValidateContext vc5 = new DOMValidateContext(KeySelector.singletonKeySelector(kp.getPublic()), sigEl5);
        check(sf.unmarshalXMLSignature(vc5).validate(vc5), "sha256-rsa-MGF1 round-trip validates");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testFactoryIntegration();
        testSignValidateTamperLoop();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
