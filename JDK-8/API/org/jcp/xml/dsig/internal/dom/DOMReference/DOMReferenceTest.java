/*
 * @test
 * @summary org.jcp.xml.dsig.internal.dom.DOMReference conformance: type shape via reflection (headless-safe).
 * @run main DOMReferenceTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class DOMReferenceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.DOMReference", false, DOMReferenceTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("org.jcp.xml.dsig.internal.dom.DOMStructure"), "superclass org.jcp.xml.dsig.internal.dom.DOMStructure");
        check(hasInterface(c, "javax.xml.crypto.dsig.Reference"), "implements javax.xml.crypto.dsig.Reference");
        check(hasInterface(c, "javax.xml.crypto.dom.DOMURIReference"), "implements javax.xml.crypto.dom.DOMURIReference");
        check(hasMethod(c, "digest"), "declares digest");
        check(hasMethod(c, "equals"), "declares equals");
        check(hasMethod(c, "getCalculatedDigestValue"), "declares getCalculatedDigestValue");
        check(hasMethod(c, "getDereferencedData"), "declares getDereferencedData");
        check(hasMethod(c, "getDigestInputStream"), "declares getDigestInputStream");
        check(hasMethod(c, "getDigestMethod"), "declares getDigestMethod");
        check(hasMethod(c, "getDigestValue"), "declares getDigestValue");
        check(hasMethod(c, "getHere"), "declares getHere");
        check(hasField(c, "MAXIMUM_TRANSFORM_COUNT"), "declares field MAXIMUM_TRANSFORM_COUNT");
        deepen();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ---- deepening: exact member face + behavior (contract lock) ----
    static void deepen() throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.DOMReference");
        check("org.jcp.xml.dsig.internal.dom.DOMStructure".equals(c.getSuperclass().getName()), "superclass org.jcp.xml.dsig.internal.dom.DOMStructure");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList("javax.xml.crypto.dsig.Reference", "javax.xml.crypto.dom.DOMURIReference"))), "exact interface set");
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
        check(names.equals(new java.util.HashSet<String>(java.util.Arrays.asList("digest", "equals", "getCalculatedDigestValue", "getDereferencedData", "getDigestInputStream", "getDigestMethod", "getDigestValue", "getHere", "getId", "getTransforms", "getType", "getURI", "hashCode", "marshal", "validate"))),
                "exact public/protected method face (15 names): " + names);
        java.util.Set<String> ctorFace = new java.util.HashSet<String>();
        for (java.lang.reflect.Constructor<?> k : c.getDeclaredConstructors()) {
            if (java.lang.reflect.Modifier.isPrivate(k.getModifiers())) continue;
            StringBuilder sb = new StringBuilder();
            for (Class<?> t : k.getParameterTypes()) sb.append(t.getName()).append(",");
            ctorFace.add(sb.toString());
        }
        check(ctorFace.size() == 4, "exactly 4 declared constructors, got " + ctorFace.size());
        check(ctorFace.contains("org.w3c.dom.Element,javax.xml.crypto.XMLCryptoContext,java.security.Provider,"), "constructor(Element, XMLCryptoContext, Provider) present");
        check(ctorFace.contains("java.lang.String,java.lang.String,javax.xml.crypto.dsig.DigestMethod,java.util.List,javax.xml.crypto.Data,java.util.List,java.lang.String,[B,java.security.Provider,"), "constructor(String, String, DigestMethod, List, Data, List, String, byte[], Provider) present");
        check(ctorFace.contains("java.lang.String,java.lang.String,javax.xml.crypto.dsig.DigestMethod,java.util.List,javax.xml.crypto.Data,java.util.List,java.lang.String,java.security.Provider,"), "constructor(String, String, DigestMethod, List, Data, List, String, Provider) present");
        check(ctorFace.contains("java.lang.String,java.lang.String,javax.xml.crypto.dsig.DigestMethod,java.util.List,java.lang.String,java.security.Provider,"), "constructor(String, String, DigestMethod, List, String, Provider) present");
        // black-box behavior
        javax.xml.crypto.dsig.XMLSignatureFactory xsf = javax.xml.crypto.dsig.XMLSignatureFactory.getInstance("DOM");
        javax.xml.crypto.dsig.DigestMethod dm = xsf.newDigestMethod(javax.xml.crypto.dsig.DigestMethod.SHA256, null);
        javax.xml.crypto.dsig.CanonicalizationMethod cm = xsf.newCanonicalizationMethod(javax.xml.crypto.dsig.CanonicalizationMethod.INCLUSIVE, (javax.xml.crypto.dsig.spec.C14NMethodParameterSpec) null);
        javax.xml.crypto.dsig.SignatureMethod sm = xsf.newSignatureMethod(javax.xml.crypto.dsig.SignatureMethod.RSA_SHA1, null);
        javax.xml.crypto.dsig.Reference ref = xsf.newReference("#o1", dm);
        org.jcp.xml.dsig.internal.dom.DOMReference dr =
                (org.jcp.xml.dsig.internal.dom.DOMReference) xsf.newReference("#o1", dm);
        check("#o1".equals(dr.getURI()), "getURI round trip");
        check(javax.xml.crypto.dsig.DigestMethod.SHA256.equals(dr.getDigestMethod().getAlgorithm()),
                "digest algorithm forwards");
        check(dr.getType() == null, "type null by default");
        check(dr.getDigestValue() == null, "digest value null before calculation");
        check(dr.getTransforms().isEmpty(), "no transforms by default");
        check(dr instanceof javax.xml.crypto.dsig.Reference, "DOMReference is a Reference");
        org.jcp.xml.dsig.internal.dom.DOMReference dr2 =
                (org.jcp.xml.dsig.internal.dom.DOMReference) xsf.newReference("#o2", dm,
                        java.util.Collections.<javax.xml.crypto.dsig.Transform>emptyList(), "objType", null);
        check("objType".equals(dr2.getType()), "explicit type round trip");
        check(dr2.getTransforms().isEmpty(), "empty transform list round trip");
        check(dr != dr2, "distinct references are distinct objects");
    }
}
