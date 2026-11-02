/*
 * @test
 * @summary jdk.internal.icu.impl.Punycode conformance: internal class reflection.
 * @modules java.base/jdk.internal.icu.impl:+open
 * @run main PunycodeTest
 */
public class PunycodeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.icu.impl.Punycode", false, PunycodeTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.icu.impl.Punycode"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 9, "declared methods >= 9");
        check(hasM(ms, "decode"), "method decode");
        check(hasM(ms, "encode"), "method encode");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 18, "declared fields >= 18");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
