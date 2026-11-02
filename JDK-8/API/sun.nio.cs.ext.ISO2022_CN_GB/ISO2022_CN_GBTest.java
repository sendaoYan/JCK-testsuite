/*
 * @test
 * @summary sun.nio.cs.ext.ISO2022_CN_GB conformance: class and members.
 * @run main ISO2022_CN_GBTest
 */

public class ISO2022_CN_GBTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.cs.ext.ISO2022_CN_GB", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.nio.cs.ext.ISO2022_CN_GB"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.nio.cs.ext.ISO2022"), "superclass sun.nio.cs.ext.ISO2022");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("sun.nio.cs.HistoricallyNamedCharset"), "interface sun.nio.cs.HistoricallyNamedCharset");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("contains"), "declares method contains");
        check(mnames.contains("historicalName"), "declares method historicalName");
        check(mnames.contains("newDecoder"), "declares method newDecoder");
        check(mnames.contains("newEncoder"), "declares method newEncoder");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
