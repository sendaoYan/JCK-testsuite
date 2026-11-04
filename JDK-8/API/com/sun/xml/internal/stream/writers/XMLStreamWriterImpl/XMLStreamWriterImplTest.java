/*
 * @test
 * @summary com.sun.xml.internal.stream.writers.XMLStreamWriterImpl conformance: class hierarchy and members.
 * @run main XMLStreamWriterImplTest
 */

public class XMLStreamWriterImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.stream.writers.XMLStreamWriterImpl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.stream.writers.XMLStreamWriterImpl"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("java.util.AbstractMap".equals(c.getSuperclass().getName()), "extends java.util.AbstractMap");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "direct interface count");
        check(ifs.contains("javax.xml.stream.XMLStreamWriter"), "has interface javax.xml.stream.XMLStreamWriter");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("addAttrNamespace"), "has method addAttrNamespace");
        check(mn.contains("canReuse"), "has method canReuse");
        check(mn.contains("checkForNull"), "has method checkForNull");
        check(mn.contains("checkUserNamespaceContext"), "has method checkUserNamespaceContext");
        check(mn.contains("close"), "has method close");
        check(mn.contains("closeStartTag"), "has method closeStartTag");
        check(mn.contains("containsKey"), "has method containsKey");
        check(mn.contains("correctPrefix"), "has method correctPrefix");
        check(mn.contains("entrySet"), "has method entrySet");
        check(mn.contains("equals"), "has method equals");
        check(mn.contains("flush"), "has method flush");
        check(mn.contains("get"), "has method get");
        check(mn.contains("getAttrPrefix"), "has method getAttrPrefix");
        check(mn.contains("getEscapeCharacters"), "has method getEscapeCharacters");
        check(mn.contains("getNamespaceContext"), "has method getNamespaceContext");
        check(mn.contains("getPrefix"), "has method getPrefix");
        check(mn.contains("getProperty"), "has method getProperty");
        check(mn.contains("hashCode"), "has method hashCode");
        check(mn.contains("init"), "has method init");
        check(mn.contains("isDeclared"), "has method isDeclared");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
