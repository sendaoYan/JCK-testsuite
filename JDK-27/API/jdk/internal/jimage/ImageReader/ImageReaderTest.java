/*
 * @test
 * @summary jdk.internal.jimage.ImageReader conformance: internal package reflection.
 * @modules java.base/jdk.internal.jimage:+open
 * @run main ImageReaderTest
 */
public class ImageReaderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.jimage.ImageReader", false, ImageReaderTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "java.lang.AutoCloseable"), "interface java.lang.AutoCloseable");
        check(hasMethod(c, "close"), "method close");
        check(hasMethod(c, "containsResource"), "method containsResource");
        check(hasMethod(c, "findNode"), "method findNode");
        check(hasMethod(c, "findResourceNode"), "method findResourceNode");
        check(hasMethod(c, "getResource"), "method getResource");
        check(hasMethod(c, "getResourceBuffer"), "method getResourceBuffer");
        check(hasMethod(c, "open"), "method open");
        check(hasField(c, "PREVIEW_RESOURCE_PREFIX"), "field PREVIEW_RESOURCE_PREFIX");
        check(hasField(c, "closed"), "field closed");
        check(hasField(c, "reader"), "field reader");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
