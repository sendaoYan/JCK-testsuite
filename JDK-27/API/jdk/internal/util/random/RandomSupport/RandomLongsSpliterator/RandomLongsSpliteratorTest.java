/*
 * @test
 * @summary jdk.internal.util.random.RandomSupport.RandomLongsSpliterator conformance: internal class reflection.
 * @modules java.base/jdk.internal.util.random:+open
 * @run main RandomLongsSpliteratorTest
 */
public class RandomLongsSpliteratorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.util.random.RandomSupport$RandomLongsSpliterator", false, RandomLongsSpliteratorTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.util.random.RandomSupport$RandomLongsSpliterator"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.internal.util.random.RandomSupport$RandomSpliterator"), "superclass jdk.internal.util.random.RandomSupport$RandomSpliterator");
        check(c.getInterfaces().length >= 1, "direct interfaces >= 1");
        check(hasI(c.getInterfaces(), "java.util.Spliterator$OfLong"), "interface java.util.Spliterator$OfLong");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 7, "declared methods >= 7");
        check(hasM(ms, "forEachRemaining"), "method forEachRemaining");
        check(hasM(ms, "tryAdvance"), "method tryAdvance");
        check(hasM(ms, "trySplit"), "method trySplit");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 3, "declared fields >= 3");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
