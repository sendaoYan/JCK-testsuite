/*
 * @test
 * @summary jdk.internal.vm.ContinuationSupport conformance: internal package reflection.
 * @modules java.base/jdk.internal.vm:+open
 * @run main ContinuationSupportTest
 */
public class ContinuationSupportTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.vm.ContinuationSupport", false, ContinuationSupportTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "ensureSupported"), "method ensureSupported");
        check(hasMethod(c, "isSupported"), "method isSupported");
        check(hasMethod(c, "pinIfSupported"), "method pinIfSupported");
        check(hasMethod(c, "unpinIfSupported"), "method unpinIfSupported");
        check(hasField(c, "SUPPORTED"), "field SUPPORTED");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
