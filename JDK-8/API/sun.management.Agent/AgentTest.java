/*
 * @test
 * @summary sun.management.Agent conformance: class and members.
 * @run main AgentTest
 */

public class AgentTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.management.Agent", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.management.Agent"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("agentmain"), "declares method agentmain");
        check(mnames.contains("error"), "declares method error");
        check(mnames.contains("getManagementProperties"), "declares method getManagementProperties");
        check(mnames.contains("getText"), "declares method getText");
        check(mnames.contains("initResource"), "declares method initResource");
        check(mnames.contains("loadManagementProperties"), "declares method loadManagementProperties");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
