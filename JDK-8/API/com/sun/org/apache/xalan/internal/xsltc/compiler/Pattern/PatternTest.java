/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.compiler.Pattern conformance: reflection.
 * @run main PatternTest
 */

public class PatternTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.compiler.Pattern", false, PatternTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("Pattern"), "simple name Pattern");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xalan.internal.xsltc.compiler.Expression"), "superclass com.sun.org.apache.xalan.internal.xsltc.compiler.Expression");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "typeCheck"), "method typeCheck");
        check(has(c, "translate"), "method translate");
        check(has(c, "getPriority"), "method getPriority");
        check(has(c, "lookupPrimop"), "method lookupPrimop");
        check(has(c, "backPatchTrueList"), "method backPatchTrueList");
        check(has(c, "backPatchFalseList"), "method backPatchFalseList");
        check(has(c, "getTrueList"), "method getTrueList");
        check(has(c, "getFalseList"), "method getFalseList");
        check(has(c, "desynthesize"), "method desynthesize");
        check(has(c, "synthesize"), "method synthesize");
        check(has(c, "startIterator"), "method startIterator");
        check(has(c, "translateDesynthesized"), "method translateDesynthesized");
        check(has(c, "evaluateAtCompileTime"), "method evaluateAtCompileTime");
        check(has(c, "hasLastCall"), "method hasLastCall");
        check(has(c, "hasPositionCall"), "method hasPositionCall");
        check(has(c, "getType"), "method getType");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
