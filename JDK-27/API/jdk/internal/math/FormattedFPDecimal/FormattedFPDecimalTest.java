/*
 * @test
 * @summary jdk.internal.math.FormattedFPDecimal conformance: internal package reflection.
 * @modules java.base/jdk.internal.math:+open
 * @run main FormattedFPDecimalTest
 */
public class FormattedFPDecimalTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.math.FormattedFPDecimal", false, FormattedFPDecimalTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getAway"), "method getAway");
        check(hasMethod(c, "getExact"), "method getExact");
        check(hasMethod(c, "getExp"), "method getExp");
        check(hasMethod(c, "getExponent"), "method getExponent");
        check(hasMethod(c, "getExponentRounded"), "method getExponentRounded");
        check(hasMethod(c, "getMantissa"), "method getMantissa");
        check(hasMethod(c, "getPrecision"), "method getPrecision");
        check(hasMethod(c, "getSignificand"), "method getSignificand");
        check(hasMethod(c, "set"), "method set");
        check(hasMethod(c, "valueForDoubleToString"), "method valueForDoubleToString");
        check(hasMethod(c, "valueOf"), "method valueOf");
        check(hasField(c, "GENERAL"), "field GENERAL");
        check(hasField(c, "PLAIN"), "field PLAIN");
        check(hasField(c, "SCIENTIFIC"), "field SCIENTIFIC");
        check(hasField(c, "away"), "field away");
        check(hasField(c, "digits"), "field digits");
        check(hasField(c, "e"), "field e");
        check(hasField(c, "exact"), "field exact");
        check(hasField(c, "exp"), "field exp");
        check(hasField(c, "f"), "field f");
        check(hasField(c, "n"), "field n");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
