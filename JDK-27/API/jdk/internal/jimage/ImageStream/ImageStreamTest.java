/*
 * @test
 * @summary jdk.internal.jimage.ImageStream conformance: internal package reflection.
 * @modules java.base/jdk.internal.jimage:+open
 * @run main ImageStreamTest
 */
public class ImageStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.jimage.ImageStream", false, ImageStreamTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "align"), "method align");
        check(hasMethod(c, "ensure"), "method ensure");
        check(hasMethod(c, "get"), "method get");
        check(hasMethod(c, "getBuffer"), "method getBuffer");
        check(hasMethod(c, "getBytes"), "method getBytes");
        check(hasMethod(c, "getInt"), "method getInt");
        check(hasMethod(c, "getLong"), "method getLong");
        check(hasMethod(c, "getPosition"), "method getPosition");
        check(hasMethod(c, "getShort"), "method getShort");
        check(hasMethod(c, "getSize"), "method getSize");
        check(hasMethod(c, "hasByte"), "method hasByte");
        check(hasMethod(c, "hasBytes"), "method hasBytes");
        check(hasMethod(c, "put"), "method put");
        check(hasMethod(c, "putInt"), "method putInt");
        check(hasMethod(c, "putLong"), "method putLong");
        check(hasMethod(c, "putShort"), "method putShort");
        check(hasMethod(c, "setPosition"), "method setPosition");
        check(hasMethod(c, "skip"), "method skip");
        check(hasMethod(c, "toArray"), "method toArray");
        check(hasField(c, "buffer"), "field buffer");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
