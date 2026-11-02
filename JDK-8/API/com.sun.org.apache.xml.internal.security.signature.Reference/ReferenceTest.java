/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.signature.Reference conformance: class hierarchy and members.
 * @run main ReferenceTest
 */
public class ReferenceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.signature.Reference", false, ReferenceTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.security.utils.SignatureElementProxy"), "superclass com.sun.org.apache.xml.internal.security.utils.SignatureElementProxy");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getMessageDigestAlgorithm"), "method getMessageDigestAlgorithm");
        check(mn.contains("setURI"), "method setURI");
        check(mn.contains("getURI"), "method getURI");
        check(mn.contains("setId"), "method setId");
        check(mn.contains("getId"), "method getId");
        check(mn.contains("setType"), "method setType");
        check(mn.contains("getType"), "method getType");
        check(mn.contains("typeIsReferenceToObject"), "method typeIsReferenceToObject");
        check(mn.contains("typeIsReferenceToManifest"), "method typeIsReferenceToManifest");
        check(mn.contains("generateDigestValue"), "method generateDigestValue");
        check(mn.contains("getContentsBeforeTransformation"), "method getContentsBeforeTransformation");
        check(mn.contains("getContentsAfterTransformation"), "method getContentsAfterTransformation");
        check(mn.contains("getNodesetBeforeFirstCanonicalization"), "method getNodesetBeforeFirstCanonicalization");
        check(mn.contains("getHTMLRepresentation"), "method getHTMLRepresentation");
        check(mn.contains("getTransformsOutput"), "method getTransformsOutput");
        check(mn.contains("getReferenceData"), "method getReferenceData");
        check(mn.contains("dereferenceURIandPerformTransforms"), "method dereferenceURIandPerformTransforms");
        check(mn.contains("getTransforms"), "method getTransforms");
        check(mn.contains("getReferencedBytes"), "method getReferencedBytes");
        check(mn.contains("getDigestValue"), "method getDigestValue");
        check(mn.contains("verify"), "method verify");
        check(mn.contains("getBaseLocalName"), "method getBaseLocalName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
