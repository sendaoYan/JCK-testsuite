/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.compiler.util.FilterGenerator conformance: reflection.
 * @run main FilterGeneratorTest
 */

public class FilterGeneratorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.compiler.util.FilterGenerator", false, FilterGeneratorTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("FilterGenerator"), "simple name FilterGenerator");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "is final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator"), "superclass com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "loadTranslet"), "method loadTranslet");
        check(has(c, "isExternal"), "method isExternal");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
