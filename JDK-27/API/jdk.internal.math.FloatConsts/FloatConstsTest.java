/*
 * @test
 * @summary jdk.internal.math.FloatConsts conformance: internal package reflection.
 * @modules java.base/jdk.internal.math:+open
 * @run main FloatConstsTest
 */
public class FloatConstsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.math.FloatConsts", false, FloatConstsTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasField(c, "EXP_BIAS"), "field EXP_BIAS");
        check(hasField(c, "EXP_BIT_MASK"), "field EXP_BIT_MASK");
        check(hasField(c, "MAG_BIT_MASK"), "field MAG_BIT_MASK");
        check(hasField(c, "MIN_SUB_EXPONENT"), "field MIN_SUB_EXPONENT");
        check(hasField(c, "SIGNIFICAND_WIDTH"), "field SIGNIFICAND_WIDTH");
        check(hasField(c, "SIGNIF_BIT_MASK"), "field SIGNIF_BIT_MASK");
        check(hasField(c, "SIGN_BIT_MASK"), "field SIGN_BIT_MASK");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
