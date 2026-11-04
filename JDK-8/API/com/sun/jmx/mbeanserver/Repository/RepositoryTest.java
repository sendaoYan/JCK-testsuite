/*
 * @test
 * @summary com.sun.jmx.mbeanserver.Repository conformance: hierarchy and declared members via reflection.
 * @run main RepositoryTest
 */

public class RepositoryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jmx.mbeanserver.Repository", false, null);
        check(c.getName().equals("com.sun.jmx.mbeanserver.Repository"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("addAllMatching"), "declares addAllMatching");
        check(ms.contains("addMBean"), "declares addMBean");
        check(ms.contains("addMoiToTb"), "declares addMoiToTb");
        check(ms.contains("addNewDomMoi"), "declares addNewDomMoi");
        check(ms.contains("contains"), "declares contains");
        check(ms.contains("getCount"), "declares getCount");
        check(ms.contains("getDefaultDomain"), "declares getDefaultDomain");
        check(ms.contains("getDomains"), "declares getDomains");
        check(ms.contains("query"), "declares query");
        check(ms.contains("registering"), "declares registering");
        check(ms.contains("remove"), "declares remove");
        check(ms.contains("retrieve"), "declares retrieve");
        check(ms.contains("retrieveNamedObject"), "declares retrieveNamedObject");
        check(ms.contains("unregistering"), "declares unregistering");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
