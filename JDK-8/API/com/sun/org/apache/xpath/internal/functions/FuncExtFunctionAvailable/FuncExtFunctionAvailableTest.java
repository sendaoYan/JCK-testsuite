/*
 * @test
 * @summary com.sun.org.apache.xpath.internal.functions.FuncExtFunctionAvailable conformance: reflection.
 * @run main FuncExtFunctionAvailableTest
 */

public class FuncExtFunctionAvailableTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xpath.internal.functions.FuncExtFunctionAvailable", false, FuncExtFunctionAvailableTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("FuncExtFunctionAvailable"), "simple name FuncExtFunctionAvailable");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xpath.internal.functions.FunctionOneArg"), "superclass com.sun.org.apache.xpath.internal.functions.FunctionOneArg");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "execute"), "method execute");
        check(has(c, "setFunctionTable"), "method setFunctionTable");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
