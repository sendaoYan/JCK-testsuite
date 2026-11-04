/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.compiler.XPathParser conformance: reflection.
 * @run main XPathParserTest
 */

public class XPathParserTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.compiler.XPathParser", false, XPathParserTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("XPathParser"), "simple name XPathParser");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.java_cup.internal.runtime.lr_parser"), "superclass com.sun.java_cup.internal.runtime.lr_parser");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "production_table"), "method production_table");
        check(has(c, "action_table"), "method action_table");
        check(has(c, "reduce_table"), "method reduce_table");
        check(has(c, "do_action"), "method do_action");
        check(has(c, "start_state"), "method start_state");
        check(has(c, "start_production"), "method start_production");
        check(has(c, "EOF_sym"), "method EOF_sym");
        check(has(c, "error_sym"), "method error_sym");
        check(has(c, "getLineNumber"), "method getLineNumber");
        check(has(c, "getQNameIgnoreDefaultNs"), "method getQNameIgnoreDefaultNs");
        check(has(c, "getQName"), "method getQName");
        check(has(c, "setMultiDocument"), "method setMultiDocument");
        check(has(c, "setCallsNodeset"), "method setCallsNodeset");
        check(has(c, "setHasIdCall"), "method setHasIdCall");
        check(has(c, "createStepPattern"), "method createStepPattern");
        check(has(c, "findNodeType"), "method findNodeType");
        check(has(c, "parse"), "method parse");
        check(has(c, "addError"), "method addError");
        check(has(c, "report_error"), "method report_error");
        check(has(c, "report_fatal_error"), "method report_fatal_error");
        check(has(c, "insertStep"), "method insertStep");
        check(has(c, "isElementAxis"), "method isElementAxis");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
