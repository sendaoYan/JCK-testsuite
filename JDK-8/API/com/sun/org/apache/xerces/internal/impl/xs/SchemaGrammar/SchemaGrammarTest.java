/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar conformance: class hierarchy and members.
 * @run main SchemaGrammarTest
 */
public class SchemaGrammarTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar", false, SchemaGrammarTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xni.grammars.XSGrammar"), "has interface com.sun.org.apache.xerces.internal.xni.grammars.XSGrammar");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xs.XSNamespaceItem"), "has interface com.sun.org.apache.xerces.internal.xs.XSNamespaceItem");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getGrammarDescription"), "method getGrammarDescription");
        check(mn.contains("isNamespaceAware"), "method isNamespaceAware");
        check(mn.contains("setImportedGrammars"), "method setImportedGrammars");
        check(mn.contains("getImportedGrammars"), "method getImportedGrammars");
        check(mn.contains("getTargetNamespace"), "method getTargetNamespace");
        check(mn.contains("addGlobalAttributeDecl"), "method addGlobalAttributeDecl");
        check(mn.contains("addGlobalAttributeGroupDecl"), "method addGlobalAttributeGroupDecl");
        check(mn.contains("addGlobalElementDeclAll"), "method addGlobalElementDeclAll");
        check(mn.contains("addGlobalElementDecl"), "method addGlobalElementDecl");
        check(mn.contains("addGlobalGroupDecl"), "method addGlobalGroupDecl");
        check(mn.contains("addGlobalNotationDecl"), "method addGlobalNotationDecl");
        check(mn.contains("addGlobalTypeDecl"), "method addGlobalTypeDecl");
        check(mn.contains("addGlobalComplexTypeDecl"), "method addGlobalComplexTypeDecl");
        check(mn.contains("addGlobalSimpleTypeDecl"), "method addGlobalSimpleTypeDecl");
        check(mn.contains("addIDConstraintDecl"), "method addIDConstraintDecl");
        check(mn.contains("getGlobalAttributeDecl"), "method getGlobalAttributeDecl");
        check(mn.contains("getGlobalAttributeGroupDecl"), "method getGlobalAttributeGroupDecl");
        check(mn.contains("getGlobalElementDecl"), "method getGlobalElementDecl");
        check(mn.contains("getGlobalGroupDecl"), "method getGlobalGroupDecl");
        check(mn.contains("getGlobalNotationDecl"), "method getGlobalNotationDecl");
        check(mn.contains("getGlobalTypeDecl"), "method getGlobalTypeDecl");
        check(mn.contains("getIDConstraintDecl"), "method getIDConstraintDecl");
        check(mn.contains("hasIDConstraints"), "method hasIDConstraints");
        check(mn.contains("addComplexTypeDecl"), "method addComplexTypeDecl");
        check(mn.contains("addRedefinedGroupDecl"), "method addRedefinedGroupDecl");
        check(mn.contains("getUncheckedComplexTypeDecls"), "method getUncheckedComplexTypeDecls");
        check(mn.contains("getUncheckedCTLocators"), "method getUncheckedCTLocators");
        check(mn.contains("getRedefinedGroupDecls"), "method getRedefinedGroupDecls");
        check(mn.contains("getRGLocators"), "method getRGLocators");
        check(mn.contains("setUncheckedTypeNum"), "method setUncheckedTypeNum");
        check(mn.contains("getSubstitutionGroups"), "method getSubstitutionGroups");
        check(mn.contains("getS4SGrammar"), "method getS4SGrammar");
        check(mn.contains("resize"), "method resize");
        check(mn.contains("addDocument"), "method addDocument");
        check(mn.contains("removeDocument"), "method removeDocument");
        check(mn.contains("getSchemaNamespace"), "method getSchemaNamespace");
        check(mn.contains("getDOMParser"), "method getDOMParser");
        check(mn.contains("getSAXParser"), "method getSAXParser");
        check(mn.contains("getComponents"), "method getComponents");
        check(mn.contains("getComponentsExt"), "method getComponentsExt");
        check(mn.contains("resetComponents"), "method resetComponents");
        check(mn.contains("getTypeDefinition"), "method getTypeDefinition");
        check(mn.contains("getAttributeDeclaration"), "method getAttributeDeclaration");
        check(mn.contains("getElementDeclaration"), "method getElementDeclaration");
        check(mn.contains("getAttributeGroup"), "method getAttributeGroup");
        check(mn.contains("getModelGroupDefinition"), "method getModelGroupDefinition");
        check(mn.contains("getNotationDeclaration"), "method getNotationDeclaration");
        check(mn.contains("getDocumentLocations"), "method getDocumentLocations");
        check(mn.contains("toXSModel"), "method toXSModel");
        check(mn.contains("getAnnotations"), "method getAnnotations");
        check(mn.contains("addAnnotation"), "method addAnnotation");
        check(mn.contains("setImmutable"), "method setImmutable");
        check(mn.contains("isImmutable"), "method isImmutable");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
