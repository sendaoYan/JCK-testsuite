/*
 * @test
 * @summary com.sun.org.apache.xpath.internal.functions.Function conformance: reflection.
 * @run main FunctionTest
 */

public class FunctionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xpath.internal.functions.Function", false, FunctionTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("Function"), "simple name Function");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xpath.internal.Expression"), "superclass com.sun.org.apache.xpath.internal.Expression");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "setArg"), "method setArg");
        check(has(c, "checkNumberArgs"), "method checkNumberArgs");
        check(has(c, "execute"), "method execute");
        check(has(c, "callArgVisitors"), "method callArgVisitors");
        check(has(c, "callVisitors"), "method callVisitors");
        check(has(c, "deepEquals"), "method deepEquals");
        check(has(c, "postCompileStep"), "method postCompileStep");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
