/*
 * @test
 * @summary com.sun.xml.internal.org.jvnet.fastinfoset.sax.PrimitiveTypeContentHandler conformance: interface hierarchy and members.
 * @run main PrimitiveTypeContentHandlerTest
 */

public class PrimitiveTypeContentHandlerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.org.jvnet.fastinfoset.sax.PrimitiveTypeContentHandler", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.org.jvnet.fastinfoset.sax.PrimitiveTypeContentHandler"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("booleans"), "has method booleans");
        check(mn.contains("bytes"), "has method bytes");
        check(mn.contains("doubles"), "has method doubles");
        check(mn.contains("floats"), "has method floats");
        check(mn.contains("ints"), "has method ints");
        check(mn.contains("longs"), "has method longs");
        check(mn.contains("shorts"), "has method shorts");
        check(mn.contains("uuids"), "has method uuids");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
