/*
 * @test
 * @summary jdk.internal.util.random.RandomSupport.AbstractSplittableGenerator conformance: internal abstract class reflection.
 * @modules java.base/jdk.internal.util.random:+open
 * @run main AbstractSplittableGeneratorTest
 */
public class AbstractSplittableGeneratorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.util.random.RandomSupport$AbstractSplittableGenerator", false, AbstractSplittableGeneratorTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.util.random.RandomSupport$AbstractSplittableGenerator"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.internal.util.random.RandomSupport$AbstractSpliteratorGenerator"), "superclass jdk.internal.util.random.RandomSupport$AbstractSpliteratorGenerator");
        check(c.getInterfaces().length >= 1, "direct interfaces >= 1");
        check(hasI(c.getInterfaces(), "java.util.random.RandomGenerator$SplittableGenerator"), "interface java.util.random.RandomGenerator$SplittableGenerator");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 6, "declared methods >= 6");
        check(hasM(ms, "split"), "method split");
        check(hasM(ms, "splits"), "method splits");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
