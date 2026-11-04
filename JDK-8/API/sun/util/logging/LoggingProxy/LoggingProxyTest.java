/*
 * @test
 * @summary sun.util.logging.LoggingProxy conformance: interface and members.
 * @run main LoggingProxyTest
 */

public class LoggingProxyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.logging.LoggingProxy", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.util.logging.LoggingProxy"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("getLevel"), "declares method getLevel");
        check(mnames.contains("getLevelName"), "declares method getLevelName");
        check(mnames.contains("getLevelValue"), "declares method getLevelValue");
        check(mnames.contains("getLogger"), "declares method getLogger");
        check(mnames.contains("getLoggerLevel"), "declares method getLoggerLevel");
        check(mnames.contains("getLoggerNames"), "declares method getLoggerNames");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
