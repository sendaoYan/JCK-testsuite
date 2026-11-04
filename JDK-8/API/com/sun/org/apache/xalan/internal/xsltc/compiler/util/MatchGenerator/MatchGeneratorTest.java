/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.compiler.util.MatchGenerator conformance: reflection.
 * @run main MatchGeneratorTest
 */

public class MatchGeneratorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.compiler.util.MatchGenerator", false, MatchGeneratorTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("MatchGenerator"), "simple name MatchGenerator");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "is final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator"), "superclass com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "loadCurrentNode"), "method loadCurrentNode");
        check(has(c, "storeCurrentNode"), "method storeCurrentNode");
        check(has(c, "getHandlerIndex"), "method getHandlerIndex");
        check(has(c, "loadDOM"), "method loadDOM");
        check(has(c, "setDomIndex"), "method setDomIndex");
        check(has(c, "getIteratorIndex"), "method getIteratorIndex");
        check(has(c, "setIteratorIndex"), "method setIteratorIndex");
        check(has(c, "getLocalIndex"), "method getLocalIndex");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
