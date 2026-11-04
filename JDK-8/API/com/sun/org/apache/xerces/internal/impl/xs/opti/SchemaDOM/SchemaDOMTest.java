/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.xs.opti.SchemaDOM conformance: class hierarchy and members.
 * @run main SchemaDOMTest
 */
public class SchemaDOMTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.xs.opti.SchemaDOM", false, SchemaDOMTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultDocument"), "superclass com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultDocument");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("startElement"), "method startElement");
        check(mn.contains("emptyElement"), "method emptyElement");
        check(mn.contains("endElement"), "method endElement");
        check(mn.contains("comment"), "method comment");
        check(mn.contains("processingInstruction"), "method processingInstruction");
        check(mn.contains("characters"), "method characters");
        check(mn.contains("charactersRaw"), "method charactersRaw");
        check(mn.contains("endAnnotation"), "method endAnnotation");
        check(mn.contains("endAnnotationElement"), "method endAnnotationElement");
        check(mn.contains("endSyntheticAnnotationElement"), "method endSyntheticAnnotationElement");
        check(mn.contains("startAnnotationCDATA"), "method startAnnotationCDATA");
        check(mn.contains("endAnnotationCDATA"), "method endAnnotationCDATA");
        check(mn.contains("reset"), "method reset");
        check(mn.contains("printDOM"), "method printDOM");
        check(mn.contains("traverse"), "method traverse");
        check(mn.contains("indent"), "method indent");
        check(mn.contains("getDocumentElement"), "method getDocumentElement");
        check(mn.contains("getImplementation"), "method getImplementation");
        check(mn.contains("startAnnotation"), "method startAnnotation");
        check(mn.contains("startAnnotationElement"), "method startAnnotationElement");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
