/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.util.SyntheticRepository conformance: internal package reflection.
 * @modules java.xml/com.sun.org.apache.bcel.internal.util:+open
 * @run main SyntheticRepositoryTest
 */
public class SyntheticRepositoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.util.SyntheticRepository", false, SyntheticRepositoryTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.bcel.internal.util.SyntheticRepository"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        java.util.Set<String> ifs = new java.util.HashSet<>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "interface-count");
        check(ifs.contains("com.sun.org.apache.bcel.internal.util.Repository"), "if-com.sun.org.apache.bcel.internal.util.Repository");
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
        check(md.size() == 6, "method-count");
        check(md.contains("clear"), "method-clear");
        check(md.contains("findClass"), "method-findClass");
        check(md.contains("getInstance"), "method-getInstance");
        check(md.contains("loadClass"), "method-loadClass");
        check(md.contains("removeClass"), "method-removeClass");
        check(md.contains("storeClass"), "method-storeClass");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
