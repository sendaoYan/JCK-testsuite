/*
 * @test
 * @summary com.sun.xml.internal.stream.dtd.DTDGrammarUtil conformance: internal package reflection.
 * @modules java.xml/com.sun.xml.internal.stream.dtd:+open
 * @run main DTDGrammarUtilTest
 */
public class DTDGrammarUtilTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.stream.dtd.DTDGrammarUtil", false, DTDGrammarUtilTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.stream.dtd.DTDGrammarUtil"), "name");
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
        check(fd.size() == 5, "field-count");
        check(fd.contains("SYMBOL_TABLE"), "field-SYMBOL_TABLE");
        check(fd.contains("NAMESPACES"), "field-NAMESPACES");
        check(fd.contains("fDTDGrammar"), "field-fDTDGrammar");
        check(fd.contains("fNamespaces"), "field-fNamespaces");
        check(fd.contains("fSymbolTable"), "field-fSymbolTable");
        check(md.size() == 10, "method-count");
        check(md.contains("addDTDDefaultAttrs"), "method-addDTDDefaultAttrs");
        check(md.contains("endCDATA"), "method-endCDATA");
        check(md.contains("endElement"), "method-endElement");
        check(md.contains("handleEndElement"), "method-handleEndElement");
        check(md.contains("handleStartElement"), "method-handleStartElement");
        check(md.contains("isIgnorableWhiteSpace"), "method-isIgnorableWhiteSpace");
        check(md.contains("isInElementContent"), "method-isInElementContent");
        check(md.contains("reset"), "method-reset");
        check(md.contains("startCDATA"), "method-startCDATA");
        check(md.contains("startElement"), "method-startElement");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
