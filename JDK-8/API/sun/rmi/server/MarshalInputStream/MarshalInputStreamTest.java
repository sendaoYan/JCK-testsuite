/*
 * @test
 * @summary sun.rmi.server.MarshalInputStream conformance: class and members.
 * @run main MarshalInputStreamTest
 */

public class MarshalInputStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.rmi.server.MarshalInputStream", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.rmi.server.MarshalInputStream"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.io.ObjectInputStream"), "superclass java.io.ObjectInputStream");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("checkSunClass"), "declares method checkSunClass");
        check(mnames.contains("close"), "declares method close");
        check(mnames.contains("done"), "declares method done");
        check(mnames.contains("getDoneCallback"), "declares method getDoneCallback");
        check(mnames.contains("latestUserDefinedLoader"), "declares method latestUserDefinedLoader");
        check(mnames.contains("readClassDescriptor"), "declares method readClassDescriptor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
