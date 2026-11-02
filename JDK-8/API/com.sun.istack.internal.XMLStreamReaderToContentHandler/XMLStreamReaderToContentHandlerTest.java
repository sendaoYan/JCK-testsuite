/*
 * @test
 * @summary com.sun.istack.internal.XMLStreamReaderToContentHandler conformance: hierarchy and declared members via reflection.
 * @run main XMLStreamReaderToContentHandlerTest
 */

public class XMLStreamReaderToContentHandlerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.istack.internal.XMLStreamReaderToContentHandler", false, null);
        check(c.getName().equals("com.sun.istack.internal.XMLStreamReaderToContentHandler"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("bridge"), "declares bridge");
        check(ms.contains("fixNull"), "declares fixNull");
        check(ms.contains("getAttributes"), "declares getAttributes");
        check(ms.contains("handleAttribute"), "declares handleAttribute");
        check(ms.contains("handleCDATA"), "declares handleCDATA");
        check(ms.contains("handleCharacters"), "declares handleCharacters");
        check(ms.contains("handleComment"), "declares handleComment");
        check(ms.contains("handleDTD"), "declares handleDTD");
        check(ms.contains("handleEndDocument"), "declares handleEndDocument");
        check(ms.contains("handleEndElement"), "declares handleEndElement");
        check(ms.contains("handleEntityDecl"), "declares handleEntityDecl");
        check(ms.contains("handleEntityReference"), "declares handleEntityReference");
        check(ms.contains("handleNamespace"), "declares handleNamespace");
        check(ms.contains("handleNotationDecl"), "declares handleNotationDecl");
        check(ms.contains("handlePI"), "declares handlePI");
        check(ms.contains("handleSpace"), "declares handleSpace");
        check(ms.contains("handleStartDocument"), "declares handleStartDocument");
        check(ms.contains("handleStartElement"), "declares handleStartElement");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
