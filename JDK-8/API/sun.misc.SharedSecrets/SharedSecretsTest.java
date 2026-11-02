/*
 * @test
 * @summary sun.misc.SharedSecrets conformance: class and members.
 * @run main SharedSecretsTest
 */

public class SharedSecretsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.misc.SharedSecrets", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.misc.SharedSecrets"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("getJavaAWTAccess"), "declares method getJavaAWTAccess");
        check(mnames.contains("getJavaIOAccess"), "declares method getJavaIOAccess");
        check(mnames.contains("getJavaIOFileDescriptorAccess"), "declares method getJavaIOFileDescriptorAccess");
        check(mnames.contains("getJavaLangAccess"), "declares method getJavaLangAccess");
        check(mnames.contains("getJavaLangRefAccess"), "declares method getJavaLangRefAccess");
        check(mnames.contains("getJavaNetAccess"), "declares method getJavaNetAccess");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
