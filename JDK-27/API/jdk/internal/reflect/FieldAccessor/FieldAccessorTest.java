/*
 * @test
 * @summary jdk.internal.reflect.FieldAccessor conformance: internal package reflection.
 * @modules java.base/jdk.internal.reflect:+open
 * @run main FieldAccessorTest
 */
public class FieldAccessorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.reflect.FieldAccessor", false, FieldAccessorTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "superclass null");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "get"), "method get");
        check(hasMethod(c, "getBoolean"), "method getBoolean");
        check(hasMethod(c, "getByte"), "method getByte");
        check(hasMethod(c, "getChar"), "method getChar");
        check(hasMethod(c, "getDouble"), "method getDouble");
        check(hasMethod(c, "getFloat"), "method getFloat");
        check(hasMethod(c, "getInt"), "method getInt");
        check(hasMethod(c, "getLong"), "method getLong");
        check(hasMethod(c, "getShort"), "method getShort");
        check(hasMethod(c, "set"), "method set");
        check(hasMethod(c, "setBoolean"), "method setBoolean");
        check(hasMethod(c, "setByte"), "method setByte");
        check(hasMethod(c, "setChar"), "method setChar");
        check(hasMethod(c, "setDouble"), "method setDouble");
        check(hasMethod(c, "setFloat"), "method setFloat");
        check(hasMethod(c, "setInt"), "method setInt");
        check(hasMethod(c, "setLong"), "method setLong");
        check(hasMethod(c, "setShort"), "method setShort");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
