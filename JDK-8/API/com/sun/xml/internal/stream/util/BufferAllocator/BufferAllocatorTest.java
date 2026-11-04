/*
 * @test
 * @summary com.sun.xml.internal.stream.util.BufferAllocator conformance: class hierarchy and members.
 * @run main BufferAllocatorTest
 */

public class BufferAllocatorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.stream.util.BufferAllocator", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.stream.util.BufferAllocator"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getByteBuffer"), "has method getByteBuffer");
        check(mn.contains("getCharBuffer"), "has method getCharBuffer");
        check(mn.contains("returnByteBuffer"), "has method returnByteBuffer");
        check(mn.contains("returnCharBuffer"), "has method returnCharBuffer");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
