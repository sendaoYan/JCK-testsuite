/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.generic.ConstantPoolGen conformance: internal package reflection.
 * @modules java.xml/com.sun.org.apache.bcel.internal.generic:+open
 * @run main ConstantPoolGenTest
 */
public class ConstantPoolGenTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.generic.ConstantPoolGen", false, ConstantPoolGenTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.bcel.internal.generic.ConstantPoolGen"), "name");
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
        check(fd.contains("size"), "field-size");
        check(fd.contains("constants"), "field-constants");
        check(fd.contains("index"), "field-index");
        check(md.size() == 31, "method-count");
        check(md.contains("addArrayClass"), "method-addArrayClass");
        check(md.contains("addClass"), "method-addClass");
        check(md.contains("addConstant"), "method-addConstant");
        check(md.contains("addDouble"), "method-addDouble");
        check(md.contains("addFieldref"), "method-addFieldref");
        check(md.contains("addFloat"), "method-addFloat");
        check(md.contains("addInteger"), "method-addInteger");
        check(md.contains("addInterfaceMethodref"), "method-addInterfaceMethodref");
        check(md.contains("addLong"), "method-addLong");
        check(md.contains("addMethodref"), "method-addMethodref");
        check(md.contains("addNameAndType"), "method-addNameAndType");
        check(md.contains("addString"), "method-addString");
        check(md.contains("addUtf8"), "method-addUtf8");
        check(md.contains("adjustSize"), "method-adjustSize");
        check(md.contains("getConstant"), "method-getConstant");
        check(md.contains("getConstantPool"), "method-getConstantPool");
        check(md.contains("getFinalConstantPool"), "method-getFinalConstantPool");
        check(md.contains("getSize"), "method-getSize");
        check(md.contains("lookupClass"), "method-lookupClass");
        check(md.contains("lookupDouble"), "method-lookupDouble");
        check(md.contains("lookupFieldref"), "method-lookupFieldref");
        check(md.contains("lookupFloat"), "method-lookupFloat");
        check(md.contains("lookupInteger"), "method-lookupInteger");
        check(md.contains("lookupInterfaceMethodref"), "method-lookupInterfaceMethodref");
        check(md.contains("lookupLong"), "method-lookupLong");
        check(md.contains("lookupMethodref"), "method-lookupMethodref");
        check(md.contains("lookupNameAndType"), "method-lookupNameAndType");
        check(md.contains("lookupString"), "method-lookupString");
        check(md.contains("lookupUtf8"), "method-lookupUtf8");
        check(md.contains("setConstant"), "method-setConstant");
        check(md.contains("toString"), "method-toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
