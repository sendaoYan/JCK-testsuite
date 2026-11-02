/*
 * @test
 * @summary com.sun.xml.internal.org.jvnet.staxex.StreamingDataHandler conformance: abstract class hierarchy and members.
 * @run main StreamingDataHandlerTest
 */

public class StreamingDataHandlerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.org.jvnet.staxex.StreamingDataHandler", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.org.jvnet.staxex.StreamingDataHandler"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check("javax.activation.DataHandler".equals(c.getSuperclass().getName()), "extends javax.activation.DataHandler");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "direct interface count");
        check(ifs.contains("java.io.Closeable"), "has interface java.io.Closeable");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("close"), "has method close");
        check(mn.contains("moveTo"), "has method moveTo");
        check(mn.contains("readOnce"), "has method readOnce");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
