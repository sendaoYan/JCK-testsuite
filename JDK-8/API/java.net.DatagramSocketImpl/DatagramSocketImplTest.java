/*
 * @test
 * @summary java.net.DatagramSocketImpl conformance: abstract class hierarchy and members.
 * @run main DatagramSocketImplTest
 */


public class DatagramSocketImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.net.DatagramSocketImpl");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(!c.isInterface(), "not interface");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        java.util.Set<String> expIfs = new java.util.HashSet<String>();
        expIfs.add("java.net.SocketOptions");
        java.util.Set<String> actIfs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) actIfs.add(i.getName());
        check(actIfs.equals(expIfs), "direct interfaces");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getOption"), "has method getOption");
        check(names.contains("setOption"), "has method setOption");        System.out.println("PASS (" + passed + " assertions)");
    }
}
