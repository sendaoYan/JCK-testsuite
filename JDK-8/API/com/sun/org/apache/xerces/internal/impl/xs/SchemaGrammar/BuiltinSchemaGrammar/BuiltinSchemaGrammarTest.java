/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar.BuiltinSchemaGrammar conformance: class hierarchy and members.
 * @run main BuiltinSchemaGrammarTest
 */
public class BuiltinSchemaGrammarTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar$BuiltinSchemaGrammar", false, BuiltinSchemaGrammarTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar"), "superclass com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getGrammarDescription"), "method getGrammarDescription");
        check(mn.contains("setImportedGrammars"), "method setImportedGrammars");
        check(mn.contains("addGlobalAttributeDecl"), "method addGlobalAttributeDecl");
        check(mn.contains("addGlobalAttributeGroupDecl"), "method addGlobalAttributeGroupDecl");
        check(mn.contains("addGlobalElementDecl"), "method addGlobalElementDecl");
        check(mn.contains("addGlobalElementDeclAll"), "method addGlobalElementDeclAll");
        check(mn.contains("addGlobalGroupDecl"), "method addGlobalGroupDecl");
        check(mn.contains("addGlobalNotationDecl"), "method addGlobalNotationDecl");
        check(mn.contains("addGlobalTypeDecl"), "method addGlobalTypeDecl");
        check(mn.contains("addGlobalComplexTypeDecl"), "method addGlobalComplexTypeDecl");
        check(mn.contains("addGlobalSimpleTypeDecl"), "method addGlobalSimpleTypeDecl");
        check(mn.contains("addComplexTypeDecl"), "method addComplexTypeDecl");
        check(mn.contains("addRedefinedGroupDecl"), "method addRedefinedGroupDecl");
        check(mn.contains("addDocument"), "method addDocument");
        check(mn.contains("getDOMParser"), "method getDOMParser");
        check(mn.contains("getSAXParser"), "method getSAXParser");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
