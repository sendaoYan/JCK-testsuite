/*
 * @test
 * @summary com.sun.xml.internal.fastinfoset.util.CharArrayIntMap conformance: class hierarchy and members.
 * @run main CharArrayIntMapTest
 */

public class CharArrayIntMapTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.fastinfoset.util.CharArrayIntMap", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.fastinfoset.util.CharArrayIntMap"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("com.sun.xml.internal.fastinfoset.util.KeyIntMap".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.fastinfoset.util.KeyIntMap");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("addEntry"), "has method addEntry");
        check(mn.contains("clear"), "has method clear");
        check(mn.contains("get"), "has method get");
        check(mn.contains("getTotalCharacterCount"), "has method getTotalCharacterCount");
        check(mn.contains("obtainIndex"), "has method obtainIndex");
        check(mn.contains("resize"), "has method resize");
        check(mn.contains("setReadOnlyMap"), "has method setReadOnlyMap");
        check(mn.contains("transfer"), "has method transfer");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
