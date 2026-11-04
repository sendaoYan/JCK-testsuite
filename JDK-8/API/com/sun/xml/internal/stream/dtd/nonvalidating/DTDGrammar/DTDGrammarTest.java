/*
 * @test
 * @summary com.sun.xml.internal.stream.dtd.nonvalidating.DTDGrammar conformance: class hierarchy and members.
 * @run main DTDGrammarTest
 */

public class DTDGrammarTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.stream.dtd.nonvalidating.DTDGrammar", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.stream.dtd.nonvalidating.DTDGrammar"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("attributeDecl"), "has method attributeDecl");
        check(mn.contains("createAttributeDecl"), "has method createAttributeDecl");
        check(mn.contains("createElementDecl"), "has method createElementDecl");
        check(mn.contains("elementDecl"), "has method elementDecl");
        check(mn.contains("endDTD"), "has method endDTD");
        check(mn.contains("ensureAttributeDeclCapacity"), "has method ensureAttributeDeclCapacity");
        check(mn.contains("ensureElementDeclCapacity"), "has method ensureElementDeclCapacity");
        check(mn.contains("getAttributeDecl"), "has method getAttributeDecl");
        check(mn.contains("getAttributeDeclIndex"), "has method getAttributeDeclIndex");
        check(mn.contains("getContentSpecType"), "has method getContentSpecType");
        check(mn.contains("getElementDecl"), "has method getElementDecl");
        check(mn.contains("getElementDeclIndex"), "has method getElementDeclIndex");
        check(mn.contains("getFirstAttributeDeclIndex"), "has method getFirstAttributeDeclIndex");
        check(mn.contains("getFirstElementDeclIndex"), "has method getFirstElementDeclIndex");
        check(mn.contains("getNextAttributeDeclIndex"), "has method getNextAttributeDeclIndex");
        check(mn.contains("getNextElementDeclIndex"), "has method getNextElementDeclIndex");
        check(mn.contains("getNotationDecls"), "has method getNotationDecls");
        check(mn.contains("getSymbolTable"), "has method getSymbolTable");
        check(mn.contains("isCDATAAttribute"), "has method isCDATAAttribute");
        check(mn.contains("normalizeDefaultAttrValue"), "has method normalizeDefaultAttrValue");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
