/*
 * @test
 * @summary jdk.internal.util.random.RandomSupport.AbstractSpliteratorGenerator conformance: internal abstract class reflection.
 * @modules java.base/jdk.internal.util.random:+open
 * @run main AbstractSpliteratorGeneratorTest
 */
public class AbstractSpliteratorGeneratorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.util.random.RandomSupport$AbstractSpliteratorGenerator", false, AbstractSpliteratorGeneratorTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.util.random.RandomSupport$AbstractSpliteratorGenerator"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length >= 1, "direct interfaces >= 1");
        check(hasI(c.getInterfaces(), "java.util.random.RandomGenerator"), "interface java.util.random.RandomGenerator");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 27, "declared methods >= 27");
        check(hasM(ms, "doubles"), "method doubles");
        check(hasM(ms, "ints"), "method ints");
        check(hasM(ms, "longs"), "method longs");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
