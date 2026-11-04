/*
 * @test
 * @summary javax.xml.crypto.dsig.XMLSignatureFactory conformance: white-box structure + black-box behavior.
 * @run main XMLSignatureFactoryTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class XMLSignatureFactoryTest {
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
    static boolean hasField(Class<?> c, String n) {
        try { c.getField(n); return true; } catch (NoSuchFieldException e) { return false; }
    }


        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        Class<?> c = Class.forName("javax.xml.crypto.dsig.XMLSignatureFactory");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasMethod(c, "getInstance"), "method getInstance");
        check(hasMethod(c, "getKeyInfoFactory"), "method getKeyInfoFactory");
        check(hasMethod(c, "getMechanismType"), "method getMechanismType");
        check(hasMethod(c, "getProvider"), "method getProvider");
        check(hasMethod(c, "getURIDereferencer"), "method getURIDereferencer");
        check(hasMethod(c, "isFeatureSupported"), "method isFeatureSupported");
        check(hasMethod(c, "newCanonicalizationMethod"), "method newCanonicalizationMethod");
        check(hasMethod(c, "newDigestMethod"), "method newDigestMethod");
        check(hasMethod(c, "newManifest"), "method newManifest");
        check(hasMethod(c, "newReference"), "method newReference");
        check(hasMethod(c, "newSignatureMethod"), "method newSignatureMethod");
        check(hasMethod(c, "newSignatureProperties"), "method newSignatureProperties");
        check(hasMethod(c, "newSignatureProperty"), "method newSignatureProperty");
        check(hasMethod(c, "newSignedInfo"), "method newSignedInfo");
        check(hasMethod(c, "newTransform"), "method newTransform");
        check(hasMethod(c, "newXMLObject"), "method newXMLObject");
        check(hasMethod(c, "newXMLSignature"), "method newXMLSignature");
        check(hasMethod(c, "unmarshalXMLSignature"), "method unmarshalXMLSignature");

        javax.xml.crypto.dsig.XMLSignatureFactory f = javax.xml.crypto.dsig.XMLSignatureFactory.getInstance("DOM");
        check("DOM".equals(f.getMechanismType()), "mechanism");
        check(f.getProvider() != null, "provider");
        check(f.getKeyInfoFactory() != null, "keyInfoFactory");
        javax.xml.crypto.dsig.DigestMethod dm = f.newDigestMethod(javax.xml.crypto.dsig.DigestMethod.SHA256, null);
        check(javax.xml.crypto.dsig.DigestMethod.SHA256.equals(dm.getAlgorithm()), "digestMethod");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        javax.xml.crypto.dsig.XMLSignatureFactory xf = javax.xml.crypto.dsig.XMLSignatureFactory.getInstance();
        check(xf != null, "getInstance");
        check("DOM".equals(xf.getMechanismType()), "mechanismType DOM");
        check(xf.getProvider() != null, "provider non-null");
        javax.xml.crypto.dsig.keyinfo.KeyInfoFactory kf = xf.getKeyInfoFactory();
        check(kf != null && "DOM".equals(kf.getMechanismType()), "getKeyInfoFactory");
        javax.xml.crypto.dsig.DigestMethod dm = xf.newDigestMethod(javax.xml.crypto.dsig.DigestMethod.SHA256, null);
        check(javax.xml.crypto.dsig.DigestMethod.SHA256.equals(dm.getAlgorithm()), "newDigestMethod");
        javax.xml.crypto.dsig.Transform tr = xf.newTransform(javax.xml.crypto.dsig.Transform.BASE64, (javax.xml.crypto.dsig.spec.TransformParameterSpec) null);
        check(javax.xml.crypto.dsig.Transform.BASE64.equals(tr.getAlgorithm()), "newTransform");
        javax.xml.crypto.dsig.CanonicalizationMethod cm = xf.newCanonicalizationMethod(
            javax.xml.crypto.dsig.CanonicalizationMethod.INCLUSIVE, (javax.xml.crypto.dsig.spec.C14NMethodParameterSpec) null);
        check(javax.xml.crypto.dsig.CanonicalizationMethod.INCLUSIVE.equals(cm.getAlgorithm()), "newCanonicalizationMethod");
        javax.xml.crypto.dsig.SignatureMethod sm = xf.newSignatureMethod(javax.xml.crypto.dsig.SignatureMethod.RSA_SHA256, null);
        check(javax.xml.crypto.dsig.SignatureMethod.RSA_SHA256.equals(sm.getAlgorithm()), "newSignatureMethod");
        javax.xml.crypto.dsig.Reference ref = xf.newReference("#x", dm);
        check("#x".equals(ref.getURI()) && ref.getDigestMethod() == dm, "newReference");
        java.util.List<javax.xml.crypto.dsig.Reference> refs = new java.util.ArrayList<>();
        refs.add(ref);
        javax.xml.crypto.dsig.SignedInfo si = xf.newSignedInfo(cm, sm, refs);
        check(si.getCanonicalizationMethod() == cm && si.getSignatureMethod() == sm
              && si.getReferences().size() == 1, "newSignedInfo");
        javax.xml.crypto.dsig.keyinfo.KeyName kn = kf.newKeyName("kn");
        javax.xml.crypto.dsig.keyinfo.KeyInfo ki = kf.newKeyInfo(java.util.Collections.singletonList((javax.xml.crypto.XMLStructure) kn));
        javax.xml.crypto.dsig.XMLSignature sig = xf.newXMLSignature(si, ki);
        check(sig.getSignedInfo() == si && sig.getKeyInfo() == ki, "newXMLSignature");
        org.w3c.dom.Document doc = javax.xml.parsers.DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();
        org.w3c.dom.Element el = doc.createElement("a");
        javax.xml.crypto.dom.DOMStructure ds = new javax.xml.crypto.dom.DOMStructure(el);
        java.util.List<javax.xml.crypto.XMLStructure> content = new java.util.ArrayList<>();
        content.add(ds);
        javax.xml.crypto.dsig.XMLObject obj = xf.newXMLObject(content, "oid", "text/plain", "UTF-8");
        check("oid".equals(obj.getId()) && "text/plain".equals(obj.getMimeType())
              && "UTF-8".equals(obj.getEncoding()) && obj.getContent().size() == 1, "newXMLObject");
        javax.xml.crypto.dsig.Manifest mf = xf.newManifest(refs, "mid");
        check("mid".equals(mf.getId()) && mf.getReferences().size() == 1, "newManifest");
        javax.xml.crypto.dsig.SignatureProperty sp = xf.newSignatureProperty(content, "target", "pid");
        check("target".equals(sp.getTarget()) && "pid".equals(sp.getId()), "newSignatureProperty");
        java.util.List<javax.xml.crypto.dsig.SignatureProperty> sps = new java.util.ArrayList<>();
        sps.add(sp);
        javax.xml.crypto.dsig.SignatureProperties spp = xf.newSignatureProperties(sps, "sppid");
        check("sppid".equals(spp.getId()) && spp.getProperties().size() == 1, "newSignatureProperties");
        check(!xf.isFeatureSupported("x"), "isFeatureSupported false");
        check(xf.getURIDereferencer() != null, "default URIDereferencer non-null");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
