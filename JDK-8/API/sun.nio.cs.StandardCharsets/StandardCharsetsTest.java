/*
 * @test
 * @summary sun.nio.cs.StandardCharsets conformance: class and members.
 * @run main StandardCharsetsTest
 */

public class StandardCharsetsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.cs.StandardCharsets", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.nio.cs.StandardCharsets"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.nio.cs.FastCharsetProvider"), "superclass sun.nio.cs.FastCharsetProvider");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
