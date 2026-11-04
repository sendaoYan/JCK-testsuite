/*
 * @test
 * @summary jdk.internal.vm.ThreadContainers conformance: internal package reflection.
 * @modules java.base/jdk.internal.vm:+open
 * @run main ThreadContainersTest
 */
public class ThreadContainersTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.vm.ThreadContainers", false, ThreadContainersTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "container"), "method container");
        check(hasMethod(c, "deregisterContainer"), "method deregisterContainer");
        check(hasMethod(c, "registerContainer"), "method registerContainer");
        check(hasMethod(c, "root"), "method root");
        check(hasMethod(c, "trackAllThreads"), "method trackAllThreads");
        check(hasField(c, "$assertionsDisabled"), "field $assertionsDisabled");
        check(hasField(c, "CONTAINER_REGISTRY"), "field CONTAINER_REGISTRY");
        check(hasField(c, "JLA"), "field JLA");
        check(hasField(c, "QUEUE"), "field QUEUE");
        check(hasField(c, "ROOT_CONTAINER"), "field ROOT_CONTAINER");
        check(hasField(c, "TRACK_ALL_THREADS"), "field TRACK_ALL_THREADS");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
