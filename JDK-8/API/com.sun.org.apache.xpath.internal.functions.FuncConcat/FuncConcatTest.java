/*
 * @test
 * @summary com.sun.org.apache.xpath.internal.functions.FuncConcat conformance: reflection.
 * @run main FuncConcatTest
 */

public class FuncConcatTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xpath.internal.functions.FuncConcat", false, FuncConcatTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("FuncConcat"), "simple name FuncConcat");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xpath.internal.functions.FunctionMultiArgs"), "superclass com.sun.org.apache.xpath.internal.functions.FunctionMultiArgs");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "execute"), "method execute");
        check(has(c, "checkNumberArgs"), "method checkNumberArgs");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
