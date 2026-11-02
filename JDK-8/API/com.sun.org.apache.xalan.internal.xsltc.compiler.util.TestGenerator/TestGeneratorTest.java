/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.compiler.util.TestGenerator conformance: reflection.
 * @run main TestGeneratorTest
 */

public class TestGeneratorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.compiler.util.TestGenerator", false, TestGeneratorTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("TestGenerator"), "simple name TestGenerator");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "is final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator"), "superclass com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "getHandlerIndex"), "method getHandlerIndex");
        check(has(c, "getIteratorIndex"), "method getIteratorIndex");
        check(has(c, "setDomIndex"), "method setDomIndex");
        check(has(c, "loadDOM"), "method loadDOM");
        check(has(c, "loadCurrentNode"), "method loadCurrentNode");
        check(has(c, "loadContextNode"), "method loadContextNode");
        check(has(c, "storeContextNode"), "method storeContextNode");
        check(has(c, "storeCurrentNode"), "method storeCurrentNode");
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
