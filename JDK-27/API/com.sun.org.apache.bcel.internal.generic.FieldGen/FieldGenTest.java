/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.generic.FieldGen conformance: internal package reflection.
 * @modules java.xml/com.sun.org.apache.bcel.internal.generic:+open
 * @run main FieldGenTest
 */
public class FieldGenTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.generic.FieldGen", false, FieldGenTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.bcel.internal.generic.FieldGen"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.bcel.internal.generic.FieldGenOrMethodGen"), "superclass");
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
        check(md.size() == 14, "method-count");
        check(md.contains("addObserver"), "method-addObserver");
        check(md.contains("cancelInitValue"), "method-cancelInitValue");
        check(md.contains("copy"), "method-copy");
        check(md.contains("equals"), "method-equals");
        check(md.contains("getComparator"), "method-getComparator");
        check(md.contains("getField"), "method-getField");
        check(md.contains("getInitValue"), "method-getInitValue");
        check(md.contains("getSignature"), "method-getSignature");
        check(md.contains("hashCode"), "method-hashCode");
        check(md.contains("removeObserver"), "method-removeObserver");
        check(md.contains("setComparator"), "method-setComparator");
        check(md.contains("setInitValue"), "method-setInitValue");
        check(md.contains("toString"), "method-toString");
        check(md.contains("update"), "method-update");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
