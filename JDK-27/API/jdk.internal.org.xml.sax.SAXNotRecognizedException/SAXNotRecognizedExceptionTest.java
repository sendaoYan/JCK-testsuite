/*
 * @test
 * @summary jdk.internal.org.xml.sax.SAXNotRecognizedException conformance: internal class reflection.
 * @modules java.base/jdk.internal.org.xml.sax:+open
 * @run main SAXNotRecognizedExceptionTest
 */
public class SAXNotRecognizedExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.org.xml.sax.SAXNotRecognizedException", false, SAXNotRecognizedExceptionTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.org.xml.sax.SAXNotRecognizedException"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.internal.org.xml.sax.SAXException"), "superclass jdk.internal.org.xml.sax.SAXException");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 1, "declared fields >= 1");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
