/*
 * @test
 * @summary com.sun.org.apache.xpath.internal.axes.ContextNodeList conformance: reflection.
 * @run main ContextNodeListTest
 */

public class ContextNodeListTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xpath.internal.axes.ContextNodeList", false, ContextNodeListTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("ContextNodeList"), "simple name ContextNodeList");
        check(c.isInterface(), "is interface");
        check(!c.isEnum(), "not enum");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "getCurrentNode"), "method getCurrentNode");
        check(has(c, "getCurrentPos"), "method getCurrentPos");
        check(has(c, "reset"), "method reset");
        check(has(c, "setShouldCacheNodes"), "method setShouldCacheNodes");
        check(has(c, "runTo"), "method runTo");
        check(has(c, "setCurrentPos"), "method setCurrentPos");
        check(has(c, "size"), "method size");
        check(has(c, "isFresh"), "method isFresh");
        check(has(c, "cloneWithReset"), "method cloneWithReset");
        check(has(c, "clone"), "method clone");
        check(has(c, "getLast"), "method getLast");
        check(has(c, "setLast"), "method setLast");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
