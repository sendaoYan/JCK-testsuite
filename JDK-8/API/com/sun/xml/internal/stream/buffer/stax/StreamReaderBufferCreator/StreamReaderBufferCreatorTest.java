/*
 * @test
 * @summary com.sun.xml.internal.stream.buffer.stax.StreamReaderBufferCreator conformance: class hierarchy and members.
 * @run main StreamReaderBufferCreatorTest
 */

public class StreamReaderBufferCreatorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.stream.buffer.stax.StreamReaderBufferCreator", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.stream.buffer.stax.StreamReaderBufferCreator"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("com.sun.xml.internal.stream.buffer.stax.StreamBufferCreator".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.stream.buffer.stax.StreamBufferCreator");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("create"), "has method create");
        check(mn.contains("createElementFragment"), "has method createElementFragment");
        check(mn.contains("store"), "has method store");
        check(mn.contains("storeAttributes"), "has method storeAttributes");
        check(mn.contains("storeComment"), "has method storeComment");
        check(mn.contains("storeDocumentAndChildren"), "has method storeDocumentAndChildren");
        check(mn.contains("storeElement"), "has method storeElement");
        check(mn.contains("storeElementAndChildren"), "has method storeElementAndChildren");
        check(mn.contains("storeElementAndChildrenEx"), "has method storeElementAndChildrenEx");
        check(mn.contains("storeElementAndChildrenNoEx"), "has method storeElementAndChildrenNoEx");
        check(mn.contains("storeElementWithInScopeNamespaces"), "has method storeElementWithInScopeNamespaces");
        check(mn.contains("storeEndElement"), "has method storeEndElement");
        check(mn.contains("storeNamespaceAttributes"), "has method storeNamespaceAttributes");
        check(mn.contains("storeProcessingInstruction"), "has method storeProcessingInstruction");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
