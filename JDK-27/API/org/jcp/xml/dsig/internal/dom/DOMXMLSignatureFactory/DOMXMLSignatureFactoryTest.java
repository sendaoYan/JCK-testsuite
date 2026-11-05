/*
 * @test
 * @summary org.jcp.xml.dsig.internal.dom.DOMXMLSignatureFactory conformance: internal package reflection.
 * @modules java.xml.crypto/org.jcp.xml.dsig.internal.dom:+open
 * @run main DOMXMLSignatureFactoryTest
 */
import java.lang.reflect.*;
import java.util.*;
public class DOMXMLSignatureFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.DOMXMLSignatureFactory", false, DOMXMLSignatureFactoryTest.class.getClassLoader());
        check(c.getName().equals("org.jcp.xml.dsig.internal.dom.DOMXMLSignatureFactory"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass().getName().equals("javax.xml.crypto.dsig.XMLSignatureFactory"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("getURIDereferencer"), "method:getURIDereferencer");
        check(methods.contains("isFeatureSupported"), "method:isFeatureSupported");
        check(methods.contains("newCanonicalizationMethod"), "method:newCanonicalizationMethod");
        check(methods.contains("newDigestMethod"), "method:newDigestMethod");
        check(methods.contains("newManifest"), "method:newManifest");
        check(methods.contains("newReference"), "method:newReference");
        check(methods.contains("newSignatureMethod"), "method:newSignatureMethod");
        check(methods.contains("newSignatureProperties"), "method:newSignatureProperties");
        check(methods.contains("newSignatureProperty"), "method:newSignatureProperty");
        check(methods.contains("newSignedInfo"), "method:newSignedInfo");
        check(methods.contains("newTransform"), "method:newTransform");
        check(methods.contains("newXMLObject"), "method:newXMLObject");
        check(methods.contains("newXMLSignature"), "method:newXMLSignature");
        check(methods.contains("unmarshalXMLSignature"), "method:unmarshalXMLSignature");
        deepen();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ---- deepening: exact member face + behavior (contract lock) ----
    static void deepen() throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.DOMXMLSignatureFactory");
        check("javax.xml.crypto.dsig.XMLSignatureFactory".equals(c.getSuperclass().getName()), "superclass javax.xml.crypto.dsig.XMLSignatureFactory");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.isEmpty(), "no directly declared interfaces");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()) && java.lang.reflect.Modifier.isFinal(c.getModifiers()) && !java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "modifier set [public final]");
        java.util.Set<String> names = new java.util.HashSet<String>();
        java.util.Set<String> staticNames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (java.lang.reflect.Modifier.isPublic(mod) || java.lang.reflect.Modifier.isProtected(mod)) {
                names.add(m.getName());
                if (java.lang.reflect.Modifier.isStatic(mod)) staticNames.add(m.getName());
            }
        }
        check(names.equals(new java.util.HashSet<String>(java.util.Arrays.asList("getURIDereferencer", "isFeatureSupported", "newCanonicalizationMethod", "newDigestMethod", "newManifest", "newReference", "newSignatureMethod", "newSignatureProperties", "newSignatureProperty", "newSignedInfo", "newTransform", "newXMLObject", "newXMLSignature", "unmarshalXMLSignature"))),
                "exact public/protected method face (14 names): " + names);
        java.util.Set<String> ctorFace = new java.util.HashSet<String>();
        for (java.lang.reflect.Constructor<?> k : c.getDeclaredConstructors()) {
            if (java.lang.reflect.Modifier.isPrivate(k.getModifiers())) continue;
            StringBuilder sb = new StringBuilder();
            for (Class<?> t : k.getParameterTypes()) sb.append(t.getName()).append(",");
            ctorFace.add(sb.toString());
        }
        check(ctorFace.size() == 1, "exactly 1 declared constructors, got " + ctorFace.size());
        check(ctorFace.contains(""), "constructor(no-arg) present");
        // black-box behavior
        javax.xml.crypto.dsig.XMLSignatureFactory xsf =
                javax.xml.crypto.dsig.XMLSignatureFactory.getInstance("DOM");
        check(xsf instanceof org.jcp.xml.dsig.internal.dom.DOMXMLSignatureFactory,
                "getInstance(DOM) returns the DOM implementation");
        check("DOM".equals(xsf.getMechanismType()), "mechanism type DOM");
        check("XMLDSig".equals(xsf.getProvider().getName()), "provider XMLDSig");
        org.jcp.xml.dsig.internal.dom.DOMXMLSignatureFactory direct =
                new org.jcp.xml.dsig.internal.dom.DOMXMLSignatureFactory();
        check(direct.getMechanismType() == null, "direct construction leaves the mechanism type unset (registration-driven)");
        javax.xml.crypto.dsig.DigestMethod dm = direct.newDigestMethod(javax.xml.crypto.dsig.DigestMethod.SHA256, null);
        check(javax.xml.crypto.dsig.DigestMethod.SHA256.equals(dm.getAlgorithm()), "digest method closure");
        javax.xml.crypto.dsig.Reference r = direct.newReference("#x", dm);
        check("#x".equals(r.getURI()), "reference closure");
        check(xsf.getURIDereferencer() != null, "URI dereferencer available");
        check(xsf.getURIDereferencer() instanceof org.jcp.xml.dsig.internal.dom.DOMURIDereferencer,
                "URI dereferencer is the DOM implementation");
    }
}
