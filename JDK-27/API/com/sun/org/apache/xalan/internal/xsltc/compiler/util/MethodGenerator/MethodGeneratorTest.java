/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator conformance: internal package reflection.
 * @modules java.xml/com.sun.org.apache.xalan.internal.xsltc.compiler.util:+open
 * @run main MethodGeneratorTest
 */
public class MethodGeneratorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator", false, MethodGeneratorTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.bcel.internal.generic.MethodGen"), "superclass");
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
        check(fd.size() == 1, "field-count");
        check(fd.contains("INVALID_INDEX"), "field-INVALID_INDEX");
        check(md.size() == 32, "method-count");
        check(md.contains("addInstructionList"), "method-addInstructionList");
        check(md.contains("addLocalVariable"), "method-addLocalVariable");
        check(md.contains("addLocalVariable2"), "method-addLocalVariable2");
        check(md.contains("attribute"), "method-attribute");
        check(md.contains("endDocument"), "method-endDocument");
        check(md.contains("endElement"), "method-endElement");
        check(md.contains("getInstructionList"), "method-getInstructionList");
        check(md.contains("getLocalIndex"), "method-getLocalIndex");
        check(md.contains("getLocalVariable"), "method-getLocalVariable");
        check(md.contains("getThisMethod"), "method-getThisMethod");
        check(md.contains("loadContextNode"), "method-loadContextNode");
        check(md.contains("loadCurrentNode"), "method-loadCurrentNode");
        check(md.contains("loadDOM"), "method-loadDOM");
        check(md.contains("loadHandler"), "method-loadHandler");
        check(md.contains("loadIterator"), "method-loadIterator");
        check(md.contains("markChunkEnd"), "method-markChunkEnd");
        check(md.contains("markChunkStart"), "method-markChunkStart");
        check(md.contains("namespace"), "method-namespace");
        check(md.contains("nextNode"), "method-nextNode");
        check(md.contains("outlineChunks"), "method-outlineChunks");
        check(md.contains("removeLocalVariable"), "method-removeLocalVariable");
        check(md.contains("reset"), "method-reset");
        check(md.contains("setMaxLocals"), "method-setMaxLocals");
        check(md.contains("setStartNode"), "method-setStartNode");
        check(md.contains("startDocument"), "method-startDocument");
        check(md.contains("startElement"), "method-startElement");
        check(md.contains("storeContextNode"), "method-storeContextNode");
        check(md.contains("storeCurrentNode"), "method-storeCurrentNode");
        check(md.contains("storeDOM"), "method-storeDOM");
        check(md.contains("storeHandler"), "method-storeHandler");
        check(md.contains("storeIterator"), "method-storeIterator");
        check(md.contains("uniqueAttribute"), "method-uniqueAttribute");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
