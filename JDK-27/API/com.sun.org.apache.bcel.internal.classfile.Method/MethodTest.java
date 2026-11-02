/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.classfile.Method conformance: internal package reflection.
 * @modules java.xml/com.sun.org.apache.bcel.internal.classfile:+open
 * @run main MethodTest
 */
public class MethodTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.classfile.Method", false, MethodTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.bcel.internal.classfile.Method"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.bcel.internal.classfile.FieldOrMethod"), "superclass");
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
        check(fd.contains("EMPTY_ARRAY"), "field-EMPTY_ARRAY");
        check(md.size() == 15, "method-count");
        check(md.contains("accept"), "method-accept");
        check(md.contains("copy"), "method-copy");
        check(md.contains("equals"), "method-equals");
        check(md.contains("getArgumentTypes"), "method-getArgumentTypes");
        check(md.contains("getCode"), "method-getCode");
        check(md.contains("getComparator"), "method-getComparator");
        check(md.contains("getExceptionTable"), "method-getExceptionTable");
        check(md.contains("getLineNumberTable"), "method-getLineNumberTable");
        check(md.contains("getLocalVariableTable"), "method-getLocalVariableTable");
        check(md.contains("getLocalVariableTypeTable"), "method-getLocalVariableTypeTable");
        check(md.contains("getParameterAnnotationEntries"), "method-getParameterAnnotationEntries");
        check(md.contains("getReturnType"), "method-getReturnType");
        check(md.contains("hashCode"), "method-hashCode");
        check(md.contains("setComparator"), "method-setComparator");
        check(md.contains("toString"), "method-toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
