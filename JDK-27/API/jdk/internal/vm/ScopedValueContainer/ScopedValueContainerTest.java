/*
 * @test
 * @summary jdk.internal.vm.ScopedValueContainer conformance: internal package reflection.
 * @modules java.base/jdk.internal.vm:+open
 * @run main ScopedValueContainerTest
 */
public class ScopedValueContainerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.vm.ScopedValueContainer", false, ScopedValueContainerTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.internal.vm.StackableScope"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "call"), "method call");
        check(hasMethod(c, "captureBindings"), "method captureBindings");
        check(hasMethod(c, "latest"), "method latest");
        check(hasMethod(c, "run"), "method run");
        check(hasField(c, "$assertionsDisabled"), "field $assertionsDisabled");
        check(hasField(c, "JLA"), "field JLA");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
