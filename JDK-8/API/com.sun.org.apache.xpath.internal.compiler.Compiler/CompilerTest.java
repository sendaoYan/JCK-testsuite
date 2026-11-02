/*
 * @test
 * @summary com.sun.org.apache.xpath.internal.compiler.Compiler conformance: reflection.
 * @run main CompilerTest
 */

public class CompilerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xpath.internal.compiler.Compiler", false, CompilerTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("Compiler"), "simple name Compiler");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xpath.internal.compiler.OpMap"), "superclass com.sun.org.apache.xpath.internal.compiler.OpMap");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "compileExpression"), "method compileExpression");
        check(has(c, "getLocationPathDepth"), "method getLocationPathDepth");
        check(has(c, "locationPath"), "method locationPath");
        check(has(c, "predicate"), "method predicate");
        check(has(c, "locationPathPattern"), "method locationPathPattern");
        check(has(c, "getWhatToShow"), "method getWhatToShow");
        check(has(c, "getCompiledPredicates"), "method getCompiledPredicates");
        check(has(c, "countPredicates"), "method countPredicates");
        check(has(c, "warn"), "method warn");
        check(has(c, "assertion"), "method assertion");
        check(has(c, "error"), "method error");
        check(has(c, "getNamespaceContext"), "method getNamespaceContext");
        check(has(c, "setNamespaceContext"), "method setNamespaceContext");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
