/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.generic.FieldGenOrMethodGen conformance: internal package reflection.
 * @modules java.xml/com.sun.org.apache.bcel.internal.generic:+open
 * @run main FieldGenOrMethodGenTest
 */
public class FieldGenOrMethodGenTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.generic.FieldGenOrMethodGen", false, FieldGenOrMethodGenTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.bcel.internal.generic.FieldGenOrMethodGen"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.bcel.internal.classfile.AccessFlags"), "superclass");
        java.util.Set<String> ifs = new java.util.HashSet<>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 2, "interface-count");
        check(ifs.contains("com.sun.org.apache.bcel.internal.generic.NamedAndTyped"), "if-com.sun.org.apache.bcel.internal.generic.NamedAndTyped");
        check(ifs.contains("java.lang.Cloneable"), "if-java.lang.Cloneable");
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
        check(fd.contains("name"), "field-name");
        check(fd.contains("type"), "field-type");
        check(fd.contains("cp"), "field-cp");
        check(md.size() == 17, "method-count");
        check(md.contains("addAll"), "method-addAll");
        check(md.contains("addAnnotationEntry"), "method-addAnnotationEntry");
        check(md.contains("addAttribute"), "method-addAttribute");
        check(md.contains("clone"), "method-clone");
        check(md.contains("getAnnotationEntries"), "method-getAnnotationEntries");
        check(md.contains("getAttributes"), "method-getAttributes");
        check(md.contains("getConstantPool"), "method-getConstantPool");
        check(md.contains("getName"), "method-getName");
        check(md.contains("getSignature"), "method-getSignature");
        check(md.contains("getType"), "method-getType");
        check(md.contains("removeAnnotationEntries"), "method-removeAnnotationEntries");
        check(md.contains("removeAnnotationEntry"), "method-removeAnnotationEntry");
        check(md.contains("removeAttribute"), "method-removeAttribute");
        check(md.contains("removeAttributes"), "method-removeAttributes");
        check(md.contains("setConstantPool"), "method-setConstantPool");
        check(md.contains("setName"), "method-setName");
        check(md.contains("setType"), "method-setType");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
