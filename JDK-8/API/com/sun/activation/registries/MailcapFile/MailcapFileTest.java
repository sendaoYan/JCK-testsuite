/*
 * @test
 * @summary com.sun.activation.registries.MailcapFile conformance: hierarchy and declared members via reflection.
 * @run main MailcapFileTest
 */

public class MailcapFileTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.activation.registries.MailcapFile", false, null);
        check(c.getName().equals("com.sun.activation.registries.MailcapFile"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("appendToMailcap"), "declares appendToMailcap");
        check(ms.contains("getMailcapFallbackList"), "declares getMailcapFallbackList");
        check(ms.contains("getMailcapList"), "declares getMailcapList");
        check(ms.contains("getMimeTypes"), "declares getMimeTypes");
        check(ms.contains("getNativeCommands"), "declares getNativeCommands");
        check(ms.contains("mergeResults"), "declares mergeResults");
        check(ms.contains("parse"), "declares parse");
        check(ms.contains("parseLine"), "declares parseLine");
        check(ms.contains("reportParseError"), "declares reportParseError");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
