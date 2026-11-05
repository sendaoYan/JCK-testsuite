/*
 * @test
 * @summary org.jcp.xml.dsig.internal.dom.Policy conformance: type shape via reflection (headless-safe).
 * @run main PolicyTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class PolicyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.Policy", false, PolicyTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        check(hasMethod(c, "disabledAlgs"), "declares disabledAlgs");
        check(hasMethod(c, "disabledReferenceUriSchemes"), "declares disabledReferenceUriSchemes");
        check(hasMethod(c, "maxReferences"), "declares maxReferences");
        check(hasMethod(c, "maxTransforms"), "declares maxTransforms");
        check(hasMethod(c, "minKeySize"), "declares minKeySize");
        check(hasMethod(c, "restrictAlg"), "declares restrictAlg");
        check(hasMethod(c, "restrictDuplicateIds"), "declares restrictDuplicateIds");
        check(hasMethod(c, "restrictKey"), "declares restrictKey");
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
        check(dis.size() == 4, "disabled algorithm set size locked to 4");
        java.util.Set<String> schemes = org.jcp.xml.dsig.internal.dom.Policy.disabledReferenceUriSchemes();
        check(schemes.contains("file") && schemes.contains("http") && schemes.contains("https"),
                "file/http/https reference schemes disabled");
    }
}
