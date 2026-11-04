/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.classfile.AnnotationEntry conformance: internal package reflection.
 * @modules java.xml/com.sun.org.apache.bcel.internal.classfile:+open
 * @run main AnnotationEntryTest
 */
public class AnnotationEntryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.classfile.AnnotationEntry", false, AnnotationEntryTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.bcel.internal.classfile.AnnotationEntry"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        java.util.Set<String> ifs = new java.util.HashSet<>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "interface-count");
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
        check(fd.size() == 1, "field-count");
        check(fd.contains("EMPTY_ARRAY"), "field-EMPTY_ARRAY");
        check(md.size() == 14, "method-count");
        check(md.contains("accept"), "method-accept");
        check(md.contains("addElementNameValuePair"), "method-addElementNameValuePair");
        check(md.contains("createAnnotationEntries"), "method-createAnnotationEntries");
        check(md.contains("dump"), "method-dump");
        check(md.contains("getAnnotationType"), "method-getAnnotationType");
        check(md.contains("getAnnotationTypeIndex"), "method-getAnnotationTypeIndex");
        check(md.contains("getConstantPool"), "method-getConstantPool");
        check(md.contains("getElementValuePairs"), "method-getElementValuePairs");
        check(md.contains("getNumElementValuePairs"), "method-getNumElementValuePairs");
        check(md.contains("getTypeIndex"), "method-getTypeIndex");
        check(md.contains("isRuntimeVisible"), "method-isRuntimeVisible");
        check(md.contains("read"), "method-read");
        check(md.contains("toShortString"), "method-toShortString");
        check(md.contains("toString"), "method-toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
