/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.classfile.FieldOrMethod conformance: internal package reflection.
 * @modules java.xml/com.sun.org.apache.bcel.internal.classfile:+open
 * @run main FieldOrMethodTest
 */
public class FieldOrMethodTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.classfile.FieldOrMethod", false, FieldOrMethodTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.bcel.internal.classfile.FieldOrMethod"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.bcel.internal.classfile.AccessFlags"), "superclass");
        java.util.Set<String> ifs = new java.util.HashSet<>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 2, "interface-count");
        check(ifs.contains("java.lang.Cloneable"), "if-java.lang.Cloneable");
        check(ifs.contains("com.sun.org.apache.bcel.internal.classfile.Node"), "if-com.sun.org.apache.bcel.internal.classfile.Node");
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
        check(fd.size() == 5, "field-count");
        check(fd.contains("name_index"), "field-name_index");
        check(fd.contains("signature_index"), "field-signature_index");
        check(fd.contains("attributes"), "field-attributes");
        check(fd.contains("attributes_count"), "field-attributes_count");
        check(fd.contains("constant_pool"), "field-constant_pool");
        check(md.size() == 15, "method-count");
        check(md.contains("copy_"), "method-copy_");
        check(md.contains("dump"), "method-dump");
        check(md.contains("getAnnotationEntries"), "method-getAnnotationEntries");
        check(md.contains("getAttribute"), "method-getAttribute");
        check(md.contains("getAttributes"), "method-getAttributes");
        check(md.contains("getConstantPool"), "method-getConstantPool");
        check(md.contains("getGenericSignature"), "method-getGenericSignature");
        check(md.contains("getName"), "method-getName");
        check(md.contains("getNameIndex"), "method-getNameIndex");
        check(md.contains("getSignature"), "method-getSignature");
        check(md.contains("getSignatureIndex"), "method-getSignatureIndex");
        check(md.contains("setAttributes"), "method-setAttributes");
        check(md.contains("setConstantPool"), "method-setConstantPool");
        check(md.contains("setNameIndex"), "method-setNameIndex");
        check(md.contains("setSignatureIndex"), "method-setSignatureIndex");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
