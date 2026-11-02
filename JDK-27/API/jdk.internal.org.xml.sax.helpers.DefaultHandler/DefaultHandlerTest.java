/*
 * @test
 * @summary jdk.internal.org.xml.sax.helpers.DefaultHandler conformance: internal class reflection.
 * @modules java.base/jdk.internal.org.xml.sax.helpers:+open
 * @run main DefaultHandlerTest
 */
public class DefaultHandlerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.org.xml.sax.helpers.DefaultHandler", false, DefaultHandlerTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.org.xml.sax.helpers.DefaultHandler"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length >= 4, "direct interfaces >= 4");
        check(hasI(c.getInterfaces(), "jdk.internal.org.xml.sax.ContentHandler"), "interface jdk.internal.org.xml.sax.ContentHandler");
        check(hasI(c.getInterfaces(), "jdk.internal.org.xml.sax.DTDHandler"), "interface jdk.internal.org.xml.sax.DTDHandler");
        check(hasI(c.getInterfaces(), "jdk.internal.org.xml.sax.EntityResolver"), "interface jdk.internal.org.xml.sax.EntityResolver");
        check(hasI(c.getInterfaces(), "jdk.internal.org.xml.sax.ErrorHandler"), "interface jdk.internal.org.xml.sax.ErrorHandler");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 17, "declared methods >= 17");
        check(hasM(ms, "characters"), "method characters");
        check(hasM(ms, "endDocument"), "method endDocument");
        check(hasM(ms, "endElement"), "method endElement");
        check(hasM(ms, "endPrefixMapping"), "method endPrefixMapping");
        check(hasM(ms, "error"), "method error");
        check(hasM(ms, "fatalError"), "method fatalError");
        check(hasM(ms, "ignorableWhitespace"), "method ignorableWhitespace");
        check(hasM(ms, "notationDecl"), "method notationDecl");
        check(hasM(ms, "processingInstruction"), "method processingInstruction");
        check(hasM(ms, "resolveEntity"), "method resolveEntity");
        check(hasM(ms, "setDocumentLocator"), "method setDocumentLocator");
        check(hasM(ms, "skippedEntity"), "method skippedEntity");
        check(hasM(ms, "startDocument"), "method startDocument");
        check(hasM(ms, "startElement"), "method startElement");
        check(hasM(ms, "startPrefixMapping"), "method startPrefixMapping");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
