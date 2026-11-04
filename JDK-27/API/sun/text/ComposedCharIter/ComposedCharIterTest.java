/*
 * @test
 * @summary sun.text.ComposedCharIter conformance: internal package reflection.
 * @modules java.base/sun.text:+open
 * @run main ComposedCharIterTest
 */
public class ComposedCharIterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.text.ComposedCharIter", false, ComposedCharIterTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "decomposition"), "method decomposition");
        check(hasMethod(c, "next"), "method next");
        check(hasField(c, "DONE"), "field DONE");
        check(hasField(c, "chars"), "field chars");
        check(hasField(c, "curChar"), "field curChar");
        check(hasField(c, "decompNum"), "field decompNum");
        check(hasField(c, "decomps"), "field decomps");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
