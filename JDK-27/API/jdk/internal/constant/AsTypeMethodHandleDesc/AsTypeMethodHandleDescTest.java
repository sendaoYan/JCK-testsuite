/*
 * @test
 * @summary jdk.internal.constant.AsTypeMethodHandleDesc conformance: internal package reflection.
 * @modules java.base/jdk.internal.constant:+open
 * @run main AsTypeMethodHandleDescTest
 */
public class AsTypeMethodHandleDescTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.constant.AsTypeMethodHandleDesc", false, AsTypeMethodHandleDescTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.constant.DynamicConstantDesc"), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "java.lang.constant.MethodHandleDesc"), "interface java.lang.constant.MethodHandleDesc");
        check(hasMethod(c, "invocationType"), "method invocationType");
        check(hasMethod(c, "resolveConstantDesc"), "method resolveConstantDesc");
        check(hasMethod(c, "toString"), "method toString");
        check(hasField(c, "MHD_METHODHANDLE_ASTYPE"), "field MHD_METHODHANDLE_ASTYPE");
        check(hasField(c, "type"), "field type");
        check(hasField(c, "underlying"), "field underlying");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
