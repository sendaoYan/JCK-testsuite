/*
 * @test
 * @summary com.sun.xml.internal.messaging.saaj.soap.ver1_1.Message1_1Impl conformance: class hierarchy and members.
 * @run main Message1_1ImplTest
 */

public class Message1_1ImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.messaging.saaj.soap.ver1_1.Message1_1Impl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.messaging.saaj.soap.ver1_1.Message1_1Impl"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("com.sun.xml.internal.messaging.saaj.soap.MessageImpl".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.messaging.saaj.soap.MessageImpl");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "direct interface count");
        check(ifs.contains("javax.xml.soap.SOAPConstants"), "has interface javax.xml.soap.SOAPConstants");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getAction"), "has method getAction");
        check(mn.contains("getCharset"), "has method getCharset");
        check(mn.contains("getExpectedAcceptHeader"), "has method getExpectedAcceptHeader");
        check(mn.contains("getExpectedContentType"), "has method getExpectedContentType");
        check(mn.contains("getSOAPPart"), "has method getSOAPPart");
        check(mn.contains("isCorrectSoapVersion"), "has method isCorrectSoapVersion");
        check(mn.contains("setAction"), "has method setAction");
        check(mn.contains("setCharset"), "has method setCharset");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
