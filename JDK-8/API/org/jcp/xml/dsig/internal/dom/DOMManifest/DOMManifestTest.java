/*
 * @test
 * @summary org.jcp.xml.dsig.internal.dom.DOMManifest conformance: type shape via reflection (headless-safe).
 * @run main DOMManifestTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class DOMManifestTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.DOMManifest", false, DOMManifestTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("org.jcp.xml.dsig.internal.dom.DOMStructure"), "superclass org.jcp.xml.dsig.internal.dom.DOMStructure");
        check(hasInterface(c, "javax.xml.crypto.dsig.Manifest"), "implements javax.xml.crypto.dsig.Manifest");
        check(hasMethod(c, "equals"), "declares equals");
        check(hasMethod(c, "getId"), "declares getId");
        check(hasMethod(c, "getManifestReferences"), "declares getManifestReferences");
        check(hasMethod(c, "getReferences"), "declares getReferences");
        check(hasMethod(c, "hashCode"), "declares hashCode");
        check(hasMethod(c, "marshal"), "declares marshal");
        deepen();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ---- deepening: exact member face + behavior (contract lock) ----
    static void deepen() throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.DOMManifest");
        check("org.jcp.xml.dsig.internal.dom.DOMStructure".equals(c.getSuperclass().getName()), "superclass org.jcp.xml.dsig.internal.dom.DOMStructure");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList("javax.xml.crypto.dsig.Manifest"))), "exact interface set");
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
        check(names.equals(new java.util.HashSet<String>(java.util.Arrays.asList("equals", "getId", "getManifestReferences", "getReferences", "hashCode", "marshal"))),
                "exact public/protected method face (6 names): " + names);
        check(staticNames.equals(new java.util.HashSet<String>(java.util.Arrays.asList("getManifestReferences"))), "static member names locked");
        java.util.Set<String> ctorFace = new java.util.HashSet<String>();
        for (java.lang.reflect.Constructor<?> k : c.getDeclaredConstructors()) {
            if (java.lang.reflect.Modifier.isPrivate(k.getModifiers())) continue;
            StringBuilder sb = new StringBuilder();
            for (Class<?> t : k.getParameterTypes()) sb.append(t.getName()).append(",");
            ctorFace.add(sb.toString());
        }
        check(ctorFace.size() == 2, "exactly 2 declared constructors, got " + ctorFace.size());
        check(ctorFace.contains("java.util.List,java.lang.String,"), "constructor(List, String) present");
        check(ctorFace.contains("org.w3c.dom.Element,javax.xml.crypto.XMLCryptoContext,java.security.Provider,"), "constructor(Element, XMLCryptoContext, Provider) present");
        // black-box behavior
        javax.xml.crypto.dsig.XMLSignatureFactory xsf = javax.xml.crypto.dsig.XMLSignatureFactory.getInstance("DOM");
        javax.xml.crypto.dsig.DigestMethod dm = xsf.newDigestMethod(javax.xml.crypto.dsig.DigestMethod.SHA256, null);
        javax.xml.crypto.dsig.CanonicalizationMethod cm = xsf.newCanonicalizationMethod(javax.xml.crypto.dsig.CanonicalizationMethod.INCLUSIVE, (javax.xml.crypto.dsig.spec.C14NMethodParameterSpec) null);
        javax.xml.crypto.dsig.SignatureMethod sm = xsf.newSignatureMethod(javax.xml.crypto.dsig.SignatureMethod.RSA_SHA1, null);
        javax.xml.crypto.dsig.Reference ref = xsf.newReference("#o1", dm);
        try {
            new org.jcp.xml.dsig.internal.dom.DOMManifest(java.util.Collections.<javax.xml.crypto.dsig.Reference>emptyList(), "m1");
            check(false, "expected IAE for empty references");
        } catch (IllegalArgumentException expected) {
            check("list of references must contain at least one entry".equals(expected.getMessage()),
                    "empty references IAE message");
        }
        org.jcp.xml.dsig.internal.dom.DOMManifest mf =
                new org.jcp.xml.dsig.internal.dom.DOMManifest(java.util.Arrays.asList(ref), "m1");
        check("m1".equals(mf.getId()), "getId round trip");
        check(mf.getReferences().size() == 1, "one reference retained");
        check("#o1".equals(mf.getReferences().get(0).getURI()), "reference forwarded unchanged");
        try {
            mf.getReferences().add((javax.xml.crypto.dsig.Reference) null);
            check(false, "expected UOE modifying references");
        } catch (UnsupportedOperationException expected) {
            check(true, "references list is unmodifiable");
        }
        org.jcp.xml.dsig.internal.dom.DOMManifest mf2 =
                new org.jcp.xml.dsig.internal.dom.DOMManifest(java.util.Arrays.asList(ref), null);
        check(mf2.getId() == null, "null id stays null");
        check(mf instanceof javax.xml.crypto.dsig.Manifest, "DOMManifest is a Manifest");
    }
}
