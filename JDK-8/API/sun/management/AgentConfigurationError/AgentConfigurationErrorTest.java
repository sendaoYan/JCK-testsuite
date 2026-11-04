/*
 * @test
 * @summary sun.management.AgentConfigurationError conformance: exception/error hierarchy and construction.
 * @run main AgentConfigurationErrorTest
 */

public class AgentConfigurationErrorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.management.AgentConfigurationError", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.management.AgentConfigurationError"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Error"), "superclass java.lang.Error");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("getError"), "declares method getError");
        check(mnames.contains("getParams"), "declares method getParams");
        sun.management.AgentConfigurationError x = new sun.management.AgentConfigurationError("msg");
        check(x instanceof Throwable, "is a Throwable");
        check(x.toString() != null, "toString non-null");
        x.getMessage(); x.getCause(); x.getStackTrace();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
