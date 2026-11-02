/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.signature.XMLSignature conformance: class hierarchy and members.
 * @run main XMLSignatureTest
 */
public class XMLSignatureTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.signature.XMLSignature", false, XMLSignatureTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.security.utils.SignatureElementProxy"), "superclass com.sun.org.apache.xml.internal.security.utils.SignatureElementProxy");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("setId"), "method setId");
        check(mn.contains("getId"), "method getId");
        check(mn.contains("getSignedInfo"), "method getSignedInfo");
        check(mn.contains("getSignatureValue"), "method getSignatureValue");
        check(mn.contains("getKeyInfo"), "method getKeyInfo");
        check(mn.contains("appendObject"), "method appendObject");
        check(mn.contains("getObjectItem"), "method getObjectItem");
        check(mn.contains("getObjectLength"), "method getObjectLength");
        check(mn.contains("sign"), "method sign");
        check(mn.contains("addResourceResolver"), "method addResourceResolver");
        check(mn.contains("checkSignatureValue"), "method checkSignatureValue");
        check(mn.contains("addDocument"), "method addDocument");
        check(mn.contains("addKeyInfo"), "method addKeyInfo");
        check(mn.contains("createSecretKey"), "method createSecretKey");
        check(mn.contains("setFollowNestedManifests"), "method setFollowNestedManifests");
        check(mn.contains("getBaseLocalName"), "method getBaseLocalName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
