/*
 * @test
 * @summary jdk.internal.util.xml.PropertiesDefaultHandler conformance: internal class reflection.
 * @modules java.base/jdk.internal.util.xml:+open
 * @run main PropertiesDefaultHandlerTest
 */
public class PropertiesDefaultHandlerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.util.xml.PropertiesDefaultHandler", false, PropertiesDefaultHandlerTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.util.xml.PropertiesDefaultHandler"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.internal.org.xml.sax.helpers.DefaultHandler"), "superclass jdk.internal.org.xml.sax.helpers.DefaultHandler");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 11, "declared methods >= 11");
        check(hasM(ms, "characters"), "method characters");
        check(hasM(ms, "endElement"), "method endElement");
        check(hasM(ms, "error"), "method error");
        check(hasM(ms, "fatalError"), "method fatalError");
        check(hasM(ms, "load"), "method load");
        check(hasM(ms, "resolveEntity"), "method resolveEntity");
        check(hasM(ms, "startDTD"), "method startDTD");
        check(hasM(ms, "startElement"), "method startElement");
        check(hasM(ms, "startInternalSub"), "method startInternalSub");
        check(hasM(ms, "store"), "method store");
        check(hasM(ms, "warning"), "method warning");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 17, "declared fields >= 17");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
