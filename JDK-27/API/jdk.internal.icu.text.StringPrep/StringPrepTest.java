/*
 * @test
 * @summary jdk.internal.icu.text.StringPrep conformance: internal class reflection.
 * @modules java.base/jdk.internal.icu.text:+open
 * @run main StringPrepTest
 */
public class StringPrepTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.icu.text.StringPrep", false, StringPrepTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.icu.text.StringPrep"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 7, "declared methods >= 7");
        check(hasM(ms, "prepare"), "method prepare");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 30, "declared fields >= 30");
        check(hasF(fs, "ALLOW_UNASSIGNED"), "field ALLOW_UNASSIGNED");
        check(hasF(fs, "DEFAULT"), "field DEFAULT");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
