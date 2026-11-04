/*
 * @test
 * @summary com.sun.jndi.toolkit.dir.SearchFilter conformance: hierarchy and declared members via reflection.
 * @run main SearchFilterTest
 */

public class SearchFilterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jndi.toolkit.dir.SearchFilter", false, null);
        check(c.getName().equals("com.sun.jndi.toolkit.dir.SearchFilter"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("com.sun.jndi.toolkit.dir.AttrFilter"), "implements com.sun.jndi.toolkit.dir.AttrFilter");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("check"), "declares check");
        check(ms.contains("consumeChar"), "declares consumeChar");
        check(ms.contains("consumeChars"), "declares consumeChars");
        check(ms.contains("createNextFilter"), "declares createNextFilter");
        check(ms.contains("findUnescaped"), "declares findUnescaped");
        check(ms.contains("format"), "declares format");
        check(ms.contains("getCurrentChar"), "declares getCurrentChar");
        check(ms.contains("getEncodedStringRep"), "declares getEncodedStringRep");
        check(ms.contains("hexDigit"), "declares hexDigit");
        check(ms.contains("normalizeFilter"), "declares normalizeFilter");
        check(ms.contains("relCharAt"), "declares relCharAt");
        check(ms.contains("relIndexOf"), "declares relIndexOf");
        check(ms.contains("relSubstring"), "declares relSubstring");
        check(ms.contains("selectAttributes"), "declares selectAttributes");
        check(ms.contains("skipWhiteSpace"), "declares skipWhiteSpace");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
