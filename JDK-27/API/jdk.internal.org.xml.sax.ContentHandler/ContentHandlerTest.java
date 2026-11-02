/*
 * @test
 * @summary jdk.internal.org.xml.sax.ContentHandler conformance: internal interface reflection.
 * @modules java.base/jdk.internal.org.xml.sax:+open
 * @run main ContentHandlerTest
 */
public class ContentHandlerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.org.xml.sax.ContentHandler", false, ContentHandlerTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.org.xml.sax.ContentHandler"), "name");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() == null, "superclass null");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 11, "declared methods >= 11");
        check(hasM(ms, "characters"), "method characters");
        check(hasM(ms, "endDocument"), "method endDocument");
        check(hasM(ms, "endElement"), "method endElement");
        check(hasM(ms, "endPrefixMapping"), "method endPrefixMapping");
        check(hasM(ms, "ignorableWhitespace"), "method ignorableWhitespace");
        check(hasM(ms, "processingInstruction"), "method processingInstruction");
        check(hasM(ms, "setDocumentLocator"), "method setDocumentLocator");
        check(hasM(ms, "skippedEntity"), "method skippedEntity");
        check(hasM(ms, "startDocument"), "method startDocument");
        check(hasM(ms, "startElement"), "method startElement");
        check(hasM(ms, "startPrefixMapping"), "method startPrefixMapping");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
