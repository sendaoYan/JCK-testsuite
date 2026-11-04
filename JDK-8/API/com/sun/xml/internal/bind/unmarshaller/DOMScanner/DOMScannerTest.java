/*
 * @test
 * @summary com.sun.xml.internal.bind.unmarshaller.DOMScanner conformance: class hierarchy and members.
 * @run main DOMScannerTest
 */

public class DOMScannerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.unmarshaller.DOMScanner", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.unmarshaller.DOMScanner"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 2, "direct interface count");
        check(ifs.contains("com.sun.xml.internal.bind.unmarshaller.InfosetScanner"), "has interface com.sun.xml.internal.bind.unmarshaller.InfosetScanner");
        check(ifs.contains("com.sun.xml.internal.bind.v2.runtime.unmarshaller.LocatorEx"), "has interface com.sun.xml.internal.bind.v2.runtime.unmarshaller.LocatorEx");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("buildNamespaceSupport"), "has method buildNamespaceSupport");
        check(mn.contains("getColumnNumber"), "has method getColumnNumber");
        check(mn.contains("getContentHandler"), "has method getContentHandler");
        check(mn.contains("getCurrentElement"), "has method getCurrentElement");
        check(mn.contains("getCurrentLocation"), "has method getCurrentLocation");
        check(mn.contains("getLineNumber"), "has method getLineNumber");
        check(mn.contains("getLocation"), "has method getLocation");
        check(mn.contains("getLocator"), "has method getLocator");
        check(mn.contains("getPublicId"), "has method getPublicId");
        check(mn.contains("getSystemId"), "has method getSystemId");
        check(mn.contains("parse"), "has method parse");
        check(mn.contains("parseWithContext"), "has method parseWithContext");
        check(mn.contains("scan"), "has method scan");
        check(mn.contains("setContentHandler"), "has method setContentHandler");
        check(mn.contains("setCurrentLocation"), "has method setCurrentLocation");
        check(mn.contains("setLocator"), "has method setLocator");
        check(mn.contains("visit"), "has method visit");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
