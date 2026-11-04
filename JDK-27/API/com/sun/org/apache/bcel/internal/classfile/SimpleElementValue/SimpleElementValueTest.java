/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.classfile.SimpleElementValue conformance: internal package reflection.
 * @modules java.xml/com.sun.org.apache.bcel.internal.classfile:+open
 * @run main SimpleElementValueTest
 */
public class SimpleElementValueTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.classfile.SimpleElementValue", false, SimpleElementValueTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.bcel.internal.classfile.SimpleElementValue"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.bcel.internal.classfile.ElementValue"), "superclass");
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
        check(md.contains("dump"), "method-dump");
        check(md.contains("getIndex"), "method-getIndex");
        check(md.contains("getValueBoolean"), "method-getValueBoolean");
        check(md.contains("getValueByte"), "method-getValueByte");
        check(md.contains("getValueChar"), "method-getValueChar");
        check(md.contains("getValueDouble"), "method-getValueDouble");
        check(md.contains("getValueFloat"), "method-getValueFloat");
        check(md.contains("getValueInt"), "method-getValueInt");
        check(md.contains("getValueLong"), "method-getValueLong");
        check(md.contains("getValueShort"), "method-getValueShort");
        check(md.contains("getValueString"), "method-getValueString");
        check(md.contains("setIndex"), "method-setIndex");
        check(md.contains("stringifyValue"), "method-stringifyValue");
        check(md.contains("toString"), "method-toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
