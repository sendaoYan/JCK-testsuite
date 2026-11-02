/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.classfile.ConstantPool conformance: internal package reflection.
 * @modules java.xml/com.sun.org.apache.bcel.internal.classfile:+open
 * @run main ConstantPoolTest
 */
public class ConstantPoolTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.classfile.ConstantPool", false, ConstantPoolTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.bcel.internal.classfile.ConstantPool"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        java.util.Set<String> ifs = new java.util.HashSet<>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 3, "interface-count");
        check(ifs.contains("java.lang.Cloneable"), "if-java.lang.Cloneable");
        check(ifs.contains("com.sun.org.apache.bcel.internal.classfile.Node"), "if-com.sun.org.apache.bcel.internal.classfile.Node");
        check(ifs.contains("java.lang.Iterable"), "if-java.lang.Iterable");
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
        check(md.contains("accept"), "method-accept");
        check(md.contains("constantToString"), "method-constantToString");
        check(md.contains("copy"), "method-copy");
        check(md.contains("dump"), "method-dump");
        check(md.contains("getConstant"), "method-getConstant");
        check(md.contains("getConstantInteger"), "method-getConstantInteger");
        check(md.contains("getConstantPool"), "method-getConstantPool");
        check(md.contains("getConstantString"), "method-getConstantString");
        check(md.contains("getConstantUtf8"), "method-getConstantUtf8");
        check(md.contains("getLength"), "method-getLength");
        check(md.contains("iterator"), "method-iterator");
        check(md.contains("setConstant"), "method-setConstant");
        check(md.contains("setConstantPool"), "method-setConstantPool");
        check(md.contains("toString"), "method-toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
