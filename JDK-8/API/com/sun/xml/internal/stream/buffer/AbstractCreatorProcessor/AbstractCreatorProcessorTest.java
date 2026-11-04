/*
 * @test
 * @summary com.sun.xml.internal.stream.buffer.AbstractCreatorProcessor conformance: abstract class hierarchy and members.
 * @run main AbstractCreatorProcessorTest
 */

public class AbstractCreatorProcessorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.stream.buffer.AbstractCreatorProcessor", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.stream.buffer.AbstractCreatorProcessor"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
