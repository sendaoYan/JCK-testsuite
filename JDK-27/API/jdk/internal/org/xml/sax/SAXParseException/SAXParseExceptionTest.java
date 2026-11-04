/*
 * @test
 * @summary jdk.internal.org.xml.sax.SAXParseException conformance: internal class reflection.
 * @modules java.base/jdk.internal.org.xml.sax:+open
 * @run main SAXParseExceptionTest
 */
public class SAXParseExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.org.xml.sax.SAXParseException", false, SAXParseExceptionTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.org.xml.sax.SAXParseException"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.internal.org.xml.sax.SAXException"), "superclass jdk.internal.org.xml.sax.SAXException");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 6, "declared methods >= 6");
        check(hasM(ms, "getColumnNumber"), "method getColumnNumber");
        check(hasM(ms, "getLineNumber"), "method getLineNumber");
        check(hasM(ms, "getPublicId"), "method getPublicId");
        check(hasM(ms, "getSystemId"), "method getSystemId");
        check(hasM(ms, "toString"), "method toString");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 5, "declared fields >= 5");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
