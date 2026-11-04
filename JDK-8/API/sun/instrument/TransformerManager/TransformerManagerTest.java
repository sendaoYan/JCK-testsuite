/*
 * @test
 * @summary sun.instrument.TransformerManager conformance: class and members.
 * @run main TransformerManagerTest
 */

public class TransformerManagerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.instrument.TransformerManager", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.instrument.TransformerManager"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("addTransformer"), "declares method addTransformer");
        check(mnames.contains("getNativeMethodPrefixes"), "declares method getNativeMethodPrefixes");
        check(mnames.contains("getSnapshotTransformerList"), "declares method getSnapshotTransformerList");
        check(mnames.contains("getTransformerCount"), "declares method getTransformerCount");
        check(mnames.contains("includesTransformer"), "declares method includesTransformer");
        check(mnames.contains("isRetransformable"), "declares method isRetransformable");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
