/*
 * @test
 * @summary jdk.internal.math.FloatingDecimal conformance: internal package reflection.
 * @modules java.base/jdk.internal.math:+open
 * @run main FloatingDecimalTest
 */
public class FloatingDecimalTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.math.FloatingDecimal", false, FloatingDecimalTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getBinaryToASCIIConverter"), "method getBinaryToASCIIConverter");
        check(hasMethod(c, "parseDouble"), "method parseDouble");
        check(hasMethod(c, "parseDoubleSignlessDigits"), "method parseDoubleSignlessDigits");
        check(hasMethod(c, "parseFloat"), "method parseFloat");
        check(hasField(c, "$assertionsDisabled"), "field $assertionsDisabled");
        check(hasField(c, "B2AC_NEGATIVE_ZERO"), "field B2AC_NEGATIVE_ZERO");
        check(hasField(c, "B2AC_POSITIVE_ZERO"), "field B2AC_POSITIVE_ZERO");
        check(hasField(c, "BINARY_16_IX"), "field BINARY_16_IX");
        check(hasField(c, "BINARY_32_IX"), "field BINARY_32_IX");
        check(hasField(c, "BINARY_64_IX"), "field BINARY_64_IX");
        check(hasField(c, "EXP_ONE"), "field EXP_ONE");
        check(hasField(c, "EXP_SHIFT"), "field EXP_SHIFT");
        check(hasField(c, "E_THR_I"), "field E_THR_I");
        check(hasField(c, "E_THR_Z"), "field E_THR_Z");
        check(hasField(c, "FLOG_10_MAX_LONG"), "field FLOG_10_MAX_LONG");
        check(hasField(c, "FRACT_HOB"), "field FRACT_HOB");
        check(hasField(c, "HEX_COUNT"), "field HEX_COUNT");
        check(hasField(c, "INFINITY_REP"), "field INFINITY_REP");
        check(hasField(c, "L_HALF"), "field L_HALF");
        check(hasField(c, "MAX_DEC_DIGITS"), "field MAX_DEC_DIGITS");
        check(hasField(c, "MAX_OUT"), "field MAX_OUT");
        check(hasField(c, "MAX_SMALL_BIN_EXP"), "field MAX_SMALL_BIN_EXP");
        check(hasField(c, "MIN_SMALL_BIN_EXP"), "field MIN_SMALL_BIN_EXP");
        check(hasField(c, "NAN_REP"), "field NAN_REP");
        check(hasField(c, "OMITTED"), "field OMITTED");
        check(hasField(c, "P"), "field P");
        check(hasField(c, "QP_MAX"), "field QP_MAX");
        check(hasField(c, "QP_MIN"), "field QP_MIN");
        check(hasField(c, "Q_MIN"), "field Q_MIN");
        check(hasField(c, "R_HALF"), "field R_HALF");
        check(hasField(c, "threadLocalBinaryToASCIIBuffer"), "field threadLocalBinaryToASCIIBuffer");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
