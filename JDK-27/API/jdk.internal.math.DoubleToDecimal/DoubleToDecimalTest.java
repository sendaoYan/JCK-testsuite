/*
 * @test
 * @summary jdk.internal.math.DoubleToDecimal conformance: internal package reflection.
 * @modules java.base/jdk.internal.math:+open
 * @run main DoubleToDecimalTest
 */
public class DoubleToDecimalTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.math.DoubleToDecimal", false, DoubleToDecimalTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.internal.math.ToDecimal"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "putDecimal"), "method putDecimal");
        check(hasMethod(c, "split"), "method split");
        check(hasMethod(c, "toString"), "method toString");
        check(hasField(c, "$assertionsDisabled"), "field $assertionsDisabled");
        check(hasField(c, "BQ_MASK"), "field BQ_MASK");
        check(hasField(c, "C_MAX"), "field C_MAX");
        check(hasField(c, "C_MIN"), "field C_MIN");
        check(hasField(c, "C_TINY"), "field C_TINY");
        check(hasField(c, "E_MAX"), "field E_MAX");
        check(hasField(c, "E_MIN"), "field E_MIN");
        check(hasField(c, "E_THR_I"), "field E_THR_I");
        check(hasField(c, "E_THR_Z"), "field E_THR_Z");
        check(hasField(c, "H"), "field H");
        check(hasField(c, "K_MAX"), "field K_MAX");
        check(hasField(c, "K_MIN"), "field K_MIN");
        check(hasField(c, "LATIN1"), "field LATIN1");
        check(hasField(c, "MASK_63"), "field MASK_63");
        check(hasField(c, "MAX_CHARS"), "field MAX_CHARS");
        check(hasField(c, "P"), "field P");
        check(hasField(c, "Q_MAX"), "field Q_MAX");
        check(hasField(c, "Q_MIN"), "field Q_MIN");
        check(hasField(c, "T_MASK"), "field T_MASK");
        check(hasField(c, "UTF16"), "field UTF16");
        check(hasField(c, "W"), "field W");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
