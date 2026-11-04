/*
 * @test
 * @summary com.sun.java_cup.internal.runtime.virtual_parse_stack conformance: internal package reflection.
 * @modules java.xml/com.sun.java_cup.internal.runtime:+open
 * @run main virtual_parse_stackTest
 */
public class virtual_parse_stackTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.java_cup.internal.runtime.virtual_parse_stack", false, virtual_parse_stackTest.class.getClassLoader());
        check(c.getName().equals("com.sun.java_cup.internal.runtime.virtual_parse_stack"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
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
        check(fd.size() == 3, "field-count");
        check(fd.contains("real_stack"), "field-real_stack");
        check(fd.contains("real_next"), "field-real_next");
        check(fd.contains("vstack"), "field-vstack");
        check(md.size() == 5, "method-count");
        check(md.contains("empty"), "method-empty");
        check(md.contains("get_from_real"), "method-get_from_real");
        check(md.contains("pop"), "method-pop");
        check(md.contains("push"), "method-push");
        check(md.contains("top"), "method-top");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
