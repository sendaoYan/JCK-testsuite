/*
 * @test
 * @summary com.sun.org.apache.xpath.internal.axes.HasPositionalPredChecker conformance: reflection.
 * @run main HasPositionalPredCheckerTest
 */

public class HasPositionalPredCheckerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xpath.internal.axes.HasPositionalPredChecker", false, HasPositionalPredCheckerTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("HasPositionalPredChecker"), "simple name HasPositionalPredChecker");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xpath.internal.XPathVisitor"), "superclass com.sun.org.apache.xpath.internal.XPathVisitor");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "check"), "method check");
        check(has(c, "visitFunction"), "method visitFunction");
        check(has(c, "visitPredicate"), "method visitPredicate");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
