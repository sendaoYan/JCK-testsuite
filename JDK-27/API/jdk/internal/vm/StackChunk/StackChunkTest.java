/*
 * @test
 * @summary jdk.internal.vm.StackChunk conformance: internal package reflection.
 * @modules java.base/jdk.internal.vm:+open
 * @run main StackChunkTest
 */
public class StackChunkTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.vm.StackChunk", false, StackChunkTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "init"), "method init");
        check(hasMethod(c, "isEmpty"), "method isEmpty");
        check(hasMethod(c, "parent"), "method parent");
        check(hasField(c, "bottom"), "field bottom");
        check(hasField(c, "parent"), "field parent");
        check(hasField(c, "size"), "field size");
        check(hasField(c, "sp"), "field sp");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
