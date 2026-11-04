/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.xpath.regex.REUtil conformance: class hierarchy and members.
 * @run main REUtilTest
 */
public class REUtilTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.xpath.regex.REUtil", false, REUtilTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("composeFromSurrogates"), "method composeFromSurrogates");
        check(mn.contains("isLowSurrogate"), "method isLowSurrogate");
        check(mn.contains("isHighSurrogate"), "method isHighSurrogate");
        check(mn.contains("decomposeToSurrogates"), "method decomposeToSurrogates");
        check(mn.contains("substring"), "method substring");
        check(mn.contains("getOptionValue"), "method getOptionValue");
        check(mn.contains("parseOptions"), "method parseOptions");
        check(mn.contains("createOptionString"), "method createOptionString");
        check(mn.contains("stripExtendedComment"), "method stripExtendedComment");
        check(mn.contains("main"), "method main");
        check(mn.contains("createRegex"), "method createRegex");
        check(mn.contains("matches"), "method matches");
        check(mn.contains("quoteMeta"), "method quoteMeta");
        check(mn.contains("dumpString"), "method dumpString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
