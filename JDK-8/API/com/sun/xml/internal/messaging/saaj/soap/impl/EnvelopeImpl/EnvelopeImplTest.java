/*
 * @test
 * @summary com.sun.xml.internal.messaging.saaj.soap.impl.EnvelopeImpl conformance: abstract class hierarchy and members.
 * @run main EnvelopeImplTest
 */

public class EnvelopeImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.messaging.saaj.soap.impl.EnvelopeImpl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.messaging.saaj.soap.impl.EnvelopeImpl"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check("com.sun.xml.internal.messaging.saaj.soap.impl.ElementImpl".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.messaging.saaj.soap.impl.ElementImpl");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "direct interface count");
        check(ifs.contains("com.sun.xml.internal.messaging.saaj.soap.Envelope"), "has interface com.sun.xml.internal.messaging.saaj.soap.Envelope");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("addBody"), "has method addBody");
        check(mn.contains("addElement"), "has method addElement");
        check(mn.contains("addHeader"), "has method addHeader");
        check(mn.contains("createName"), "has method createName");
        check(mn.contains("getBody"), "has method getBody");
        check(mn.contains("getBodyName"), "has method getBodyName");
        check(mn.contains("getContent"), "has method getContent");
        check(mn.contains("getHeader"), "has method getHeader");
        check(mn.contains("getHeaderName"), "has method getHeaderName");
        check(mn.contains("getOmitXmlDecl"), "has method getOmitXmlDecl");
        check(mn.contains("lookForBody"), "has method lookForBody");
        check(mn.contains("lookForHeader"), "has method lookForHeader");
        check(mn.contains("output"), "has method output");
        check(mn.contains("setCharsetEncoding"), "has method setCharsetEncoding");
        check(mn.contains("setElementQName"), "has method setElementQName");
        check(mn.contains("setOmitXmlDecl"), "has method setOmitXmlDecl");
        check(mn.contains("setXmlDecl"), "has method setXmlDecl");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
