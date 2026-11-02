/*
 * @test
 * @summary com.sun.xml.internal.stream.events.EntityDeclarationImpl conformance: internal package reflection.
 * @modules java.xml/com.sun.xml.internal.stream.events:+open
 * @run main EntityDeclarationImplTest
 */
public class EntityDeclarationImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.stream.events.EntityDeclarationImpl", false, EntityDeclarationImplTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.stream.events.EntityDeclarationImpl"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.xml.internal.stream.events.DummyEvent"), "superclass");
        java.util.Set<String> ifs = new java.util.HashSet<>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "interface-count");
        check(ifs.contains("javax.xml.stream.events.EntityDeclaration"), "if-javax.xml.stream.events.EntityDeclaration");
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
        check(md.contains("getBaseURI"), "method-getBaseURI");
        check(md.contains("getEntityName"), "method-getEntityName");
        check(md.contains("getName"), "method-getName");
        check(md.contains("getNotationName"), "method-getNotationName");
        check(md.contains("getPublicId"), "method-getPublicId");
        check(md.contains("getReplacementText"), "method-getReplacementText");
        check(md.contains("getSystemId"), "method-getSystemId");
        check(md.contains("getXMLResourceIdentifier"), "method-getXMLResourceIdentifier");
        check(md.contains("init"), "method-init");
        check(md.contains("setEntityName"), "method-setEntityName");
        check(md.contains("setEntityReplacementText"), "method-setEntityReplacementText");
        check(md.contains("setNotationName"), "method-setNotationName");
        check(md.contains("setXMLResourceIdentifier"), "method-setXMLResourceIdentifier");
        check(md.contains("writeAsEncodedUnicodeEx"), "method-writeAsEncodedUnicodeEx");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
