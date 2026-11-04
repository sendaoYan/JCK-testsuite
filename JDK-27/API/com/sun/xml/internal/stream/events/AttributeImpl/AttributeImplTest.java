/*
 * @test
 * @summary com.sun.xml.internal.stream.events.AttributeImpl conformance: internal package reflection.
 * @modules java.xml/com.sun.xml.internal.stream.events:+open
 * @run main AttributeImplTest
 */
public class AttributeImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.stream.events.AttributeImpl", false, AttributeImplTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.stream.events.AttributeImpl"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.xml.internal.stream.events.DummyEvent"), "superclass");
        java.util.Set<String> ifs = new java.util.HashSet<>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "interface-count");
        check(ifs.contains("javax.xml.stream.events.Attribute"), "if-javax.xml.stream.events.Attribute");
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
        check(md.contains("getDTDType"), "method-getDTDType");
        check(md.contains("getName"), "method-getName");
        check(md.contains("getNonNormalizedValue"), "method-getNonNormalizedValue");
        check(md.contains("getValue"), "method-getValue");
        check(md.contains("init"), "method-init");
        check(md.contains("isSpecified"), "method-isSpecified");
        check(md.contains("setAttributeType"), "method-setAttributeType");
        check(md.contains("setName"), "method-setName");
        check(md.contains("setNonNormalizedValue"), "method-setNonNormalizedValue");
        check(md.contains("setSpecified"), "method-setSpecified");
        check(md.contains("setValue"), "method-setValue");
        check(md.contains("toString"), "method-toString");
        check(md.contains("writeAsEncodedUnicodeEx"), "method-writeAsEncodedUnicodeEx");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
