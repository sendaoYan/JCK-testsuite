/*
 * @test
 * @summary sun.nio.ch.sctp.MessageInfoImpl conformance: class and members.
 * @run main MessageInfoImplTest
 */

public class MessageInfoImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.ch.sctp.MessageInfoImpl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.nio.ch.sctp.MessageInfoImpl"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.nio.sctp.MessageInfo"), "superclass com.sun.nio.sctp.MessageInfo");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("address"), "declares method address");
        check(mnames.contains("association"), "declares method association");
        check(mnames.contains("associationID"), "declares method associationID");
        check(mnames.contains("bytes"), "declares method bytes");
        check(mnames.contains("complete"), "declares method complete");
        check(mnames.contains("isComplete"), "declares method isComplete");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
