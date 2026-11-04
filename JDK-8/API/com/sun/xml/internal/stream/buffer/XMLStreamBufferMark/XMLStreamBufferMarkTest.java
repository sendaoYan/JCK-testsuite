/*
 * @test
 * @summary com.sun.xml.internal.stream.buffer.XMLStreamBufferMark conformance: class hierarchy and members.
 * @run main XMLStreamBufferMarkTest
 */

public class XMLStreamBufferMarkTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.stream.buffer.XMLStreamBufferMark", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.stream.buffer.XMLStreamBufferMark"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("com.sun.xml.internal.stream.buffer.XMLStreamBuffer".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.stream.buffer.XMLStreamBuffer");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
