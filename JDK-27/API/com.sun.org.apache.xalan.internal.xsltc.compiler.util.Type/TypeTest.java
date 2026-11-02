/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type conformance: internal package reflection.
 * @modules java.xml/com.sun.org.apache.xalan.internal.xsltc.compiler.util:+open
 * @run main TypeTest
 */
public class TypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type", false, TypeTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type"), "name");
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
        check(fd.size() == 17, "field-count");
        check(fd.contains("Int"), "field-Int");
        check(fd.contains("Real"), "field-Real");
        check(fd.contains("Boolean"), "field-Boolean");
        check(fd.contains("NodeSet"), "field-NodeSet");
        check(fd.contains("String"), "field-String");
        check(fd.contains("ResultTree"), "field-ResultTree");
        check(fd.contains("Reference"), "field-Reference");
        check(fd.contains("Void"), "field-Void");
        check(fd.contains("Object"), "field-Object");
        check(fd.contains("ObjectString"), "field-ObjectString");
        check(fd.contains("Node"), "field-Node");
        check(fd.contains("Root"), "field-Root");
        check(fd.contains("Element"), "field-Element");
        check(fd.contains("Attribute"), "field-Attribute");
        check(fd.contains("Text"), "field-Text");
        check(fd.contains("Comment"), "field-Comment");
        check(fd.contains("Processing_Instruction"), "field-Processing_Instruction");
        check(md.size() == 30, "method-count");
        check(md.contains("ADD"), "method-ADD");
        check(md.contains("CMP"), "method-CMP");
        check(md.contains("DIV"), "method-DIV");
        check(md.contains("DUP"), "method-DUP");
        check(md.contains("GE"), "method-GE");
        check(md.contains("GT"), "method-GT");
        check(md.contains("LE"), "method-LE");
        check(md.contains("LOAD"), "method-LOAD");
        check(md.contains("LT"), "method-LT");
        check(md.contains("MUL"), "method-MUL");
        check(md.contains("NEG"), "method-NEG");
        check(md.contains("POP"), "method-POP");
        check(md.contains("REM"), "method-REM");
        check(md.contains("STORE"), "method-STORE");
        check(md.contains("SUB"), "method-SUB");
        check(md.contains("distanceTo"), "method-distanceTo");
        check(md.contains("getClassName"), "method-getClassName");
        check(md.contains("identicalTo"), "method-identicalTo");
        check(md.contains("implementedAsMethod"), "method-implementedAsMethod");
        check(md.contains("isNumber"), "method-isNumber");
        check(md.contains("isSimple"), "method-isSimple");
        check(md.contains("newObjectType"), "method-newObjectType");
        check(md.contains("toJCType"), "method-toJCType");
        check(md.contains("toSignature"), "method-toSignature");
        check(md.contains("toString"), "method-toString");
        check(md.contains("translateBox"), "method-translateBox");
        check(md.contains("translateFrom"), "method-translateFrom");
        check(md.contains("translateTo"), "method-translateTo");
        check(md.contains("translateToDesynthesized"), "method-translateToDesynthesized");
        check(md.contains("translateUnBox"), "method-translateUnBox");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
