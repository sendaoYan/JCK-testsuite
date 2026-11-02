/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.compiler.Stylesheet conformance: reflection.
 * @run main StylesheetTest
 */

public class StylesheetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.compiler.Stylesheet", false, StylesheetTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("Stylesheet"), "simple name Stylesheet");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "is final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode"), "superclass com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "getOutputMethod"), "method getOutputMethod");
        check(has(c, "getTemplateInlining"), "method getTemplateInlining");
        check(has(c, "setTemplateInlining"), "method setTemplateInlining");
        check(has(c, "isSimplified"), "method isSimplified");
        check(has(c, "setSimplified"), "method setSimplified");
        check(has(c, "setHasIdCall"), "method setHasIdCall");
        check(has(c, "setOutputProperty"), "method setOutputProperty");
        check(has(c, "setOutputProperties"), "method setOutputProperties");
        check(has(c, "getOutputProperties"), "method getOutputProperties");
        check(has(c, "getLastOutputElement"), "method getLastOutputElement");
        check(has(c, "setMultiDocument"), "method setMultiDocument");
        check(has(c, "isMultiDocument"), "method isMultiDocument");
        check(has(c, "setCallsNodeset"), "method setCallsNodeset");
        check(has(c, "callsNodeset"), "method callsNodeset");
        check(has(c, "numberFormattingUsed"), "method numberFormattingUsed");
        check(has(c, "setImportPrecedence"), "method setImportPrecedence");
        check(has(c, "getImportPrecedence"), "method getImportPrecedence");
        check(has(c, "getMinimumDescendantPrecedence"), "method getMinimumDescendantPrecedence");
        check(has(c, "checkForLoop"), "method checkForLoop");
        check(has(c, "setParser"), "method setParser");
        check(has(c, "setParentStylesheet"), "method setParentStylesheet");
        check(has(c, "getParentStylesheet"), "method getParentStylesheet");
        check(has(c, "setImportingStylesheet"), "method setImportingStylesheet");
        check(has(c, "setIncludingStylesheet"), "method setIncludingStylesheet");
        check(has(c, "addIncludedStylesheet"), "method addIncludedStylesheet");
        check(has(c, "setSystemId"), "method setSystemId");
        check(has(c, "getSystemId"), "method getSystemId");
        check(has(c, "setSourceLoader"), "method setSourceLoader");
        check(has(c, "getSourceLoader"), "method getSourceLoader");
        check(has(c, "hasGlobals"), "method hasGlobals");
        check(has(c, "hasLocalParams"), "method hasLocalParams");
        check(has(c, "isExtension"), "method isExtension");
        check(has(c, "declareExtensionPrefixes"), "method declareExtensionPrefixes");
        check(has(c, "parseContents"), "method parseContents");
        check(has(c, "parseOwnChildren"), "method parseOwnChildren");
        check(has(c, "processModes"), "method processModes");
        check(has(c, "getMode"), "method getMode");
        check(has(c, "typeCheck"), "method typeCheck");
        check(has(c, "translate"), "method translate");
        check(has(c, "addParam"), "method addParam");
        check(has(c, "addVariable"), "method addVariable");
        check(has(c, "display"), "method display");
        check(has(c, "getNamespace"), "method getNamespace");
        check(has(c, "getClassName"), "method getClassName");
        check(has(c, "getTemplates"), "method getTemplates");
        check(has(c, "getAllValidTemplates"), "method getAllValidTemplates");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
