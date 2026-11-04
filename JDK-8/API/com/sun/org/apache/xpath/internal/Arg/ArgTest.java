/*
 * @test
 * @summary com.sun.org.apache.xpath.internal.Arg conformance: reflection.
 * @run main ArgTest
 */

public class ArgTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xpath.internal.Arg", false, ArgTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("Arg"), "simple name Arg");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "getQName"), "method getQName");
        check(has(c, "setQName"), "method setQName");
        check(has(c, "getVal"), "method getVal");
        check(has(c, "setVal"), "method setVal");
        check(has(c, "detach"), "method detach");
        check(has(c, "getExpression"), "method getExpression");
        check(has(c, "setExpression"), "method setExpression");
        check(has(c, "isFromWithParam"), "method isFromWithParam");
        check(has(c, "isVisible"), "method isVisible");
        check(has(c, "setIsVisible"), "method setIsVisible");
        check(has(c, "hashCode"), "method hashCode");
        check(has(c, "equals"), "method equals");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
