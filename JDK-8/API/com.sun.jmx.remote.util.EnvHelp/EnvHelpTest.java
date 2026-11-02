/*
 * @test
 * @summary com.sun.jmx.remote.util.EnvHelp conformance: hierarchy and declared members via reflection.
 * @run main EnvHelpTest
 */

public class EnvHelpTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jmx.remote.util.EnvHelp", false, null);
        check(c.getName().equals("com.sun.jmx.remote.util.EnvHelp"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("checkAttributes"), "declares checkAttributes");
        check(ms.contains("computeBooleanFromString"), "declares computeBooleanFromString");
        check(ms.contains("filterAttributes"), "declares filterAttributes");
        check(ms.contains("getCause"), "declares getCause");
        check(ms.contains("getConnectionCheckPeriod"), "declares getConnectionCheckPeriod");
        check(ms.contains("getFetchTimeout"), "declares getFetchTimeout");
        check(ms.contains("getIntegerAttribute"), "declares getIntegerAttribute");
        check(ms.contains("getMaxFetchNotifNumber"), "declares getMaxFetchNotifNumber");
        check(ms.contains("getNotifBufferSize"), "declares getNotifBufferSize");
        check(ms.contains("getNotificationAccessController"), "declares getNotificationAccessController");
        check(ms.contains("getServerConnectionTimeout"), "declares getServerConnectionTimeout");
        check(ms.contains("hideAttributes"), "declares hideAttributes");
        check(ms.contains("initCause"), "declares initCause");
        check(ms.contains("isServerDaemon"), "declares isServerDaemon");
        check(ms.contains("mapToHashtable"), "declares mapToHashtable");
        check(ms.contains("parseHiddenAttributes"), "declares parseHiddenAttributes");
        check(ms.contains("purgeUnserializable"), "declares purgeUnserializable");
        check(ms.contains("resolveClientClassLoader"), "declares resolveClientClassLoader");
        check(ms.contains("resolveServerClassLoader"), "declares resolveServerClassLoader");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
