/*
 * @test
 * @summary com.sun.xml.internal.messaging.saaj.soap.ver1_2.Envelope1_2Impl conformance: class hierarchy and members.
 * @run main Envelope1_2ImplTest
 */

public class Envelope1_2ImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.messaging.saaj.soap.ver1_2.Envelope1_2Impl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.messaging.saaj.soap.ver1_2.Envelope1_2Impl"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("com.sun.xml.internal.messaging.saaj.soap.impl.EnvelopeImpl".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.messaging.saaj.soap.impl.EnvelopeImpl");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("addAttribute"), "has method addAttribute");
        check(mn.contains("addChildElement"), "has method addChildElement");
        check(mn.contains("addTextNode"), "has method addTextNode");
        check(mn.contains("getBodyName"), "has method getBodyName");
        check(mn.contains("getHeaderName"), "has method getHeaderName");
        check(mn.contains("setEncodingStyle"), "has method setEncodingStyle");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
