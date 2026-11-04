/*
 * @test
 * @summary jdk.internal.random.L64X128MixRandom conformance: internal package reflection.
 * @modules java.base/jdk.internal.random:+open
 * @run main L64X128MixRandomTest
 */
public class L64X128MixRandomTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.random.L64X128MixRandom", false, L64X128MixRandomTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.internal.util.random.RandomSupport$AbstractSplittableWithBrineGenerator"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "nextLong"), "method nextLong");
        check(hasMethod(c, "split"), "method split");
        check(hasField(c, "M"), "field M");
        check(hasField(c, "a"), "field a");
        check(hasField(c, "defaultGen"), "field defaultGen");
        check(hasField(c, "s"), "field s");
        check(hasField(c, "x0"), "field x0");
        check(hasField(c, "x1"), "field x1");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
