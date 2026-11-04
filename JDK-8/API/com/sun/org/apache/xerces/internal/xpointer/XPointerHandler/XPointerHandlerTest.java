/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.xpointer.XPointerHandler conformance: class hierarchy and members.
 * @run main XPointerHandlerTest
 */
public class XPointerHandlerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.xpointer.XPointerHandler", false, XPointerHandlerTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.xinclude.XIncludeHandler"), "superclass com.sun.org.apache.xerces.internal.xinclude.XIncludeHandler");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xpointer.XPointerProcessor"), "has interface com.sun.org.apache.xerces.internal.xpointer.XPointerProcessor");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("setDocumentHandler"), "method setDocumentHandler");
        check(mn.contains("parseXPointer"), "method parseXPointer");
        check(mn.contains("resolveXPointer"), "method resolveXPointer");
        check(mn.contains("isFragmentResolved"), "method isFragmentResolved");
        check(mn.contains("isChildFragmentResolved"), "method isChildFragmentResolved");
        check(mn.contains("isXPointerResolved"), "method isXPointerResolved");
        check(mn.contains("getXPointerPart"), "method getXPointerPart");
        check(mn.contains("initErrorReporter"), "method initErrorReporter");
        check(mn.contains("init"), "method init");
        check(mn.contains("getPointerParts"), "method getPointerParts");
        check(mn.contains("comment"), "method comment");
        check(mn.contains("processingInstruction"), "method processingInstruction");
        check(mn.contains("startElement"), "method startElement");
        check(mn.contains("emptyElement"), "method emptyElement");
        check(mn.contains("characters"), "method characters");
        check(mn.contains("ignorableWhitespace"), "method ignorableWhitespace");
        check(mn.contains("endElement"), "method endElement");
        check(mn.contains("startCDATA"), "method startCDATA");
        check(mn.contains("endCDATA"), "method endCDATA");
        check(mn.contains("setProperty"), "method setProperty");
        check(mn.contains("access$300"), "method access$300");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
