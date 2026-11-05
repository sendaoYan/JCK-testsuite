/*
 * @test
 * @summary org.jcp.xml.dsig.internal.dom.XMLDSigRI conformance: internal package reflection.
 * @modules java.xml.crypto/org.jcp.xml.dsig.internal.dom:+open
 * @run main XMLDSigRITest
 */
import java.lang.reflect.*;
import java.util.*;
public class XMLDSigRITest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.XMLDSigRI", false, XMLDSigRITest.class.getClassLoader());
        check(c.getName().equals("org.jcp.xml.dsig.internal.dom.XMLDSigRI"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass().getName().equals("java.security.Provider"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        deepen();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ---- deepening: exact member face + behavior (contract lock) ----
    static void deepen() throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.XMLDSigRI");
        check("java.security.Provider".equals(c.getSuperclass().getName()), "superclass java.security.Provider");
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
        check(names.isEmpty(), "no public/protected declared methods");
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
        org.jcp.xml.dsig.internal.dom.XMLDSigRI ri = new org.jcp.xml.dsig.internal.dom.XMLDSigRI();
        check("XMLDSig".equals(ri.getName()), "provider name is XMLDSig");
        check(ri.getInfo() != null && ri.getInfo().startsWith("XMLDSig (DOM XMLSignatureFactory"),
                "provider info string describes the DOM services");
        int ts = 0, sigf = 0, kif = 0;
        for (java.security.Provider.Service s : ri.getServices()) {
            if ("TransformService".equals(s.getType())) ts++;
            else if ("XMLSignatureFactory".equals(s.getType())) { sigf++; check("DOM".equals(s.getAlgorithm()), "XMLSignatureFactory service algorithm DOM"); }
            else if ("KeyInfoFactory".equals(s.getType())) { kif++; check("DOM".equals(s.getAlgorithm()), "KeyInfoFactory service algorithm DOM"); }
        }
        check(ts == 11 && sigf == 1 && kif == 1, "13 services: 11 TransformService + one signature factory + one keyinfo factory");
        java.security.Provider sec = java.security.Security.getProvider("XMLDSig");
        check(sec != null && sec.getClass() == org.jcp.xml.dsig.internal.dom.XMLDSigRI.class,
                "Security.getProvider(XMLDSig) is exactly the XMLDSigRI class");
        int resolved = 0;
        String[] internal = {
            "org.jcp.xml.dsig.internal.DigesterOutputStream",
            "org.jcp.xml.dsig.internal.dom.AbstractDOMSignatureMethod",
            "org.jcp.xml.dsig.internal.dom.ApacheCanonicalizer",
            "org.jcp.xml.dsig.internal.dom.ApacheData",
            "org.jcp.xml.dsig.internal.dom.ApacheNodeSetData",
            "org.jcp.xml.dsig.internal.dom.ApacheOctetStreamData",
            "org.jcp.xml.dsig.internal.dom.ApacheTransform",
            "org.jcp.xml.dsig.internal.dom.DOMBase64Transform",
            "org.jcp.xml.dsig.internal.dom.DOMCanonicalizationMethod",
            "org.jcp.xml.dsig.internal.dom.DOMCanonicalXMLC14N11Method",
            "org.jcp.xml.dsig.internal.dom.DOMCanonicalXMLC14NMethod",
            "org.jcp.xml.dsig.internal.dom.DOMCryptoBinary",
            "org.jcp.xml.dsig.internal.dom.DOMDigestMethod",
            "org.jcp.xml.dsig.internal.dom.DOMEnvelopedTransform",
            "org.jcp.xml.dsig.internal.dom.DOMExcC14NMethod",
            "org.jcp.xml.dsig.internal.dom.DOMHMACSignatureMethod",
            "org.jcp.xml.dsig.internal.dom.DOMKeyInfo",
            "org.jcp.xml.dsig.internal.dom.DOMKeyInfoFactory",
            "org.jcp.xml.dsig.internal.dom.DOMKeyName",
            "org.jcp.xml.dsig.internal.dom.DOMKeyValue",
            "org.jcp.xml.dsig.internal.dom.DOMManifest",
            "org.jcp.xml.dsig.internal.dom.DOMPGPData",
            "org.jcp.xml.dsig.internal.dom.DOMReference",
            "org.jcp.xml.dsig.internal.dom.DOMRetrievalMethod",
            "org.jcp.xml.dsig.internal.dom.DOMRSAPSSSignatureMethod",
            "org.jcp.xml.dsig.internal.dom.DOMSignatureMethod",
            "org.jcp.xml.dsig.internal.dom.DOMSignatureProperties",
            "org.jcp.xml.dsig.internal.dom.DOMSignatureProperty",
            "org.jcp.xml.dsig.internal.dom.DOMSignedInfo",
            "org.jcp.xml.dsig.internal.dom.DOMStructure",
            "org.jcp.xml.dsig.internal.dom.DOMSubTreeData",
            "org.jcp.xml.dsig.internal.dom.DOMTransform",
            "org.jcp.xml.dsig.internal.dom.DOMURIDereferencer",
            "org.jcp.xml.dsig.internal.dom.DOMUtils",
            "org.jcp.xml.dsig.internal.dom.DOMX509Data",
            "org.jcp.xml.dsig.internal.dom.DOMX509IssuerSerial",
            "org.jcp.xml.dsig.internal.dom.DOMXMLObject",
            "org.jcp.xml.dsig.internal.dom.DOMXMLSignature",
            "org.jcp.xml.dsig.internal.dom.DOMXMLSignatureFactory",
            "org.jcp.xml.dsig.internal.dom.DOMXPathFilter2Transform",
            "org.jcp.xml.dsig.internal.dom.DOMXPathTransform",
            "org.jcp.xml.dsig.internal.dom.DOMXSLTTransform",
            "org.jcp.xml.dsig.internal.dom.Policy",
            "org.jcp.xml.dsig.internal.dom.Utils",
            "org.jcp.xml.dsig.internal.dom.XMLDSigRI",
            "org.jcp.xml.dsig.internal.MacOutputStream",
            "org.jcp.xml.dsig.internal.SignerOutputStream"
        };
        for (String cn : internal) { Class.forName(cn); resolved++; }
        check(resolved == 47, "all 47 internal org.jcp classes resolve");
        try {
            Class.forName("org.jcp.xml.dsig.internal.dom.DOMRSAPSSSignatureMethod");
            check(true, "RSASSA-PSS internal method present on this release (version difference vs JDK 8)");
        } catch (ClassNotFoundException unexpected) {
            check(false, "DOMRSAPSSSignatureMethod must resolve");
        }
    }
}
