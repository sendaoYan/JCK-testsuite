/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode conformance: internal package reflection.
 * @modules java.xml/com.sun.org.apache.xalan.internal.xsltc.compiler:+open
 * @run main SyntaxTreeNodeTest
 */
public class SyntaxTreeNodeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode", false, SyntaxTreeNodeTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        java.util.Set<String> ifs = new java.util.HashSet<>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "interface-count");
        check(ifs.contains("com.sun.org.apache.xalan.internal.xsltc.compiler.Constants"), "if-com.sun.org.apache.xalan.internal.xsltc.compiler.Constants");
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
        check(fd.contains("_parent"), "field-_parent");
        check(fd.contains("_qname"), "field-_qname");
        check(fd.contains("_attributes"), "field-_attributes");
        check(fd.contains("Dummy"), "field-Dummy");
        check(fd.contains("IndentIncrement"), "field-IndentIncrement");
        check(md.size() == 49, "method-count");
        check(md.contains("addAttribute"), "method-addAttribute");
        check(md.contains("addElement"), "method-addElement");
        check(md.contains("addPrefixMapping"), "method-addPrefixMapping");
        check(md.contains("compileResultTree"), "method-compileResultTree");
        check(md.contains("contextDependent"), "method-contextDependent");
        check(md.contains("dependentContents"), "method-dependentContents");
        check(md.contains("display"), "method-display");
        check(md.contains("displayContents"), "method-displayContents");
        check(md.contains("elementAt"), "method-elementAt");
        check(md.contains("elementCount"), "method-elementCount");
        check(md.contains("elements"), "method-elements");
        check(md.contains("getAttribute"), "method-getAttribute");
        check(md.contains("getAttributes"), "method-getAttributes");
        check(md.contains("getContents"), "method-getContents");
        check(md.contains("getImportPrecedence"), "method-getImportPrecedence");
        check(md.contains("getLineNumber"), "method-getLineNumber");
        check(md.contains("getParent"), "method-getParent");
        check(md.contains("getParser"), "method-getParser");
        check(md.contains("getPrefixMapping"), "method-getPrefixMapping");
        check(md.contains("getQName"), "method-getQName");
        check(md.contains("getStylesheet"), "method-getStylesheet");
        check(md.contains("getSymbolTable"), "method-getSymbolTable");
        check(md.contains("getTemplate"), "method-getTemplate");
        check(md.contains("getXSLTC"), "method-getXSLTC");
        check(md.contains("hasAttribute"), "method-hasAttribute");
        check(md.contains("hasContents"), "method-hasContents");
        check(md.contains("indent"), "method-indent");
        check(md.contains("isDummy"), "method-isDummy");
        check(md.contains("lastChild"), "method-lastChild");
        check(md.contains("lookupNamespace"), "method-lookupNamespace");
        check(md.contains("lookupPrefix"), "method-lookupPrefix");
        check(md.contains("notTypeOf"), "method-notTypeOf");
        check(md.contains("parseChildren"), "method-parseChildren");
        check(md.contains("parseContents"), "method-parseContents");
        check(md.contains("removeElement"), "method-removeElement");
        check(md.contains("reportError"), "method-reportError");
        check(md.contains("reportWarning"), "method-reportWarning");
        check(md.contains("setAttributes"), "method-setAttributes");
        check(md.contains("setFirstElement"), "method-setFirstElement");
        check(md.contains("setLineNumber"), "method-setLineNumber");
        check(md.contains("setParent"), "method-setParent");
        check(md.contains("setParser"), "method-setParser");
        check(md.contains("setPrefixMapping"), "method-setPrefixMapping");
        check(md.contains("setQName"), "method-setQName");
        check(md.contains("translate"), "method-translate");
        check(md.contains("translateContents"), "method-translateContents");
        check(md.contains("typeCheck"), "method-typeCheck");
        check(md.contains("typeCheckContents"), "method-typeCheckContents");
        check(md.contains("updateScope"), "method-updateScope");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
