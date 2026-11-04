/*
 * @test
 * @summary com.sun.xml.internal.messaging.saaj.soap.SOAPDocumentImpl conformance: class hierarchy and members.
 * @run main SOAPDocumentImplTest
 */

public class SOAPDocumentImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.messaging.saaj.soap.SOAPDocumentImpl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.messaging.saaj.soap.SOAPDocumentImpl"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("com.sun.org.apache.xerces.internal.dom.DocumentImpl".equals(c.getSuperclass().getName()), "extends com.sun.org.apache.xerces.internal.dom.DocumentImpl");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "direct interface count");
        check(ifs.contains("com.sun.xml.internal.messaging.saaj.soap.SOAPDocument"), "has interface com.sun.xml.internal.messaging.saaj.soap.SOAPDocument");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("cloneNode"), "has method cloneNode");
        check(mn.contains("createAttribute"), "has method createAttribute");
        check(mn.contains("createAttributeNS"), "has method createAttributeNS");
        check(mn.contains("createCDATASection"), "has method createCDATASection");
        check(mn.contains("createComment"), "has method createComment");
        check(mn.contains("createDocumentFragment"), "has method createDocumentFragment");
        check(mn.contains("createElement"), "has method createElement");
        check(mn.contains("createElementNS"), "has method createElementNS");
        check(mn.contains("createEntityReference"), "has method createEntityReference");
        check(mn.contains("createProcessingInstruction"), "has method createProcessingInstruction");
        check(mn.contains("createTextNode"), "has method createTextNode");
        check(mn.contains("doGetDocumentElement"), "has method doGetDocumentElement");
        check(mn.contains("getDoctype"), "has method getDoctype");
        check(mn.contains("getDocument"), "has method getDocument");
        check(mn.contains("getDocumentElement"), "has method getDocumentElement");
        check(mn.contains("getElementById"), "has method getElementById");
        check(mn.contains("getElementsByTagName"), "has method getElementsByTagName");
        check(mn.contains("getElementsByTagNameNS"), "has method getElementsByTagNameNS");
        check(mn.contains("getImplementation"), "has method getImplementation");
        check(mn.contains("getSOAPPart"), "has method getSOAPPart");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
