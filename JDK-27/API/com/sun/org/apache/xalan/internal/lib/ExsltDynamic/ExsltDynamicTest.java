/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.lib.ExsltDynamic conformance: internal package reflection.
 * @modules java.xml/com.sun.org.apache.xalan.internal.lib:+open
 * @run main ExsltDynamicTest
 */
public class ExsltDynamicTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.lib.ExsltDynamic", false, ExsltDynamicTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xalan.internal.lib.ExsltDynamic"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xalan.internal.lib.ExsltBase"), "superclass");
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
        check(fd.size() == 1, "field-count");
        check(fd.contains("EXSL_URI"), "field-EXSL_URI");
        check(md.size() == 6, "method-count");
        check(md.contains("closure"), "method-closure");
        check(md.contains("evaluate"), "method-evaluate");
        check(md.contains("map"), "method-map");
        check(md.contains("max"), "method-max");
        check(md.contains("min"), "method-min");
        check(md.contains("sum"), "method-sum");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
