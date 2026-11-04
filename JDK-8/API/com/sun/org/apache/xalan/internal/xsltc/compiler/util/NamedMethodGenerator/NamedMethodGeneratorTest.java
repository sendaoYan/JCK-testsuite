/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.compiler.util.NamedMethodGenerator conformance: reflection.
 * @run main NamedMethodGeneratorTest
 */

public class NamedMethodGeneratorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.compiler.util.NamedMethodGenerator", false, NamedMethodGeneratorTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("NamedMethodGenerator"), "simple name NamedMethodGenerator");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "is final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator"), "superclass com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "getLocalIndex"), "method getLocalIndex");
        check(has(c, "loadParameter"), "method loadParameter");
        check(has(c, "storeParameter"), "method storeParameter");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
