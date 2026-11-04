/*
 * @test
 * @summary sun.rmi.runtime.Log conformance: abstract class and members.
 * @run main LogTest
 */

public class LogTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.rmi.runtime.Log", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.rmi.runtime.Log"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("getLog"), "declares method getLog");
        check(mnames.contains("getPrintStream"), "declares method getPrintStream");
        check(mnames.contains("getSource"), "declares method getSource");
        check(mnames.contains("isLoggable"), "declares method isLoggable");
        check(mnames.contains("log"), "declares method log");
        check(mnames.contains("setOutputStream"), "declares method setOutputStream");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
