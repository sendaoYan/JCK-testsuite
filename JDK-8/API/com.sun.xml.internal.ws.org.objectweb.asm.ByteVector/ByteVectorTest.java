/*
 * @test
 * @summary com.sun.xml.internal.ws.org.objectweb.asm.ByteVector conformance: reflection (kind, superclass, interfaces, methods).
 * @run main ByteVectorTest
 */

import java.lang.reflect.Modifier;


public class ByteVectorTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.org.objectweb.asm.ByteVector", false, ByteVectorTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.org.objectweb.asm.ByteVector"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("putByte"), "method putByte");
        check(mn.contains("putByteArray"), "method putByteArray");
        check(mn.contains("putInt"), "method putInt");
        check(mn.contains("putLong"), "method putLong");
        check(mn.contains("putShort"), "method putShort");
        check(mn.contains("putUTF8"), "method putUTF8");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
