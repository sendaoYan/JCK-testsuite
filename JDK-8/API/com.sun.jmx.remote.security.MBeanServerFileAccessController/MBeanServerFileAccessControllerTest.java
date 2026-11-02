/*
 * @test
 * @summary com.sun.jmx.remote.security.MBeanServerFileAccessController conformance: hierarchy and declared members via reflection.
 * @run main MBeanServerFileAccessControllerTest
 */

public class MBeanServerFileAccessControllerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jmx.remote.security.MBeanServerFileAccessController", false, null);
        check(c.getName().equals("com.sun.jmx.remote.security.MBeanServerFileAccessController"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.jmx.remote.security.MBeanServerAccessController"), "superclass com.sun.jmx.remote.security.MBeanServerAccessController");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("checkAccess"), "declares checkAccess");
        check(ms.contains("checkCreate"), "declares checkCreate");
        check(ms.contains("checkCreateAccess"), "declares checkCreateAccess");
        check(ms.contains("checkRead"), "declares checkRead");
        check(ms.contains("checkUnregister"), "declares checkUnregister");
        check(ms.contains("checkWrite"), "declares checkWrite");
        check(ms.contains("classNameMatch"), "declares classNameMatch");
        check(ms.contains("parseProperties"), "declares parseProperties");
        check(ms.contains("propertiesFromFile"), "declares propertiesFromFile");
        check(ms.contains("refresh"), "declares refresh");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
