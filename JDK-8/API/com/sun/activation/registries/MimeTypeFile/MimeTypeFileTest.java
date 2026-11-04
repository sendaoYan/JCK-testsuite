/*
 * @test
 * @summary com.sun.activation.registries.MimeTypeFile conformance: hierarchy and declared members via reflection.
 * @run main MimeTypeFileTest
 */

public class MimeTypeFileTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.activation.registries.MimeTypeFile", false, null);
        check(c.getName().equals("com.sun.activation.registries.MimeTypeFile"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("appendToRegistry"), "declares appendToRegistry");
        check(ms.contains("getMIMETypeString"), "declares getMIMETypeString");
        check(ms.contains("getMimeTypeEntry"), "declares getMimeTypeEntry");
        check(ms.contains("parse"), "declares parse");
        check(ms.contains("parseEntry"), "declares parseEntry");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
