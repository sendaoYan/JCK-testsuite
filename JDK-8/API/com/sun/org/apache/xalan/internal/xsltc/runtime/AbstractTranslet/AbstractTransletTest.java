/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet conformance: reflection.
 * @run main AbstractTransletTest
 */

public class AbstractTransletTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet", false, AbstractTransletTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("AbstractTranslet"), "simple name AbstractTranslet");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 1, "interfaces count 1");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.xalan.internal.xsltc.Translet"), "interface com.sun.org.apache.xalan.internal.xsltc.Translet");
        check(has(c, "printInternalState"), "method printInternalState");
        check(has(c, "makeDOMAdapter"), "method makeDOMAdapter");
        check(has(c, "pushParamFrame"), "method pushParamFrame");
        check(has(c, "popParamFrame"), "method popParamFrame");
        check(has(c, "addParameter"), "method addParameter");
        check(has(c, "clearParameters"), "method clearParameters");
        check(has(c, "getParameter"), "method getParameter");
        check(has(c, "setMessageHandler"), "method setMessageHandler");
        check(has(c, "displayMessage"), "method displayMessage");
        check(has(c, "addDecimalFormat"), "method addDecimalFormat");
        check(has(c, "getDecimalFormat"), "method getDecimalFormat");
        check(has(c, "prepassDocument"), "method prepassDocument");
        check(has(c, "postInitialization"), "method postInitialization");
        check(has(c, "setIndexSize"), "method setIndexSize");
        check(has(c, "createKeyIndex"), "method createKeyIndex");
        check(has(c, "buildKeyIndex"), "method buildKeyIndex");
        check(has(c, "getKeyIndex"), "method getKeyIndex");
        check(has(c, "buildKeys"), "method buildKeys");
        check(has(c, "setKeyIndexDom"), "method setKeyIndexDom");
        check(has(c, "setDOMCache"), "method setDOMCache");
        check(has(c, "getDOMCache"), "method getDOMCache");
        check(has(c, "openOutputHandler"), "method openOutputHandler");
        check(has(c, "closeOutputHandler"), "method closeOutputHandler");
        check(has(c, "transform"), "method transform");
        check(has(c, "characters"), "method characters");
        check(has(c, "addCdataElement"), "method addCdataElement");
        check(has(c, "addAuxiliaryClass"), "method addAuxiliaryClass");
        check(has(c, "setAuxiliaryClasses"), "method setAuxiliaryClasses");
        check(has(c, "getAuxiliaryClass"), "method getAuxiliaryClass");
        check(has(c, "getNamesArray"), "method getNamesArray");
        check(has(c, "getUrisArray"), "method getUrisArray");
        check(has(c, "getTypesArray"), "method getTypesArray");
        check(has(c, "getNamespaceArray"), "method getNamespaceArray");
        check(has(c, "hasIdCall"), "method hasIdCall");
        check(has(c, "getTemplates"), "method getTemplates");
        check(has(c, "setTemplates"), "method setTemplates");
        check(has(c, "overrideDefaultParser"), "method overrideDefaultParser");
        check(has(c, "setOverrideDefaultParser"), "method setOverrideDefaultParser");
        check(has(c, "getAllowedProtocols"), "method getAllowedProtocols");
        check(has(c, "setAllowedProtocols"), "method setAllowedProtocols");
        check(has(c, "getAccessError"), "method getAccessError");
        check(has(c, "setAccessError"), "method setAccessError");
        check(has(c, "newDocument"), "method newDocument");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
