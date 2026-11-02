/*
 * @test
 * @summary jdk.internal.perf.Perf conformance: internal package reflection.
 * @modules java.base/jdk.internal.perf:+open
 * @run main PerfTest
 */
public class PerfTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.perf.Perf", false, PerfTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "attach"), "method attach");
        check(hasMethod(c, "createByteArray"), "method createByteArray");
        check(hasMethod(c, "createLong"), "method createLong");
        check(hasMethod(c, "createString"), "method createString");
        check(hasMethod(c, "getPerf"), "method getPerf");
        check(hasMethod(c, "highResCounter"), "method highResCounter");
        check(hasMethod(c, "highResFrequency"), "method highResFrequency");
        check(hasField(c, "instance"), "field instance");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
