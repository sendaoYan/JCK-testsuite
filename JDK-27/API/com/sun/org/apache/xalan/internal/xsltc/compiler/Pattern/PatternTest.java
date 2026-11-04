/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.compiler.Pattern conformance: internal package reflection.
 * @modules java.xml/com.sun.org.apache.xalan.internal.xsltc.compiler:+open
 * @run main PatternTest
 */
public class PatternTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.compiler.Pattern", false, PatternTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xalan.internal.xsltc.compiler.Pattern"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xalan.internal.xsltc.compiler.Expression"), "superclass");
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
        check(fd.size() == 0, "field-count");
        check(md.size() == 16, "method-count");
        check(md.contains("backPatchFalseList"), "method-backPatchFalseList");
        check(md.contains("backPatchTrueList"), "method-backPatchTrueList");
        check(md.contains("desynthesize"), "method-desynthesize");
        check(md.contains("evaluateAtCompileTime"), "method-evaluateAtCompileTime");
        check(md.contains("getFalseList"), "method-getFalseList");
        check(md.contains("getPriority"), "method-getPriority");
        check(md.contains("getTrueList"), "method-getTrueList");
        check(md.contains("getType"), "method-getType");
        check(md.contains("hasLastCall"), "method-hasLastCall");
        check(md.contains("hasPositionCall"), "method-hasPositionCall");
        check(md.contains("lookupPrimop"), "method-lookupPrimop");
        check(md.contains("startIterator"), "method-startIterator");
        check(md.contains("synthesize"), "method-synthesize");
        check(md.contains("translate"), "method-translate");
        check(md.contains("translateDesynthesized"), "method-translateDesynthesized");
        check(md.contains("typeCheck"), "method-typeCheck");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
