/*
 * @test
 * @summary sun.nio.cs.ext.JISAutoDetect conformance: class and members.
 * @run main JISAutoDetectTest
 */

public class JISAutoDetectTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.cs.ext.JISAutoDetect", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.nio.cs.ext.JISAutoDetect"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.nio.charset.Charset"), "superclass java.nio.charset.Charset");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("sun.nio.cs.HistoricallyNamedCharset"), "interface sun.nio.cs.HistoricallyNamedCharset");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("canBeEUCJP"), "declares method canBeEUCJP");
        check(mnames.contains("canBeEUCKana"), "declares method canBeEUCKana");
        check(mnames.contains("canBeSJIS1B"), "declares method canBeSJIS1B");
        check(mnames.contains("canEncode"), "declares method canEncode");
        check(mnames.contains("contains"), "declares method contains");
        check(mnames.contains("getByteMask1"), "declares method getByteMask1");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
