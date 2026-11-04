/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.util.DOMErrorHandlerWrapper conformance: class hierarchy and members.
 * @run main DOMErrorHandlerWrapperTest
 */
public class DOMErrorHandlerWrapperTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.util.DOMErrorHandlerWrapper", false, DOMErrorHandlerWrapperTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xni.parser.XMLErrorHandler"), "has interface com.sun.org.apache.xerces.internal.xni.parser.XMLErrorHandler");
        check(hasInterface(c, "org.w3c.dom.DOMErrorHandler"), "has interface org.w3c.dom.DOMErrorHandler");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("setErrorHandler"), "method setErrorHandler");
        check(mn.contains("getErrorHandler"), "method getErrorHandler");
        check(mn.contains("warning"), "method warning");
        check(mn.contains("error"), "method error");
        check(mn.contains("fatalError"), "method fatalError");
        check(mn.contains("handleError"), "method handleError");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
