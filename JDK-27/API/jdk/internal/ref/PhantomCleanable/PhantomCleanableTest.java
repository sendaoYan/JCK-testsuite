/*
 * @test
 * @summary jdk.internal.ref.PhantomCleanable conformance: internal package reflection.
 * @modules java.base/jdk.internal.ref:+open
 * @run main PhantomCleanableTest
 */
public class PhantomCleanableTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.ref.PhantomCleanable", false, PhantomCleanableTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.ref.PhantomReference"), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "java.lang.ref.Cleaner$Cleanable"), "interface java.lang.ref.Cleaner$Cleanable");
        check(hasMethod(c, "clean"), "method clean");
        check(hasMethod(c, "clear"), "method clear");
        check(hasMethod(c, "enqueue"), "method enqueue");
        check(hasMethod(c, "isEnqueued"), "method isEnqueued");
        check(hasMethod(c, "performCleanup"), "method performCleanup");
        check(hasField(c, "$assertionsDisabled"), "field $assertionsDisabled");
        check(hasField(c, "index"), "field index");
        check(hasField(c, "list"), "field list");
        check(hasField(c, "node"), "field node");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
