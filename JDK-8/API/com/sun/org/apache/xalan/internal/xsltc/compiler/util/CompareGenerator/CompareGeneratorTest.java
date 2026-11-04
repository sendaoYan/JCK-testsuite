/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.compiler.util.CompareGenerator conformance: reflection.
 * @run main CompareGeneratorTest
 */

public class CompareGeneratorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.compiler.util.CompareGenerator", false, CompareGeneratorTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("CompareGenerator"), "simple name CompareGenerator");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "is final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator"), "superclass com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "loadLastNode"), "method loadLastNode");
        check(has(c, "loadCurrentNode"), "method loadCurrentNode");
        check(has(c, "storeCurrentNode"), "method storeCurrentNode");
        check(has(c, "loadDOM"), "method loadDOM");
        check(has(c, "getHandlerIndex"), "method getHandlerIndex");
        check(has(c, "getIteratorIndex"), "method getIteratorIndex");
        check(has(c, "storeIterator"), "method storeIterator");
        check(has(c, "loadIterator"), "method loadIterator");
        check(has(c, "getLocalIndex"), "method getLocalIndex");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
