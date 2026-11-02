/*
 * @test
 * @summary com.sun.xml.internal.org.jvnet.fastinfoset.stax.FastInfosetStreamReader conformance: interface hierarchy and members.
 * @run main FastInfosetStreamReaderTest
 */

public class FastInfosetStreamReaderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.org.jvnet.fastinfoset.stax.FastInfosetStreamReader", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.org.jvnet.fastinfoset.stax.FastInfosetStreamReader"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("accessLocalName"), "has method accessLocalName");
        check(mn.contains("accessNamespaceCount"), "has method accessNamespaceCount");
        check(mn.contains("accessNamespaceURI"), "has method accessNamespaceURI");
        check(mn.contains("accessPrefix"), "has method accessPrefix");
        check(mn.contains("accessTextCharacters"), "has method accessTextCharacters");
        check(mn.contains("accessTextLength"), "has method accessTextLength");
        check(mn.contains("accessTextStart"), "has method accessTextStart");
        check(mn.contains("peekNext"), "has method peekNext");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
