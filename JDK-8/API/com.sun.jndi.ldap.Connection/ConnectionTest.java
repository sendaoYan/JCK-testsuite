/*
 * @test
 * @summary com.sun.jndi.ldap.Connection conformance: hierarchy and declared members via reflection.
 * @run main ConnectionTest
 */

public class ConnectionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jndi.ldap.Connection", false, null);
        check(c.getName().equals("com.sun.jndi.ldap.Connection"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("java.lang.Runnable"), "implements java.lang.Runnable");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("abandonOutstandingReqs"), "declares abandonOutstandingReqs");
        check(ms.contains("abandonRequest"), "declares abandonRequest");
        check(ms.contains("addRequest"), "declares addRequest");
        check(ms.contains("cleanup"), "declares cleanup");
        check(ms.contains("createInetSocketAddress"), "declares createInetSocketAddress");
        check(ms.contains("createSocket"), "declares createSocket");
        check(ms.contains("findRequest"), "declares findRequest");
        check(ms.contains("getInputStream"), "declares getInputStream");
        check(ms.contains("getMsgId"), "declares getMsgId");
        check(ms.contains("hostnameVerificationDisabledValue"), "declares hostnameVerificationDisabledValue");
        check(ms.contains("isUpgradedToStartTls"), "declares isUpgradedToStartTls");
        check(ms.contains("ldapUnbind"), "declares ldapUnbind");
        check(ms.contains("pauseReader"), "declares pauseReader");
        check(ms.contains("readFully"), "declares readFully");
        check(ms.contains("readReply"), "declares readReply");
        check(ms.contains("removeRequest"), "declares removeRequest");
        check(ms.contains("replaceStreams"), "declares replaceStreams");
        check(ms.contains("run"), "declares run");
        check(ms.contains("setBound"), "declares setBound");
        check(ms.contains("setV3"), "declares setV3");
        check(ms.contains("unpauseReader"), "declares unpauseReader");
        check(ms.contains("writeRequest"), "declares writeRequest");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
