/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.runtime.Constants conformance: internal package reflection.
 * @modules java.xml/com.sun.org.apache.xalan.internal.xsltc.runtime:+open
 * @run main ConstantsTest
 */
public class ConstantsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.runtime.Constants", false, ConstantsTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xalan.internal.xsltc.runtime.Constants"), "name");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() == null, "superclass-null");
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
        check(fd.size() == 14, "field-count");
        check(fd.contains("ANY"), "field-ANY");
        check(fd.contains("ATTRIBUTE"), "field-ATTRIBUTE");
        check(fd.contains("ROOT"), "field-ROOT");
        check(fd.contains("TEXT"), "field-TEXT");
        check(fd.contains("ELEMENT"), "field-ELEMENT");
        check(fd.contains("COMMENT"), "field-COMMENT");
        check(fd.contains("PROCESSING_INSTRUCTION"), "field-PROCESSING_INSTRUCTION");
        check(fd.contains("XSLT_URI"), "field-XSLT_URI");
        check(fd.contains("NAMESPACE_FEATURE"), "field-NAMESPACE_FEATURE");
        check(fd.contains("EMPTYSTRING"), "field-EMPTYSTRING");
        check(fd.contains("XML_PREFIX"), "field-XML_PREFIX");
        check(fd.contains("XMLNS_PREFIX"), "field-XMLNS_PREFIX");
        check(fd.contains("XMLNS_STRING"), "field-XMLNS_STRING");
        check(fd.contains("XMLNS_URI"), "field-XMLNS_URI");
        check(md.size() == 0, "method-count");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
