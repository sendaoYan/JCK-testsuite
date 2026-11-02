/*
 * @test
 * @summary jdk.internal.misc.ThreadFlock conformance: internal package reflection.
 * @modules java.base/jdk.internal.misc:+open
 * @run main ThreadFlockTest
 */
public class ThreadFlockTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.misc.ThreadFlock", false, ThreadFlockTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "java.lang.AutoCloseable"), "interface java.lang.AutoCloseable");
        check(hasMethod(c, "awaitAll"), "method awaitAll");
        check(hasMethod(c, "close"), "method close");
        check(hasMethod(c, "containsThread"), "method containsThread");
        check(hasMethod(c, "isClosed"), "method isClosed");
        check(hasMethod(c, "isShutdown"), "method isShutdown");
        check(hasMethod(c, "name"), "method name");
        check(hasMethod(c, "open"), "method open");
        check(hasMethod(c, "owner"), "method owner");
        check(hasMethod(c, "shutdown"), "method shutdown");
        check(hasMethod(c, "start"), "method start");
        check(hasMethod(c, "threads"), "method threads");
        check(hasMethod(c, "toString"), "method toString");
        check(hasMethod(c, "wakeup"), "method wakeup");
        check(hasField(c, "$assertionsDisabled"), "field $assertionsDisabled");
        check(hasField(c, "JLA"), "field JLA");
        check(hasField(c, "PERMIT"), "field PERMIT");
        check(hasField(c, "THREAD_COUNT"), "field THREAD_COUNT");
        check(hasField(c, "closed"), "field closed");
        check(hasField(c, "container"), "field container");
        check(hasField(c, "name"), "field name");
        check(hasField(c, "permit"), "field permit");
        check(hasField(c, "scopedValueBindings"), "field scopedValueBindings");
        check(hasField(c, "shutdown"), "field shutdown");
        check(hasField(c, "threadCount"), "field threadCount");
        check(hasField(c, "threads"), "field threads");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
