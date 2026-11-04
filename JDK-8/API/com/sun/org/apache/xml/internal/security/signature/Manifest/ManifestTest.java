/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.signature.Manifest conformance: class hierarchy and members.
 * @run main ManifestTest
 */
public class ManifestTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.signature.Manifest", false, ManifestTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.security.utils.SignatureElementProxy"), "superclass com.sun.org.apache.xml.internal.security.utils.SignatureElementProxy");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("addDocument"), "method addDocument");
        check(mn.contains("generateDigestValues"), "method generateDigestValues");
        check(mn.contains("getLength"), "method getLength");
        check(mn.contains("item"), "method item");
        check(mn.contains("setId"), "method setId");
        check(mn.contains("getId"), "method getId");
        check(mn.contains("verifyReferences"), "method verifyReferences");
        check(mn.contains("getVerificationResult"), "method getVerificationResult");
        check(mn.contains("getVerificationResults"), "method getVerificationResults");
        check(mn.contains("addResourceResolver"), "method addResourceResolver");
        check(mn.contains("getPerManifestResolvers"), "method getPerManifestResolvers");
        check(mn.contains("getResolverProperties"), "method getResolverProperties");
        check(mn.contains("setResolverProperty"), "method setResolverProperty");
        check(mn.contains("getResolverProperty"), "method getResolverProperty");
        check(mn.contains("getSignedContentItem"), "method getSignedContentItem");
        check(mn.contains("getReferencedContentBeforeTransformsItem"), "method getReferencedContentBeforeTransformsItem");
        check(mn.contains("getReferencedContentAfterTransformsItem"), "method getReferencedContentAfterTransformsItem");
        check(mn.contains("getSignedContentLength"), "method getSignedContentLength");
        check(mn.contains("getBaseLocalName"), "method getBaseLocalName");
        check(mn.contains("isSecureValidation"), "method isSecureValidation");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
