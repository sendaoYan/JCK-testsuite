/*
 * @test
 * @summary jdk.internal.math.FloatingDecimal.BinaryToASCIIBuffer conformance: internal package reflection.
 * @modules java.base/jdk.internal.math:+open
 * @run main BinaryToASCIIBufferTest
 */
public class BinaryToASCIIBufferTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.math.FloatingDecimal$BinaryToASCIIBuffer", false, BinaryToASCIIBufferTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "decimalDigitsExact"), "method decimalDigitsExact");
        check(hasMethod(c, "digitsRoundedUp"), "method digitsRoundedUp");
        check(hasMethod(c, "getDecimalExponent"), "method getDecimalExponent");
        check(hasMethod(c, "getDigits"), "method getDigits");
        check(hasField(c, "$assertionsDisabled"), "field $assertionsDisabled");
        check(hasField(c, "N_5_BITS"), "field N_5_BITS");
        check(hasField(c, "decExponent"), "field decExponent");
        check(hasField(c, "decimalDigitsRoundedUp"), "field decimalDigitsRoundedUp");
        check(hasField(c, "digits"), "field digits");
        check(hasField(c, "exactDecimalConversion"), "field exactDecimalConversion");
        check(hasField(c, "firstDigitIndex"), "field firstDigitIndex");
        check(hasField(c, "insignificantDigitsNumber"), "field insignificantDigitsNumber");
        check(hasField(c, "nDigits"), "field nDigits");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
