/*
 * @test
 * @summary jdk.internal.org.xml.sax.Attributes conformance: internal interface reflection.
 * @modules java.base/jdk.internal.org.xml.sax:+open
 * @run main AttributesTest
 */
public class AttributesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.org.xml.sax.Attributes", false, AttributesTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.org.xml.sax.Attributes"), "name");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() == null, "superclass null");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 12, "declared methods >= 12");
        check(hasM(ms, "getIndex"), "method getIndex");
        check(hasM(ms, "getLength"), "method getLength");
        check(hasM(ms, "getLocalName"), "method getLocalName");
        check(hasM(ms, "getQName"), "method getQName");
        check(hasM(ms, "getType"), "method getType");
        check(hasM(ms, "getURI"), "method getURI");
        check(hasM(ms, "getValue"), "method getValue");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
