/*
 * @test
 * @summary sun.invoke.util.ValueConversions conformance: internal package reflection.
 * @modules java.base/sun.invoke.util:+open
 * @run main ValueConversionsTest
 */
public class ValueConversionsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.invoke.util.ValueConversions", false, ValueConversionsTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "boxExact"), "method boxExact");
        check(hasMethod(c, "convertPrimitive"), "method convertPrimitive");
        check(hasMethod(c, "ignore"), "method ignore");
        check(hasMethod(c, "primitiveConversion"), "method primitiveConversion");
        check(hasMethod(c, "unboxCast"), "method unboxCast");
        check(hasMethod(c, "unboxExact"), "method unboxExact");
        check(hasMethod(c, "unboxWiden"), "method unboxWiden");
        check(hasMethod(c, "widenSubword"), "method widenSubword");
        check(hasField(c, "$assertionsDisabled"), "field $assertionsDisabled");
        check(hasField(c, "BOX_CONVERSIONS"), "field BOX_CONVERSIONS");
        check(hasField(c, "CONVERT_PRIMITIVE_FUNCTIONS"), "field CONVERT_PRIMITIVE_FUNCTIONS");
        check(hasField(c, "IMPL_LOOKUP"), "field IMPL_LOOKUP");
        check(hasField(c, "ONE_INT"), "field ONE_INT");
        check(hasField(c, "THIS_CLASS"), "field THIS_CLASS");
        check(hasField(c, "UNBOX_CONVERSIONS"), "field UNBOX_CONVERSIONS");
        check(hasField(c, "ZERO_INT"), "field ZERO_INT");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
