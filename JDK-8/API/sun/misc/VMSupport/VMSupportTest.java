/*
 * @test
 * @summary sun.misc.VMSupport conformance: class and members.
 * @run main VMSupportTest
 */

public class VMSupportTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.misc.VMSupport", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.misc.VMSupport"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("getAgentProperties"), "declares method getAgentProperties");
        check(mnames.contains("getVMTemporaryDirectory"), "declares method getVMTemporaryDirectory");
        check(mnames.contains("initAgentProperties"), "declares method initAgentProperties");
        check(mnames.contains("isClassPathAttributePresent"), "declares method isClassPathAttributePresent");
        check(mnames.contains("serializeAgentPropertiesToByteArray"), "declares method serializeAgentPropertiesToByteArray");
        check(mnames.contains("serializePropertiesToByteArray"), "declares method serializePropertiesToByteArray");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
