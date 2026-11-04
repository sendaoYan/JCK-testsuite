/*
 * @test
 * @summary com.sun.org.apache.xpath.internal.compiler.OpMap conformance: reflection.
 * @run main OpMapTest
 */

public class OpMapTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xpath.internal.compiler.OpMap", false, OpMapTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("OpMap"), "simple name OpMap");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "toString"), "method toString");
        check(has(c, "getPatternString"), "method getPatternString");
        check(has(c, "getTokenQueue"), "method getTokenQueue");
        check(has(c, "getToken"), "method getToken");
        check(has(c, "getTokenQueueSize"), "method getTokenQueueSize");
        check(has(c, "getOpMap"), "method getOpMap");
        check(has(c, "getOp"), "method getOp");
        check(has(c, "setOp"), "method setOp");
        check(has(c, "getNextOpPos"), "method getNextOpPos");
        check(has(c, "getNextStepPos"), "method getNextStepPos");
        check(has(c, "getFirstPredicateOpPos"), "method getFirstPredicateOpPos");
        check(has(c, "error"), "method error");
        check(has(c, "getFirstChildPos"), "method getFirstChildPos");
        check(has(c, "getArgLength"), "method getArgLength");
        check(has(c, "getArgLengthOfStep"), "method getArgLengthOfStep");
        check(has(c, "getFirstChildPosOfStep"), "method getFirstChildPosOfStep");
        check(has(c, "getStepTestType"), "method getStepTestType");
        check(has(c, "getStepNS"), "method getStepNS");
        check(has(c, "getStepLocalName"), "method getStepLocalName");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
