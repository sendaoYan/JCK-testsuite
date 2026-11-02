/*
 * @test
 * @summary jdk.internal.util.random.RandomSupport.AbstractArbitrarilyJumpableGenerator conformance: internal abstract class reflection.
 * @modules java.base/jdk.internal.util.random:+open
 * @run main AbstractArbitrarilyJumpableGeneratorTest
 */
public class AbstractArbitrarilyJumpableGeneratorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.util.random.RandomSupport$AbstractArbitrarilyJumpableGenerator", false, AbstractArbitrarilyJumpableGeneratorTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.util.random.RandomSupport$AbstractArbitrarilyJumpableGenerator"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.internal.util.random.RandomSupport$AbstractSpliteratorGenerator"), "superclass jdk.internal.util.random.RandomSupport$AbstractSpliteratorGenerator");
        check(c.getInterfaces().length >= 1, "direct interfaces >= 1");
        check(hasI(c.getInterfaces(), "java.util.random.RandomGenerator$ArbitrarilyJumpableGenerator"), "interface java.util.random.RandomGenerator$ArbitrarilyJumpableGenerator");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 15, "declared methods >= 15");
        check(hasM(ms, "copy"), "method copy");
        check(hasM(ms, "jumps"), "method jumps");
        check(hasM(ms, "leap"), "method leap");
        check(hasM(ms, "leaps"), "method leaps");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
