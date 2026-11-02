/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.compiler.XPathParser conformance: internal package reflection.
 * @modules java.xml/com.sun.org.apache.xalan.internal.xsltc.compiler:+open
 * @run main XPathParserTest
 */
public class XPathParserTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.compiler.XPathParser", false, XPathParserTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xalan.internal.xsltc.compiler.XPathParser"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.java_cup.internal.runtime.lr_parser"), "superclass");
        java.util.Set<String> ifs = new java.util.HashSet<>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 0, "interface-count");
        java.util.Set<String> ec = new java.util.HashSet<>();
        java.util.Set<String> fd = new java.util.HashSet<>();
        java.util.Set<String> md = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) {
            if (f.isEnumConstant()) ec.add(f.getName());
            else if (java.lang.reflect.Modifier.isPublic(f.getModifiers()) || java.lang.reflect.Modifier.isProtected(f.getModifiers())) fd.add(f.getName());
        }
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (java.lang.reflect.Modifier.isPublic(m.getModifiers()) || java.lang.reflect.Modifier.isProtected(m.getModifiers())) md.add(m.getName());
        }
        check(ec.size() == 0, "enum-constant-count");
        check(fd.size() == 7, "field-count");
        check(fd.contains("_production_table"), "field-_production_table");
        check(fd.contains("_action_table"), "field-_action_table");
        check(fd.contains("_reduce_table"), "field-_reduce_table");
        check(fd.contains("action_obj"), "field-action_obj");
        check(fd.contains("EmptyArgs"), "field-EmptyArgs");
        check(fd.contains("DummyVarRef"), "field-DummyVarRef");
        check(fd.contains("_symbolTable"), "field-_symbolTable");
        check(md.size() == 23, "method-count");
        check(md.contains("EOF_sym"), "method-EOF_sym");
        check(md.contains("action_table"), "method-action_table");
        check(md.contains("addError"), "method-addError");
        check(md.contains("createStepPattern"), "method-createStepPattern");
        check(md.contains("do_action"), "method-do_action");
        check(md.contains("error_sym"), "method-error_sym");
        check(md.contains("findNodeType"), "method-findNodeType");
        check(md.contains("getLineNumber"), "method-getLineNumber");
        check(md.contains("getQName"), "method-getQName");
        check(md.contains("getQNameIgnoreDefaultNs"), "method-getQNameIgnoreDefaultNs");
        check(md.contains("init_actions"), "method-init_actions");
        check(md.contains("insertStep"), "method-insertStep");
        check(md.contains("isElementAxis"), "method-isElementAxis");
        check(md.contains("parse"), "method-parse");
        check(md.contains("production_table"), "method-production_table");
        check(md.contains("reduce_table"), "method-reduce_table");
        check(md.contains("report_error"), "method-report_error");
        check(md.contains("report_fatal_error"), "method-report_fatal_error");
        check(md.contains("setCallsNodeset"), "method-setCallsNodeset");
        check(md.contains("setHasIdCall"), "method-setHasIdCall");
        check(md.contains("setMultiDocument"), "method-setMultiDocument");
        check(md.contains("start_production"), "method-start_production");
        check(md.contains("start_state"), "method-start_state");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
