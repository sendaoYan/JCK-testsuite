/*
 * @test
 * @summary jdk.internal.jimage.ImageStrings conformance: internal package reflection.
 * @modules java.base/jdk.internal.jimage:+open
 * @run main ImageStringsTest
 */
public class ImageStringsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.jimage.ImageStrings", false, ImageStringsTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "superclass null");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "add"), "method add");
        check(hasMethod(c, "get"), "method get");
        check(hasMethod(c, "match"), "method match");
        check(hasField(c, "CLASS_STRING_OFFSET"), "field CLASS_STRING_OFFSET");
        check(hasField(c, "EMPTY_STRING_OFFSET"), "field EMPTY_STRING_OFFSET");
        check(hasField(c, "MODULES_STRING_OFFSET"), "field MODULES_STRING_OFFSET");
        check(hasField(c, "PACKAGES_STRING_OFFSET"), "field PACKAGES_STRING_OFFSET");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
