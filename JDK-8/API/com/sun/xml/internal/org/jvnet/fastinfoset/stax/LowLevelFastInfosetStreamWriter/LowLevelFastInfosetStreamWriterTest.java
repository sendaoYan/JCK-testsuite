/*
 * @test
 * @summary com.sun.xml.internal.org.jvnet.fastinfoset.stax.LowLevelFastInfosetStreamWriter conformance: interface hierarchy and members.
 * @run main LowLevelFastInfosetStreamWriterTest
 */

public class LowLevelFastInfosetStreamWriterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.org.jvnet.fastinfoset.stax.LowLevelFastInfosetStreamWriter", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.org.jvnet.fastinfoset.stax.LowLevelFastInfosetStreamWriter"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getLocalNameIndex"), "has method getLocalNameIndex");
        check(mn.contains("getNextAttributeIndex"), "has method getNextAttributeIndex");
        check(mn.contains("getNextElementIndex"), "has method getNextElementIndex");
        check(mn.contains("getNextLocalNameIndex"), "has method getNextLocalNameIndex");
        check(mn.contains("initiateLowLevelWriting"), "has method initiateLowLevelWriting");
        check(mn.contains("writeLowLevelAttribute"), "has method writeLowLevelAttribute");
        check(mn.contains("writeLowLevelAttributeIndexed"), "has method writeLowLevelAttributeIndexed");
        check(mn.contains("writeLowLevelAttributeValue"), "has method writeLowLevelAttributeValue");
        check(mn.contains("writeLowLevelEndElement"), "has method writeLowLevelEndElement");
        check(mn.contains("writeLowLevelEndNamespaces"), "has method writeLowLevelEndNamespaces");
        check(mn.contains("writeLowLevelEndStartElement"), "has method writeLowLevelEndStartElement");
        check(mn.contains("writeLowLevelNamespace"), "has method writeLowLevelNamespace");
        check(mn.contains("writeLowLevelOctets"), "has method writeLowLevelOctets");
        check(mn.contains("writeLowLevelStartAttributes"), "has method writeLowLevelStartAttributes");
        check(mn.contains("writeLowLevelStartElement"), "has method writeLowLevelStartElement");
        check(mn.contains("writeLowLevelStartElementIndexed"), "has method writeLowLevelStartElementIndexed");
        check(mn.contains("writeLowLevelStartNameLiteral"), "has method writeLowLevelStartNameLiteral");
        check(mn.contains("writeLowLevelStartNamespaces"), "has method writeLowLevelStartNamespaces");
        check(mn.contains("writeLowLevelTerminationAndMark"), "has method writeLowLevelTerminationAndMark");
        check(mn.contains("writeLowLevelText"), "has method writeLowLevelText");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
