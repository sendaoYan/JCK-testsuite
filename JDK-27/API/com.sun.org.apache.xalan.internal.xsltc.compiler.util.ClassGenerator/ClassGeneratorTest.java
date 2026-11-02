/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator conformance: internal package reflection.
 * @modules java.xml/com.sun.org.apache.xalan.internal.xsltc.compiler.util:+open
 * @run main ClassGeneratorTest
 */
public class ClassGeneratorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator", false, ClassGeneratorTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.bcel.internal.generic.ClassGen"), "superclass");
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
        check(fd.size() == 2, "field-count");
        check(fd.contains("TRANSLET_INDEX"), "field-TRANSLET_INDEX");
        check(fd.contains("INVALID_INDEX"), "field-INVALID_INDEX");
        check(md.size() == 10, "method-count");
        check(md.contains("addMethod"), "method-addMethod");
        check(md.contains("getApplyTemplatesSig"), "method-getApplyTemplatesSig");
        check(md.contains("getApplyTemplatesSigForImport"), "method-getApplyTemplatesSigForImport");
        check(md.contains("getClassName"), "method-getClassName");
        check(md.contains("getDOMClass"), "method-getDOMClass");
        check(md.contains("getDOMClassSig"), "method-getDOMClassSig");
        check(md.contains("getParser"), "method-getParser");
        check(md.contains("getStylesheet"), "method-getStylesheet");
        check(md.contains("isExternal"), "method-isExternal");
        check(md.contains("loadTranslet"), "method-loadTranslet");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
