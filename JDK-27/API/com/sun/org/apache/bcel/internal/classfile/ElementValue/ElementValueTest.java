/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.classfile.ElementValue conformance: internal package reflection.
 * @modules java.xml/com.sun.org.apache.bcel.internal.classfile:+open
 * @run main ElementValueTest
 */
public class ElementValueTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.classfile.ElementValue", false, ElementValueTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.bcel.internal.classfile.ElementValue"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
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
        check(fd.size() == 15, "field-count");
        check(fd.contains("STRING"), "field-STRING");
        check(fd.contains("ENUM_CONSTANT"), "field-ENUM_CONSTANT");
        check(fd.contains("CLASS"), "field-CLASS");
        check(fd.contains("ANNOTATION"), "field-ANNOTATION");
        check(fd.contains("ARRAY"), "field-ARRAY");
        check(fd.contains("PRIMITIVE_INT"), "field-PRIMITIVE_INT");
        check(fd.contains("PRIMITIVE_BYTE"), "field-PRIMITIVE_BYTE");
        check(fd.contains("PRIMITIVE_CHAR"), "field-PRIMITIVE_CHAR");
        check(fd.contains("PRIMITIVE_DOUBLE"), "field-PRIMITIVE_DOUBLE");
        check(fd.contains("PRIMITIVE_FLOAT"), "field-PRIMITIVE_FLOAT");
        check(fd.contains("PRIMITIVE_LONG"), "field-PRIMITIVE_LONG");
        check(fd.contains("PRIMITIVE_SHORT"), "field-PRIMITIVE_SHORT");
        check(fd.contains("PRIMITIVE_BOOLEAN"), "field-PRIMITIVE_BOOLEAN");
        check(fd.contains("type"), "field-type");
        check(fd.contains("cpool"), "field-cpool");
        check(md.size() == 6, "method-count");
        check(md.contains("dump"), "method-dump");
        check(md.contains("getElementValueType"), "method-getElementValueType");
        check(md.contains("readElementValue"), "method-readElementValue");
        check(md.contains("stringifyValue"), "method-stringifyValue");
        check(md.contains("toShortString"), "method-toShortString");
        check(md.contains("toString"), "method-toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
