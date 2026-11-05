/*
 * @test
 * @summary org.jcp.xml.dsig.internal.dom.DOMXMLSignatureFactory conformance: type shape via reflection (headless-safe).
 * @run main DOMXMLSignatureFactoryTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class DOMXMLSignatureFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.DOMXMLSignatureFactory", false, DOMXMLSignatureFactoryTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.xml.crypto.dsig.XMLSignatureFactory"), "superclass javax.xml.crypto.dsig.XMLSignatureFactory");
        check(hasMethod(c, "getURIDereferencer"), "declares getURIDereferencer");
        check(hasMethod(c, "isFeatureSupported"), "declares isFeatureSupported");
        check(hasMethod(c, "newCanonicalizationMethod"), "declares newCanonicalizationMethod");
        check(hasMethod(c, "newDigestMethod"), "declares newDigestMethod");
        check(hasMethod(c, "newManifest"), "declares newManifest");
        check(hasMethod(c, "newReference"), "declares newReference");
        check(hasMethod(c, "newSignatureMethod"), "declares newSignatureMethod");
        check(hasMethod(c, "newSignatureProperties"), "declares newSignatureProperties");
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
