/*
 * @test
 * @summary sun.nio.ch.IOUtil conformance: internal package reflection.
 * @modules java.base/sun.nio.ch:+open
 * @run main IOUtilTest
 */
public class IOUtilTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.ch.IOUtil", false, IOUtilTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "configureBlocking"), "method configureBlocking");
        check(hasMethod(c, "fdVal"), "method fdVal");
        check(hasMethod(c, "load"), "method load");
        check(hasMethod(c, "newFD"), "method newFD");
        check(hasField(c, "$assertionsDisabled"), "field $assertionsDisabled");
        check(hasField(c, "IOV_MAX"), "field IOV_MAX");
        check(hasField(c, "NIO_ACCESS"), "field NIO_ACCESS");
        check(hasField(c, "WRITEV_MAX"), "field WRITEV_MAX");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
