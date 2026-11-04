/*
 * @test
 * @summary com.sun.org.apache.xpath.internal.functions.FunctionMultiArgs conformance: reflection.
 * @run main FunctionMultiArgsTest
 */

public class FunctionMultiArgsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xpath.internal.functions.FunctionMultiArgs", false, FunctionMultiArgsTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("FunctionMultiArgs"), "simple name FunctionMultiArgs");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xpath.internal.functions.Function3Args"), "superclass com.sun.org.apache.xpath.internal.functions.Function3Args");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "getArgs"), "method getArgs");
        check(has(c, "setArg"), "method setArg");
        check(has(c, "fixupVariables"), "method fixupVariables");
        check(has(c, "checkNumberArgs"), "method checkNumberArgs");
        check(has(c, "canTraverseOutsideSubtree"), "method canTraverseOutsideSubtree");
        check(has(c, "callArgVisitors"), "method callArgVisitors");
        check(has(c, "deepEquals"), "method deepEquals");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
