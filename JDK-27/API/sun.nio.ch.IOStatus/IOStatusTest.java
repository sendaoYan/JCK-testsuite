/*
 * @test
 * @summary sun.nio.ch.IOStatus conformance: internal package reflection.
 * @modules java.base/sun.nio.ch:+open
 * @run main IOStatusTest
 */
public class IOStatusTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.ch.IOStatus", false, IOStatusTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "check"), "method check");
        check(hasMethod(c, "checkAll"), "method checkAll");
        check(hasMethod(c, "normalize"), "method normalize");
        check(hasField(c, "EOF"), "field EOF");
        check(hasField(c, "INTERRUPTED"), "field INTERRUPTED");
        check(hasField(c, "THROWN"), "field THROWN");
        check(hasField(c, "UNAVAILABLE"), "field UNAVAILABLE");
        check(hasField(c, "UNSUPPORTED"), "field UNSUPPORTED");
        check(hasField(c, "UNSUPPORTED_CASE"), "field UNSUPPORTED_CASE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
