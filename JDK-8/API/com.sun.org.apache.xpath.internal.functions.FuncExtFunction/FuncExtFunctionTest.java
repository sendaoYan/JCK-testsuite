/*
 * @test
 * @summary com.sun.org.apache.xpath.internal.functions.FuncExtFunction conformance: reflection.
 * @run main FuncExtFunctionTest
 */

public class FuncExtFunctionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xpath.internal.functions.FuncExtFunction", false, FuncExtFunctionTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("FuncExtFunction"), "simple name FuncExtFunction");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xpath.internal.functions.Function"), "superclass com.sun.org.apache.xpath.internal.functions.Function");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "fixupVariables"), "method fixupVariables");
        check(has(c, "getNamespace"), "method getNamespace");
        check(has(c, "getFunctionName"), "method getFunctionName");
        check(has(c, "getMethodKey"), "method getMethodKey");
        check(has(c, "getArg"), "method getArg");
        check(has(c, "getArgCount"), "method getArgCount");
        check(has(c, "execute"), "method execute");
        check(has(c, "setArg"), "method setArg");
        check(has(c, "checkNumberArgs"), "method checkNumberArgs");
        check(has(c, "callArgVisitors"), "method callArgVisitors");
        check(has(c, "exprSetParent"), "method exprSetParent");
        check(has(c, "toString"), "method toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
