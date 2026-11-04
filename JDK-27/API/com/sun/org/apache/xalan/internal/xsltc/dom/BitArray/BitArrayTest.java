/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.dom.BitArray conformance: internal package reflection.
 * @modules java.xml/com.sun.org.apache.xalan.internal.xsltc.dom:+open
 * @run main BitArrayTest
 */
public class BitArrayTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.dom.BitArray", false, BitArrayTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xalan.internal.xsltc.dom.BitArray"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        java.util.Set<String> ifs = new java.util.HashSet<>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "interface-count");
        check(ifs.contains("java.io.Externalizable"), "if-java.io.Externalizable");
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
        check(md.size() == 13, "method-count");
        check(md.contains("cloneArray"), "method-cloneArray");
        check(md.contains("data"), "method-data");
        check(md.contains("getBit"), "method-getBit");
        check(md.contains("getBitNumber"), "method-getBitNumber");
        check(md.contains("getMask"), "method-getMask");
        check(md.contains("getNextBit"), "method-getNextBit");
        check(md.contains("merge"), "method-merge");
        check(md.contains("readExternal"), "method-readExternal");
        check(md.contains("resize"), "method-resize");
        check(md.contains("setBit"), "method-setBit");
        check(md.contains("setMask"), "method-setMask");
        check(md.contains("size"), "method-size");
        check(md.contains("writeExternal"), "method-writeExternal");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
