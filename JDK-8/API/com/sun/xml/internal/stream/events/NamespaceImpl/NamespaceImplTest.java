/*
 * @test
 * @summary com.sun.xml.internal.stream.events.NamespaceImpl conformance: value class behavior.
 * @run main NamespaceImplTest
 */

public class NamespaceImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        com.sun.xml.internal.stream.events.NamespaceImpl v = new com.sun.xml.internal.stream.events.NamespaceImpl("p", "uri");
        check("p".equals(v.getPrefix()), "getPrefix");
        check("uri".equals(v.getNamespaceURI()), "getNamespaceURI");
        check(v.isNamespace(), "isNamespace");
        check(v.getEventType() == javax.xml.stream.XMLStreamConstants.NAMESPACE, "event type NAMESPACE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
