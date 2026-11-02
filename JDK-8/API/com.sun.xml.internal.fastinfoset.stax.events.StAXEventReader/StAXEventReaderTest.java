/*
 * @test
 * @summary com.sun.xml.internal.fastinfoset.stax.events.StAXEventReader conformance: class hierarchy and members.
 * @run main StAXEventReaderTest
 */

public class StAXEventReaderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.fastinfoset.stax.events.StAXEventReader", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.fastinfoset.stax.events.StAXEventReader"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "direct interface count");
        check(ifs.contains("javax.xml.stream.XMLEventReader"), "has interface javax.xml.stream.XMLEventReader");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("close"), "has method close");
        check(mn.contains("getElementText"), "has method getElementText");
        check(mn.contains("getProperty"), "has method getProperty");
        check(mn.contains("hasNext"), "has method hasNext");
        check(mn.contains("next"), "has method next");
        check(mn.contains("nextEvent"), "has method nextEvent");
        check(mn.contains("nextTag"), "has method nextTag");
        check(mn.contains("peek"), "has method peek");
        check(mn.contains("remove"), "has method remove");
        check(mn.contains("setAllocator"), "has method setAllocator");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
