/*
 * @test
 * @summary sun.util.PreHashedMap conformance: abstract class and members.
 * @run main PreHashedMapTest
 */

public class PreHashedMapTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.PreHashedMap", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.util.PreHashedMap"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.util.AbstractMap"), "superclass java.util.AbstractMap");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("entrySet"), "declares method entrySet");
        check(mnames.contains("get"), "declares method get");
        check(mnames.contains("init"), "declares method init");
        check(mnames.contains("keySet"), "declares method keySet");
        check(mnames.contains("put"), "declares method put");
        check(mnames.contains("toV"), "declares method toV");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
