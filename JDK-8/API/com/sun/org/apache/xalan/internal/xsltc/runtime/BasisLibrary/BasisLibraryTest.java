/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.runtime.BasisLibrary conformance: reflection.
 * @run main BasisLibraryTest
 */

public class BasisLibraryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.runtime.BasisLibrary", false, BasisLibraryTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("BasisLibrary"), "simple name BasisLibrary");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "is final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "countF"), "method countF");
        check(has(c, "positionF"), "method positionF");
        check(has(c, "sumF"), "method sumF");
        check(has(c, "stringF"), "method stringF");
        check(has(c, "numberF"), "method numberF");
        check(has(c, "roundF"), "method roundF");
        check(has(c, "booleanF"), "method booleanF");
        check(has(c, "substringF"), "method substringF");
        check(has(c, "substring_afterF"), "method substring_afterF");
        check(has(c, "substring_beforeF"), "method substring_beforeF");
        check(has(c, "translateF"), "method translateF");
        check(has(c, "normalize_spaceF"), "method normalize_spaceF");
        check(has(c, "generate_idF"), "method generate_idF");
        check(has(c, "getLocalName"), "method getLocalName");
        check(has(c, "unresolved_externalF"), "method unresolved_externalF");
        check(has(c, "unallowed_extension_functionF"), "method unallowed_extension_functionF");
        check(has(c, "unallowed_extension_elementF"), "method unallowed_extension_elementF");
        check(has(c, "unsupported_ElementF"), "method unsupported_ElementF");
        check(has(c, "namespace_uriF"), "method namespace_uriF");
        check(has(c, "system_propertyF"), "method system_propertyF");
        check(has(c, "objectTypeF"), "method objectTypeF");
        check(has(c, "nodesetF"), "method nodesetF");
        check(has(c, "compare"), "method compare");
        check(has(c, "testLanguage"), "method testLanguage");
        check(has(c, "stringToReal"), "method stringToReal");
        check(has(c, "stringToInt"), "method stringToInt");
        check(has(c, "realToString"), "method realToString");
        check(has(c, "realToInt"), "method realToInt");
        check(has(c, "formatNumber"), "method formatNumber");
        check(has(c, "referenceToNodeSet"), "method referenceToNodeSet");
        check(has(c, "referenceToNodeList"), "method referenceToNodeList");
        check(has(c, "referenceToNode"), "method referenceToNode");
        check(has(c, "referenceToLong"), "method referenceToLong");
        check(has(c, "referenceToDouble"), "method referenceToDouble");
        check(has(c, "referenceToBoolean"), "method referenceToBoolean");
        check(has(c, "referenceToString"), "method referenceToString");
        check(has(c, "node2Iterator"), "method node2Iterator");
        check(has(c, "nodeList2Iterator"), "method nodeList2Iterator");
        check(has(c, "referenceToResultTree"), "method referenceToResultTree");
        check(has(c, "getSingleNode"), "method getSingleNode");
        check(has(c, "copy"), "method copy");
        check(has(c, "checkAttribQName"), "method checkAttribQName");
        check(has(c, "checkNCName"), "method checkNCName");
        check(has(c, "checkQName"), "method checkQName");
        check(has(c, "startXslElement"), "method startXslElement");
        check(has(c, "getPrefix"), "method getPrefix");
        check(has(c, "generatePrefix"), "method generatePrefix");
        check(has(c, "resetPrefixIndex"), "method resetPrefixIndex");
        check(has(c, "runTimeError"), "method runTimeError");
        check(has(c, "consoleOutput"), "method consoleOutput");
        check(has(c, "replace"), "method replace");
        check(has(c, "mapQNameToJavaName"), "method mapQNameToJavaName");
        check(has(c, "getStringLength"), "method getStringLength");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
