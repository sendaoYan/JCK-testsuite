/*
 * @test
 * @summary sun.rmi.server.Util conformance: class and members.
 * @run main UtilTest
 */

public class UtilTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.rmi.server.Util", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.rmi.server.Util"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("checkMethod"), "declares method checkMethod");
        check(mnames.contains("computeMethodHash"), "declares method computeMethodHash");
        check(mnames.contains("createProxy"), "declares method createProxy");
        check(mnames.contains("createSkeleton"), "declares method createSkeleton");
        check(mnames.contains("createStub"), "declares method createStub");
        check(mnames.contains("getMethodNameAndDescriptor"), "declares method getMethodNameAndDescriptor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
