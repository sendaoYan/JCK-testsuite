/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.compiler.Template conformance: internal package reflection.
 * @modules java.xml/com.sun.org.apache.xalan.internal.xsltc.compiler:+open
 * @run main TemplateTest
 */
public class TemplateTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.compiler.Template", false, TemplateTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xalan.internal.xsltc.compiler.Template"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xalan.internal.xsltc.compiler.TopLevelElement"), "superclass");
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
        check(md.size() == 25, "method-count");
        check(md.contains("addDependency"), "method-addDependency");
        check(md.contains("addParameter"), "method-addParameter");
        check(md.contains("compareTo"), "method-compareTo");
        check(md.contains("compile"), "method-compile");
        check(md.contains("disable"), "method-disable");
        check(md.contains("disabled"), "method-disabled");
        check(md.contains("display"), "method-display");
        check(md.contains("getDependencies"), "method-getDependencies");
        check(md.contains("getModeName"), "method-getModeName");
        check(md.contains("getName"), "method-getName");
        check(md.contains("getParameters"), "method-getParameters");
        check(md.contains("getPattern"), "method-getPattern");
        check(md.contains("getPosition"), "method-getPosition");
        check(md.contains("getPriority"), "method-getPriority");
        check(md.contains("getStylesheet"), "method-getStylesheet");
        check(md.contains("hasParams"), "method-hasParams");
        check(md.contains("isNamed"), "method-isNamed");
        check(md.contains("isSimpleNamedTemplate"), "method-isSimpleNamedTemplate");
        check(md.contains("isSimplified"), "method-isSimplified");
        check(md.contains("parseContents"), "method-parseContents");
        check(md.contains("parseSimplified"), "method-parseSimplified");
        check(md.contains("setName"), "method-setName");
        check(md.contains("setSimplified"), "method-setSimplified");
        check(md.contains("translate"), "method-translate");
        check(md.contains("typeCheck"), "method-typeCheck");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
