/*
 * @test
 * @summary com.sun.xml.internal.stream.dtd.nonvalidating.DTDGrammar conformance: internal package reflection.
 * @modules java.xml/com.sun.xml.internal.stream.dtd.nonvalidating:+open
 * @run main DTDGrammarTest
 */
public class DTDGrammarTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.stream.dtd.nonvalidating.DTDGrammar", false, DTDGrammarTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.stream.dtd.nonvalidating.DTDGrammar"), "name");
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
        check(fd.size() == 7, "field-count");
        check(fd.contains("TOP_LEVEL_SCOPE"), "field-TOP_LEVEL_SCOPE");
        check(fd.contains("fDTDSource"), "field-fDTDSource");
        check(fd.contains("fDTDContentModelSource"), "field-fDTDContentModelSource");
        check(fd.contains("fCurrentElementIndex"), "field-fCurrentElementIndex");
        check(fd.contains("fCurrentAttributeIndex"), "field-fCurrentAttributeIndex");
        check(fd.contains("fReadingExternalDTD"), "field-fReadingExternalDTD");
        check(fd.contains("fAttributeDecl"), "field-fAttributeDecl");
        check(md.size() == 24, "method-count");
        check(md.contains("attributeDecl"), "method-attributeDecl");
        check(md.contains("createAttributeDecl"), "method-createAttributeDecl");
        check(md.contains("createElementDecl"), "method-createElementDecl");
        check(md.contains("elementDecl"), "method-elementDecl");
        check(md.contains("endDTD"), "method-endDTD");
        check(md.contains("getAttributeDecl"), "method-getAttributeDecl");
        check(md.contains("getAttributeDeclIndex"), "method-getAttributeDeclIndex");
        check(md.contains("getContentSpecType"), "method-getContentSpecType");
        check(md.contains("getElementDecl"), "method-getElementDecl");
        check(md.contains("getElementDeclIndex"), "method-getElementDeclIndex");
        check(md.contains("getFirstAttributeDeclIndex"), "method-getFirstAttributeDeclIndex");
        check(md.contains("getFirstElementDeclIndex"), "method-getFirstElementDeclIndex");
        check(md.contains("getNextAttributeDeclIndex"), "method-getNextAttributeDeclIndex");
        check(md.contains("getNextElementDeclIndex"), "method-getNextElementDeclIndex");
        check(md.contains("getNotationDecls"), "method-getNotationDecls");
        check(md.contains("getSymbolTable"), "method-getSymbolTable");
        check(md.contains("isCDATAAttribute"), "method-isCDATAAttribute");
        check(md.contains("notationDecl"), "method-notationDecl");
        check(md.contains("printAttributes"), "method-printAttributes");
        check(md.contains("printElements"), "method-printElements");
        check(md.contains("setAttributeDecl"), "method-setAttributeDecl");
        check(md.contains("setElementDecl"), "method-setElementDecl");
        check(md.contains("setFirstAttributeDeclIndex"), "method-setFirstAttributeDeclIndex");
        check(md.contains("startDTD"), "method-startDTD");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
