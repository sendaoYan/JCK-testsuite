/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError conformance: reflection.
 * @run main TypeCheckErrorTest
 */

public class TypeCheckErrorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError", false, TypeCheckErrorTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("TypeCheckError"), "simple name TypeCheckError");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Exception"), "superclass java.lang.Exception");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "getErrorMsg"), "method getErrorMsg");
        check(has(c, "getMessage"), "method getMessage");
        check(has(c, "toString"), "method toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
