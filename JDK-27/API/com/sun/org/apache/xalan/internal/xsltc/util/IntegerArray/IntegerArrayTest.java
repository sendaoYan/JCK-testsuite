/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.util.IntegerArray conformance: internal package reflection.
 * @modules java.xml/com.sun.org.apache.xalan.internal.xsltc.util:+open
 * @run main IntegerArrayTest
 */
public class IntegerArrayTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.util.IntegerArray", false, IntegerArrayTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xalan.internal.xsltc.util.IntegerArray"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
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
        check(md.size() == 17, "method-count");
        check(md.contains("add"), "method-add");
        check(md.contains("addNew"), "method-addNew");
        check(md.contains("at"), "method-at");
        check(md.contains("cardinality"), "method-cardinality");
        check(md.contains("clear"), "method-clear");
        check(md.contains("clone"), "method-clone");
        check(md.contains("indexOf"), "method-indexOf");
        check(md.contains("last"), "method-last");
        check(md.contains("merge"), "method-merge");
        check(md.contains("pop"), "method-pop");
        check(md.contains("popLast"), "method-popLast");
        check(md.contains("print"), "method-print");
        check(md.contains("reverse"), "method-reverse");
        check(md.contains("set"), "method-set");
        check(md.contains("setLast"), "method-setLast");
        check(md.contains("sort"), "method-sort");
        check(md.contains("toIntArray"), "method-toIntArray");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
