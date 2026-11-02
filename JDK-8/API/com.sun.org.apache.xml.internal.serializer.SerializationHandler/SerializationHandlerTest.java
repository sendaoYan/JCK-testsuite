/*
 * @test
 * @summary com.sun.org.apache.xml.internal.serializer.SerializationHandler conformance: interface hierarchy and members.
 * @run main SerializationHandlerTest
 */
public class SerializationHandlerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.serializer.SerializationHandler", false, SerializationHandlerTest.class.getClassLoader());
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(hasInterface(c, "com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler"), "has interface com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler");
        check(hasInterface(c, "com.sun.org.apache.xml.internal.serializer.ExtendedLexicalHandler"), "has interface com.sun.org.apache.xml.internal.serializer.ExtendedLexicalHandler");
        check(hasInterface(c, "com.sun.org.apache.xml.internal.serializer.XSLOutputAttributes"), "has interface com.sun.org.apache.xml.internal.serializer.XSLOutputAttributes");
        check(hasInterface(c, "org.xml.sax.ext.DeclHandler"), "has interface org.xml.sax.ext.DeclHandler");
        check(hasInterface(c, "org.xml.sax.DTDHandler"), "has interface org.xml.sax.DTDHandler");
        check(hasInterface(c, "org.xml.sax.ErrorHandler"), "has interface org.xml.sax.ErrorHandler");
        check(hasInterface(c, "com.sun.org.apache.xml.internal.serializer.DOMSerializer"), "has interface com.sun.org.apache.xml.internal.serializer.DOMSerializer");
        check(hasInterface(c, "com.sun.org.apache.xml.internal.serializer.Serializer"), "has interface com.sun.org.apache.xml.internal.serializer.Serializer");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("setContentHandler"), "method setContentHandler");
        check(mn.contains("close"), "method close");
        check(mn.contains("serialize"), "method serialize");
        check(mn.contains("setEscaping"), "method setEscaping");
        check(mn.contains("setIndentAmount"), "method setIndentAmount");
        check(mn.contains("setTransformer"), "method setTransformer");
        check(mn.contains("getTransformer"), "method getTransformer");
        check(mn.contains("setNamespaceMappings"), "method setNamespaceMappings");
        check(mn.contains("flushPending"), "method flushPending");
        check(mn.contains("setDTDEntityExpansion"), "method setDTDEntityExpansion");
        check(mn.contains("setIsStandalone"), "method setIsStandalone");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
