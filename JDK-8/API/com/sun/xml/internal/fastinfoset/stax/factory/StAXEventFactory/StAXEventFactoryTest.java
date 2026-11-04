/*
 * @test
 * @summary com.sun.xml.internal.fastinfoset.stax.factory.StAXEventFactory conformance: class hierarchy and members.
 * @run main StAXEventFactoryTest
 */

public class StAXEventFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.fastinfoset.stax.factory.StAXEventFactory", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.fastinfoset.stax.factory.StAXEventFactory"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("javax.xml.stream.XMLEventFactory".equals(c.getSuperclass().getName()), "extends javax.xml.stream.XMLEventFactory");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("createAttribute"), "has method createAttribute");
        check(mn.contains("createCData"), "has method createCData");
        check(mn.contains("createCharacters"), "has method createCharacters");
        check(mn.contains("createComment"), "has method createComment");
        check(mn.contains("createDTD"), "has method createDTD");
        check(mn.contains("createEndDocument"), "has method createEndDocument");
        check(mn.contains("createEndElement"), "has method createEndElement");
        check(mn.contains("createEntityReference"), "has method createEntityReference");
        check(mn.contains("createIgnorableSpace"), "has method createIgnorableSpace");
        check(mn.contains("createNamespace"), "has method createNamespace");
        check(mn.contains("createProcessingInstruction"), "has method createProcessingInstruction");
        check(mn.contains("createSpace"), "has method createSpace");
        check(mn.contains("createStartDocument"), "has method createStartDocument");
        check(mn.contains("createStartElement"), "has method createStartElement");
        check(mn.contains("setLocation"), "has method setLocation");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
