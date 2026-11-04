/*
 * @test
 * @summary sun.text.normalizer.Trie conformance: abstract class and members.
 * @run main TrieTest
 */

public class TrieTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.text.normalizer.Trie", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.text.normalizer.Trie"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("checkHeader"), "declares method checkHeader");
        check(mnames.contains("getBMPOffset"), "declares method getBMPOffset");
        check(mnames.contains("getCodePointOffset"), "declares method getCodePointOffset");
        check(mnames.contains("getInitialValue"), "declares method getInitialValue");
        check(mnames.contains("getLeadOffset"), "declares method getLeadOffset");
        check(mnames.contains("getRawOffset"), "declares method getRawOffset");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
