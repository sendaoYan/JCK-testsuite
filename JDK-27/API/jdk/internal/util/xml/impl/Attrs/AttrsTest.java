/*
 * @test
 * @summary jdk.internal.util.xml.impl.Attrs conformance: internal class reflection.
 * @modules java.base/jdk.internal.util.xml.impl:+open
 * @run main AttrsTest
 */
public class AttrsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.util.xml.impl.Attrs", false, AttrsTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.util.xml.impl.Attrs"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length >= 1, "direct interfaces >= 1");
        check(hasI(c.getInterfaces(), "jdk.internal.org.xml.sax.Attributes"), "interface jdk.internal.org.xml.sax.Attributes");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 20, "declared methods >= 20");
        check(hasM(ms, "getIndex"), "method getIndex");
        check(hasM(ms, "getLength"), "method getLength");
        check(hasM(ms, "getLocalName"), "method getLocalName");
        check(hasM(ms, "getQName"), "method getQName");
        check(hasM(ms, "getType"), "method getType");
        check(hasM(ms, "getURI"), "method getURI");
        check(hasM(ms, "getValue"), "method getValue");
        check(hasM(ms, "isDeclared"), "method isDeclared");
        check(hasM(ms, "isSpecified"), "method isSpecified");
        check(hasM(ms, "setLength"), "method setLength");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 2, "declared fields >= 2");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
