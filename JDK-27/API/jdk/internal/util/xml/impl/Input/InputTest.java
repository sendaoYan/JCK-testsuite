/*
 * @test
 * @summary jdk.internal.util.xml.impl.Input conformance: internal class reflection.
 * @modules java.base/jdk.internal.util.xml.impl:+open
 * @run main InputTest
 */
public class InputTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.util.xml.impl.Input", false, InputTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.util.xml.impl.Input"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 9, "declared fields >= 9");
        check(hasF(fs, "chIdx"), "field chIdx");
        check(hasF(fs, "chLen"), "field chLen");
        check(hasF(fs, "chars"), "field chars");
        check(hasF(fs, "next"), "field next");
        check(hasF(fs, "pubid"), "field pubid");
        check(hasF(fs, "src"), "field src");
        check(hasF(fs, "sysid"), "field sysid");
        check(hasF(fs, "xmlenc"), "field xmlenc");
        check(hasF(fs, "xmlver"), "field xmlver");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
