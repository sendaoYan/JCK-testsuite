/*
 * @test
 * @summary com.sun.activation.registries.MailcapTokenizer conformance: hierarchy and declared members via reflection.
 * @run main MailcapTokenizerTest
 */

public class MailcapTokenizerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.activation.registries.MailcapTokenizer", false, null);
        check(c.getName().equals("com.sun.activation.registries.MailcapTokenizer"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("fixEscapeSequences"), "declares fixEscapeSequences");
        check(ms.contains("getCurrentToken"), "declares getCurrentToken");
        check(ms.contains("getCurrentTokenValue"), "declares getCurrentTokenValue");
        check(ms.contains("isControlChar"), "declares isControlChar");
        check(ms.contains("isSpecialChar"), "declares isSpecialChar");
        check(ms.contains("isStringTokenChar"), "declares isStringTokenChar");
        check(ms.contains("isWhiteSpaceChar"), "declares isWhiteSpaceChar");
        check(ms.contains("nameForToken"), "declares nameForToken");
        check(ms.contains("nextToken"), "declares nextToken");
        check(ms.contains("processAutoquoteToken"), "declares processAutoquoteToken");
        check(ms.contains("processStringToken"), "declares processStringToken");
        check(ms.contains("setIsAutoquoting"), "declares setIsAutoquoting");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
