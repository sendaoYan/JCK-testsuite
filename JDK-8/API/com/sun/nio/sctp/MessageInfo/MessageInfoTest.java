/*
 * @test
 * @summary com.sun.nio.sctp.MessageInfo conformance: hierarchy and declared members via reflection.
 * @run main MessageInfoTest
 */

public class MessageInfoTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.nio.sctp.MessageInfo", false, null);
        check(c.getName().equals("com.sun.nio.sctp.MessageInfo"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("address"), "declares address");
        check(ms.contains("association"), "declares association");
        check(ms.contains("bytes"), "declares bytes");
        check(ms.contains("complete"), "declares complete");
        check(ms.contains("createOutgoing"), "declares createOutgoing");
        check(ms.contains("isComplete"), "declares isComplete");
        check(ms.contains("isUnordered"), "declares isUnordered");
        check(ms.contains("payloadProtocolID"), "declares payloadProtocolID");
        check(ms.contains("streamNumber"), "declares streamNumber");
        check(ms.contains("timeToLive"), "declares timeToLive");
        check(ms.contains("unordered"), "declares unordered");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
