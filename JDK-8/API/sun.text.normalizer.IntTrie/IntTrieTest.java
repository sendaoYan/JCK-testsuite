/*
 * @test
 * @summary sun.text.normalizer.IntTrie conformance: class and members.
 * @run main IntTrieTest
 */

public class IntTrieTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.text.normalizer.IntTrie", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.text.normalizer.IntTrie"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.text.normalizer.Trie"), "superclass sun.text.normalizer.Trie");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("getCodePointValue"), "declares method getCodePointValue");
        check(mnames.contains("getInitialValue"), "declares method getInitialValue");
        check(mnames.contains("getLeadValue"), "declares method getLeadValue");
        check(mnames.contains("getSurrogateOffset"), "declares method getSurrogateOffset");
        check(mnames.contains("getTrailValue"), "declares method getTrailValue");
        check(mnames.contains("getValue"), "declares method getValue");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
