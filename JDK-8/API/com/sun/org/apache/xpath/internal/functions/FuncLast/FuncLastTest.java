/*
 * @test
 * @summary com.sun.org.apache.xpath.internal.functions.FuncLast conformance: reflection.
 * @run main FuncLastTest
 */

public class FuncLastTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xpath.internal.functions.FuncLast", false, FuncLastTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("FuncLast"), "simple name FuncLast");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xpath.internal.functions.Function"), "superclass com.sun.org.apache.xpath.internal.functions.Function");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "postCompileStep"), "method postCompileStep");
        check(has(c, "getCountOfContextNodeList"), "method getCountOfContextNodeList");
        check(has(c, "execute"), "method execute");
        check(has(c, "fixupVariables"), "method fixupVariables");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
