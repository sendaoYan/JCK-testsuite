/*
 * @test
 * @summary org.jcp.xml.dsig.internal.dom.Policy conformance: internal package reflection.
 * @modules java.xml.crypto/org.jcp.xml.dsig.internal.dom:+open
 * @run main PolicyTest
 */
import java.lang.reflect.*;
import java.util.*;
public class PolicyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.Policy", false, PolicyTest.class.getClassLoader());
        check(c.getName().equals("org.jcp.xml.dsig.internal.dom.Policy"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("disabledAlgs"), "method:disabledAlgs");
        check(methods.contains("disabledReferenceUriSchemes"), "method:disabledReferenceUriSchemes");
        check(methods.contains("maxReferences"), "method:maxReferences");
        check(methods.contains("maxTransforms"), "method:maxTransforms");
        check(methods.contains("minKeySize"), "method:minKeySize");
        check(methods.contains("restrictAlg"), "method:restrictAlg");
        check(methods.contains("restrictDuplicateIds"), "method:restrictDuplicateIds");
        check(methods.contains("restrictKey"), "method:restrictKey");
        check(methods.contains("restrictNumReferences"), "method:restrictNumReferences");
        check(methods.contains("restrictNumTransforms"), "method:restrictNumTransforms");
        check(methods.contains("restrictReferenceUriScheme"), "method:restrictReferenceUriScheme");
        check(methods.contains("restrictRetrievalMethodLoops"), "method:restrictRetrievalMethodLoops");
        deepen();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ---- deepening: exact member face + behavior (contract lock) ----
    static void deepen() throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.Policy");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass java.lang.Object");
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
        check(names.equals(new java.util.HashSet<String>(java.util.Arrays.asList("disabledAlgs", "disabledReferenceUriSchemes", "maxReferences", "maxTransforms", "minKeySize", "restrictAlg", "restrictDuplicateIds", "restrictKey", "restrictNumReferences", "restrictNumTransforms", "restrictReferenceUriScheme", "restrictRetrievalMethodLoops"))),
                "exact public/protected method face (12 names): " + names);
        check(staticNames.equals(new java.util.HashSet<String>(java.util.Arrays.asList("disabledAlgs", "disabledReferenceUriSchemes", "maxReferences", "maxTransforms", "minKeySize", "restrictAlg", "restrictDuplicateIds", "restrictKey", "restrictNumReferences", "restrictNumTransforms", "restrictReferenceUriScheme", "restrictRetrievalMethodLoops"))), "static member names locked");
        java.util.Set<String> ctorFace = new java.util.HashSet<String>();
        for (java.lang.reflect.Constructor<?> k : c.getDeclaredConstructors()) {
            if (java.lang.reflect.Modifier.isPrivate(k.getModifiers())) continue;
            StringBuilder sb = new StringBuilder();
            for (Class<?> t : k.getParameterTypes()) sb.append(t.getName()).append(",");
            ctorFace.add(sb.toString());
        }
        check(ctorFace.size() == 0, "exactly 0 declared constructors, got " + ctorFace.size());
        // black-box behavior
        check(org.jcp.xml.dsig.internal.dom.Policy.maxTransforms() == 5, "default maxTransforms is 5");
        check(org.jcp.xml.dsig.internal.dom.Policy.maxReferences() == 30, "default maxReferences is 30");
        check(org.jcp.xml.dsig.internal.dom.Policy.minKeySize("RSA") == 1024, "default RSA min key size 1024");
        check(org.jcp.xml.dsig.internal.dom.Policy.minKeySize("BOGUS") == 0, "unknown algorithm maps to key size 0");
        check(org.jcp.xml.dsig.internal.dom.Policy.restrictNumTransforms(99), "99 transforms exceed the limit");
        check(!org.jcp.xml.dsig.internal.dom.Policy.restrictNumTransforms(1), "1 transform is within the limit");
        check(org.jcp.xml.dsig.internal.dom.Policy.restrictNumReferences(99), "99 references exceed the limit");
        check(org.jcp.xml.dsig.internal.dom.Policy.restrictDuplicateIds(), "duplicate ids restricted by default");
        check(org.jcp.xml.dsig.internal.dom.Policy.restrictRetrievalMethodLoops(), "retrieval method loops restricted by default");
        check(org.jcp.xml.dsig.internal.dom.Policy.restrictKey("RSA", 512), "512-bit RSA key below the limit");
        check(!org.jcp.xml.dsig.internal.dom.Policy.restrictKey("RSA", 2048), "2048-bit RSA key within the limit");
        check(org.jcp.xml.dsig.internal.dom.Policy.restrictAlg("http://www.w3.org/2001/04/xmldsig-more#md5"), "MD5 restricted");
        check(!org.jcp.xml.dsig.internal.dom.Policy.restrictAlg("http://www.w3.org/2001/04/xmlenc#sha256"), "SHA-256 not restricted");
        java.util.Set<java.net.URI> dis = org.jcp.xml.dsig.internal.dom.Policy.disabledAlgs();
        check(dis != null && !dis.isEmpty(), "disabled algorithm set non-empty");
        check(dis.contains(java.net.URI.create("http://www.w3.org/2001/04/xmldsig-more#md5")), "MD5 disabled");
        check(dis.contains(java.net.URI.create("http://www.w3.org/TR/1999/REC-xslt-19991116")), "XSLT disabled");
        check(dis.size() == 10, "disabled algorithm set size locked to 10");
        java.util.Set<String> schemes = org.jcp.xml.dsig.internal.dom.Policy.disabledReferenceUriSchemes();
        check(schemes.contains("file") && schemes.contains("http") && schemes.contains("https"),
                "file/http/https reference schemes disabled");
    }
}
