/*
 * @test
 * @summary sun.text.normalizer.UnicodeMatcher conformance: interface and members.
 * @run main UnicodeMatcherTest
 */

public class UnicodeMatcherTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.text.normalizer.UnicodeMatcher", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.text.normalizer.UnicodeMatcher"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
