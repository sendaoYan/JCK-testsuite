/*
 * @test
 * @summary com.sun.xml.internal.messaging.saaj.soap.SOAPPartImpl conformance: abstract class hierarchy and members.
 * @run main SOAPPartImplTest
 */

public class SOAPPartImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.messaging.saaj.soap.SOAPPartImpl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.messaging.saaj.soap.SOAPPartImpl"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check("javax.xml.soap.SOAPPart".equals(c.getSuperclass().getName()), "extends javax.xml.soap.SOAPPart");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "direct interface count");
        check(ifs.contains("com.sun.xml.internal.messaging.saaj.soap.SOAPDocument"), "has interface com.sun.xml.internal.messaging.saaj.soap.SOAPDocument");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("addMimeHeader"), "has method addMimeHeader");
        check(mn.contains("adoptNode"), "has method adoptNode");
        check(mn.contains("appendChild"), "has method appendChild");
        check(mn.contains("cloneNode"), "has method cloneNode");
        check(mn.contains("compareDocumentPosition"), "has method compareDocumentPosition");
        check(mn.contains("createAttribute"), "has method createAttribute");
        check(mn.contains("createAttributeNS"), "has method createAttributeNS");
        check(mn.contains("createCDATASection"), "has method createCDATASection");
        check(mn.contains("createComment"), "has method createComment");
        check(mn.contains("createDocumentFragment"), "has method createDocumentFragment");
        check(mn.contains("createElement"), "has method createElement");
        check(mn.contains("createElementNS"), "has method createElementNS");
        check(mn.contains("createEmptyEnvelope"), "has method createEmptyEnvelope");
        check(mn.contains("createEntityReference"), "has method createEntityReference");
        check(mn.contains("createEnvelopeFromSource"), "has method createEnvelopeFromSource");
        check(mn.contains("createProcessingInstruction"), "has method createProcessingInstruction");
        check(mn.contains("createTextNode"), "has method createTextNode");
        check(mn.contains("detachNode"), "has method detachNode");
        check(mn.contains("doCloneNode"), "has method doCloneNode");
        check(mn.contains("doGetDocumentElement"), "has method doGetDocumentElement");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
