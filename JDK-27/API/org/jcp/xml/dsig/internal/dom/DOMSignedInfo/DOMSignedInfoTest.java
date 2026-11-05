/*
 * @test
 * @summary org.jcp.xml.dsig.internal.dom.DOMSignedInfo conformance: internal package reflection.
 * @modules java.xml.crypto/org.jcp.xml.dsig.internal.dom:+open
 * @run main DOMSignedInfoTest
 */
import java.lang.reflect.*;
import java.util.*;
public class DOMSignedInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.DOMSignedInfo", false, DOMSignedInfoTest.class.getClassLoader());
        check(c.getName().equals("org.jcp.xml.dsig.internal.dom.DOMSignedInfo"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass().getName().equals("org.jcp.xml.dsig.internal.dom.DOMStructure"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("javax.xml.crypto.dsig.SignedInfo"), "iface:javax.xml.crypto.dsig.SignedInfo");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("canonicalize"), "method:canonicalize");
        check(methods.contains("equals"), "method:equals");
        check(methods.contains("getCanonicalizationMethod"), "method:getCanonicalizationMethod");
        check(methods.contains("getCanonicalizedData"), "method:getCanonicalizedData");
        check(methods.contains("getId"), "method:getId");
        check(methods.contains("getReferences"), "method:getReferences");
        check(methods.contains("getSignatureMethod"), "method:getSignatureMethod");
        check(methods.contains("getSignedInfoReferences"), "method:getSignedInfoReferences");
        check(methods.contains("hashCode"), "method:hashCode");
        check(methods.contains("marshal"), "method:marshal");
        deepen();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ---- deepening: exact member face + behavior (contract lock) ----
    static void deepen() throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.DOMSignedInfo");
        check("org.jcp.xml.dsig.internal.dom.DOMStructure".equals(c.getSuperclass().getName()), "superclass org.jcp.xml.dsig.internal.dom.DOMStructure");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList("javax.xml.crypto.dsig.SignedInfo"))), "exact interface set");
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
        check(names.equals(new java.util.HashSet<String>(java.util.Arrays.asList("canonicalize", "equals", "getCanonicalizationMethod", "getCanonicalizedData", "getId", "getReferences", "getSignatureMethod", "getSignedInfoReferences", "hashCode", "marshal"))),
                "exact public/protected method face (10 names): " + names);
        check(staticNames.equals(new java.util.HashSet<String>(java.util.Arrays.asList("getSignedInfoReferences"))), "static member names locked");
        java.util.Set<String> ctorFace = new java.util.HashSet<String>();
        for (java.lang.reflect.Constructor<?> k : c.getDeclaredConstructors()) {
            if (java.lang.reflect.Modifier.isPrivate(k.getModifiers())) continue;
            StringBuilder sb = new StringBuilder();
            for (Class<?> t : k.getParameterTypes()) sb.append(t.getName()).append(",");
            ctorFace.add(sb.toString());
        }
        check(ctorFace.size() == 3, "exactly 3 declared constructors, got " + ctorFace.size());
        check(ctorFace.contains("org.w3c.dom.Element,javax.xml.crypto.XMLCryptoContext,java.security.Provider,"), "constructor(Element, XMLCryptoContext, Provider) present");
        check(ctorFace.contains("javax.xml.crypto.dsig.CanonicalizationMethod,javax.xml.crypto.dsig.SignatureMethod,java.util.List,java.lang.String,"), "constructor(CanonicalizationMethod, SignatureMethod, List, String) present");
        check(ctorFace.contains("javax.xml.crypto.dsig.CanonicalizationMethod,javax.xml.crypto.dsig.SignatureMethod,java.util.List,"), "constructor(CanonicalizationMethod, SignatureMethod, List) present");
        // black-box behavior
        javax.xml.crypto.dsig.XMLSignatureFactory xsf = javax.xml.crypto.dsig.XMLSignatureFactory.getInstance("DOM");
        javax.xml.crypto.dsig.DigestMethod dm = xsf.newDigestMethod(javax.xml.crypto.dsig.DigestMethod.SHA256, null);
        javax.xml.crypto.dsig.CanonicalizationMethod cm = xsf.newCanonicalizationMethod(javax.xml.crypto.dsig.CanonicalizationMethod.INCLUSIVE, (javax.xml.crypto.dsig.spec.C14NMethodParameterSpec) null);
        javax.xml.crypto.dsig.SignatureMethod sm = xsf.newSignatureMethod(javax.xml.crypto.dsig.SignatureMethod.RSA_SHA1, null);
        javax.xml.crypto.dsig.Reference ref = xsf.newReference("#o1", dm);
        try {
            xsf.newSignedInfo(cm, sm, java.util.Collections.<javax.xml.crypto.dsig.Reference>emptyList());
            check(false, "expected IAE for empty references");
        } catch (IllegalArgumentException expected) {
            check("list of references must contain at least one entry".equals(expected.getMessage()),
                    "empty references IAE message");
        }
        javax.xml.crypto.dsig.SignedInfo si = xsf.newSignedInfo(cm, sm, java.util.Arrays.asList(ref), "si1");
        check(si instanceof org.jcp.xml.dsig.internal.dom.DOMSignedInfo, "factory produces DOMSignedInfo");
        check("si1".equals(si.getId()), "getId round trip");
        check(javax.xml.crypto.dsig.CanonicalizationMethod.INCLUSIVE.equals(si.getCanonicalizationMethod().getAlgorithm()),
                "canonicalization algorithm forwards");
        check(javax.xml.crypto.dsig.SignatureMethod.RSA_SHA1.equals(si.getSignatureMethod().getAlgorithm()),
                "signature algorithm forwards");
        check(si.getReferences().size() == 1, "one reference retained");
        try {
            si.getReferences().add((javax.xml.crypto.dsig.Reference) null);
            check(false, "expected UOE modifying references");
        } catch (UnsupportedOperationException expected) {
            check(true, "references list is unmodifiable");
        }
    }
}
