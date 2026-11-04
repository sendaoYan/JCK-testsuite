/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.xs.traversers.XSDHandler conformance: class hierarchy and members.
 * @run main XSDHandlerTest
 */
public class XSDHandlerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.xs.traversers.XSDHandler", false, XSDHandlerTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("parseSchema"), "method parseSchema");
        check(mn.contains("getGrammar"), "method getGrammar");
        check(mn.contains("findGrammar"), "method findGrammar");
        check(mn.contains("constructTrees"), "method constructTrees");
        check(mn.contains("buildGlobalNameRegistries"), "method buildGlobalNameRegistries");
        check(mn.contains("traverseSchemas"), "method traverseSchemas");
        check(mn.contains("addGlobalAttributeDecl"), "method addGlobalAttributeDecl");
        check(mn.contains("addGlobalAttributeGroupDecl"), "method addGlobalAttributeGroupDecl");
        check(mn.contains("addGlobalElementDecl"), "method addGlobalElementDecl");
        check(mn.contains("addGlobalGroupDecl"), "method addGlobalGroupDecl");
        check(mn.contains("addGlobalNotationDecl"), "method addGlobalNotationDecl");
        check(mn.contains("addGlobalTypeDecl"), "method addGlobalTypeDecl");
        check(mn.contains("addIDConstraintDecl"), "method addIDConstraintDecl");
        check(mn.contains("getGlobalDecl"), "method getGlobalDecl");
        check(mn.contains("getGlobalDeclFromGrammar"), "method getGlobalDeclFromGrammar");
        check(mn.contains("traverseGlobalDecl"), "method traverseGlobalDecl");
        check(mn.contains("schemaDocument2SystemId"), "method schemaDocument2SystemId");
        check(mn.contains("getGrpOrAttrGrpRedefinedByRestriction"), "method getGrpOrAttrGrpRedefinedByRestriction");
        check(mn.contains("resolveKeyRefs"), "method resolveKeyRefs");
        check(mn.contains("getIDRegistry"), "method getIDRegistry");
        check(mn.contains("getIDRegistry_sub"), "method getIDRegistry_sub");
        check(mn.contains("storeKeyRef"), "method storeKeyRef");
        check(mn.contains("prepareForParse"), "method prepareForParse");
        check(mn.contains("prepareForTraverse"), "method prepareForTraverse");
        check(mn.contains("setDeclPool"), "method setDeclPool");
        check(mn.contains("setDVFactory"), "method setDVFactory");
        check(mn.contains("getDVFactory"), "method getDVFactory");
        check(mn.contains("reset"), "method reset");
        check(mn.contains("traverseLocalElements"), "method traverseLocalElements");
        check(mn.contains("fillInLocalElemInfo"), "method fillInLocalElemInfo");
        check(mn.contains("checkForDuplicateNames"), "method checkForDuplicateNames");
        check(mn.contains("element2Locator"), "method element2Locator");
        check(mn.contains("reportSchemaFatalError"), "method reportSchemaFatalError");
        check(mn.contains("reportSchemaError"), "method reportSchemaError");
        check(mn.contains("reportSchemaWarning"), "method reportSchemaWarning");
        check(mn.contains("reportSchemaErr"), "method reportSchemaErr");
        check(mn.contains("setGenerateSyntheticAnnotations"), "method setGenerateSyntheticAnnotations");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
