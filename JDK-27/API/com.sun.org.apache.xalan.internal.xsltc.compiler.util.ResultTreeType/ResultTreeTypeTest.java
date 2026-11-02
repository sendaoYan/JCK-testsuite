/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.compiler.util.ResultTreeType conformance: internal package reflection.
 * @modules java.xml/com.sun.org.apache.xalan.internal.xsltc.compiler.util:+open
 * @run main ResultTreeTypeTest
 */
public class ResultTreeTypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.compiler.util.ResultTreeType", false, ResultTreeTypeTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xalan.internal.xsltc.compiler.util.ResultTreeType"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type"), "superclass");
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
        check(md.size() == 13, "method-count");
        check(md.contains("LOAD"), "method-LOAD");
        check(md.contains("STORE"), "method-STORE");
        check(md.contains("getClassName"), "method-getClassName");
        check(md.contains("getMethodName"), "method-getMethodName");
        check(md.contains("identicalTo"), "method-identicalTo");
        check(md.contains("implementedAsMethod"), "method-implementedAsMethod");
        check(md.contains("toJCType"), "method-toJCType");
        check(md.contains("toSignature"), "method-toSignature");
        check(md.contains("toString"), "method-toString");
        check(md.contains("translateBox"), "method-translateBox");
        check(md.contains("translateTo"), "method-translateTo");
        check(md.contains("translateToDesynthesized"), "method-translateToDesynthesized");
        check(md.contains("translateUnBox"), "method-translateUnBox");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
