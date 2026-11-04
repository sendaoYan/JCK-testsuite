/*
 * @test
 * @summary jdk.internal.icu.impl.UBiDiProps conformance: internal class reflection.
 * @modules java.base/jdk.internal.icu.impl:+open
 * @run main UBiDiPropsTest
 */
public class UBiDiPropsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.icu.impl.UBiDiProps", false, UBiDiPropsTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.icu.impl.UBiDiProps"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 13, "declared methods >= 13");
        check(hasM(ms, "getClass"), "method getClass");
        check(hasM(ms, "getJoiningGroup"), "method getJoiningGroup");
        check(hasM(ms, "getJoiningType"), "method getJoiningType");
        check(hasM(ms, "getMirror"), "method getMirror");
        check(hasM(ms, "getPairedBracket"), "method getPairedBracket");
        check(hasM(ms, "getPairedBracketType"), "method getPairedBracketType");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 23, "declared fields >= 23");
        check(hasF(fs, "INSTANCE"), "field INSTANCE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
