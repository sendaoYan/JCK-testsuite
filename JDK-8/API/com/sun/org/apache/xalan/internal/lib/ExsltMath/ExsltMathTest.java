/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.lib.ExsltMath conformance: reflection.
 * @run main ExsltMathTest
 */

public class ExsltMathTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.lib.ExsltMath", false, ExsltMathTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("ExsltMath"), "simple name ExsltMath");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xalan.internal.lib.ExsltBase"), "superclass com.sun.org.apache.xalan.internal.lib.ExsltBase");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "max"), "method max");
        check(has(c, "min"), "method min");
        check(has(c, "highest"), "method highest");
        check(has(c, "lowest"), "method lowest");
        check(has(c, "abs"), "method abs");
        check(has(c, "acos"), "method acos");
        check(has(c, "asin"), "method asin");
        check(has(c, "atan"), "method atan");
        check(has(c, "atan2"), "method atan2");
        check(has(c, "cos"), "method cos");
        check(has(c, "exp"), "method exp");
        check(has(c, "log"), "method log");
        check(has(c, "power"), "method power");
        check(has(c, "random"), "method random");
        check(has(c, "sin"), "method sin");
        check(has(c, "sqrt"), "method sqrt");
        check(has(c, "tan"), "method tan");
        check(has(c, "constant"), "method constant");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
