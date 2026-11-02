/*
 * @test
 * @summary jdk.internal.vm.SharedThreadContainer conformance: internal package reflection.
 * @modules java.base/jdk.internal.vm:+open
 * @run main SharedThreadContainerTest
 */
public class SharedThreadContainerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.vm.SharedThreadContainer", false, SharedThreadContainerTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.internal.vm.ThreadContainer"), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "java.lang.AutoCloseable"), "interface java.lang.AutoCloseable");
        check(hasMethod(c, "close"), "method close");
        check(hasMethod(c, "create"), "method create");
        check(hasMethod(c, "name"), "method name");
        check(hasMethod(c, "onExit"), "method onExit");
        check(hasMethod(c, "onStart"), "method onStart");
        check(hasMethod(c, "owner"), "method owner");
        check(hasMethod(c, "start"), "method start");
        check(hasMethod(c, "threads"), "method threads");
        check(hasField(c, "CLOSED"), "field CLOSED");
        check(hasField(c, "JLA"), "field JLA");
        check(hasField(c, "VIRTUAL_THREADS"), "field VIRTUAL_THREADS");
        check(hasField(c, "closed"), "field closed");
        check(hasField(c, "key"), "field key");
        check(hasField(c, "name"), "field name");
        check(hasField(c, "virtualThreads"), "field virtualThreads");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
