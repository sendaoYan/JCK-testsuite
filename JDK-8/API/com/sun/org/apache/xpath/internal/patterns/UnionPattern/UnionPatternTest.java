/*
 * @test
 * @summary com.sun.org.apache.xpath.internal.patterns.UnionPattern conformance: reflection.
 * @run main UnionPatternTest
 */

public class UnionPatternTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xpath.internal.patterns.UnionPattern", false, UnionPatternTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("UnionPattern"), "simple name UnionPattern");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xpath.internal.Expression"), "superclass com.sun.org.apache.xpath.internal.Expression");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "fixupVariables"), "method fixupVariables");
        check(has(c, "canTraverseOutsideSubtree"), "method canTraverseOutsideSubtree");
        check(has(c, "setPatterns"), "method setPatterns");
        check(has(c, "getPatterns"), "method getPatterns");
        check(has(c, "execute"), "method execute");
        check(has(c, "callVisitors"), "method callVisitors");
        check(has(c, "deepEquals"), "method deepEquals");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
