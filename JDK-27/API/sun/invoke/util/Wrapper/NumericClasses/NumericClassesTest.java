/*
 * @test
 * @summary sun.invoke.util.Wrapper.NumericClasses conformance: internal package reflection.
 * @modules java.base/sun.invoke.util:+open
 * @run main NumericClassesTest
 */
public class NumericClassesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.invoke.util.Wrapper$NumericClasses", false, NumericClassesTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasField(c, "BYTE_CLASS"), "field BYTE_CLASS");
        check(hasField(c, "BYTE_SUPERCLASSES"), "field BYTE_SUPERCLASSES");
        check(hasField(c, "CHAR_CLASS"), "field CHAR_CLASS");
        check(hasField(c, "CHAR_SUPERCLASSES"), "field CHAR_SUPERCLASSES");
        check(hasField(c, "DOUBLE_CLASS"), "field DOUBLE_CLASS");
        check(hasField(c, "FLOAT_CLASS"), "field FLOAT_CLASS");
        check(hasField(c, "FLOAT_SUPERCLASSES"), "field FLOAT_SUPERCLASSES");
        check(hasField(c, "INT_CLASS"), "field INT_CLASS");
        check(hasField(c, "INT_SUPERCLASSES"), "field INT_SUPERCLASSES");
        check(hasField(c, "LONG_CLASS"), "field LONG_CLASS");
        check(hasField(c, "LONG_SUPERCLASSES"), "field LONG_SUPERCLASSES");
        check(hasField(c, "SHORT_CLASS"), "field SHORT_CLASS");
        check(hasField(c, "SHORT_SUPERCLASSES"), "field SHORT_SUPERCLASSES");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
