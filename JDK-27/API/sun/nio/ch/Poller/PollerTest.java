/*
 * @test
 * @summary sun.nio.ch.Poller conformance: internal package reflection.
 * @modules java.base/sun.nio.ch:+open
 * @run main PollerTest
 */
public class PollerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.ch.Poller", false, PollerTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "masterPoller"), "method masterPoller");
        check(hasMethod(c, "poll"), "method poll");
        check(hasMethod(c, "pollSelector"), "method pollSelector");
        check(hasMethod(c, "readPollers"), "method readPollers");
        check(hasMethod(c, "stopPoll"), "method stopPoll");
        check(hasMethod(c, "toString"), "method toString");
        check(hasMethod(c, "writePollers"), "method writePollers");
        check(hasField(c, "$assertionsDisabled"), "field $assertionsDisabled");
        check(hasField(c, "JLA"), "field JLA");
        check(hasField(c, "POLLER_GROUP"), "field POLLER_GROUP");
        check(hasField(c, "map"), "field map");
        check(hasField(c, "owner"), "field owner");
        check(hasField(c, "shutdown"), "field shutdown");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
