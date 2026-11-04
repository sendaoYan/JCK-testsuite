/*
 * @test
 * @summary com.sun.xml.internal.messaging.saaj.soap.MessageFactoryImpl conformance: class hierarchy and members.
 * @run main MessageFactoryImplTest
 */

public class MessageFactoryImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.messaging.saaj.soap.MessageFactoryImpl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.messaging.saaj.soap.MessageFactoryImpl"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("javax.xml.soap.MessageFactory".equals(c.getSuperclass().getName()), "extends javax.xml.soap.MessageFactory");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("createMessage"), "has method createMessage");
        check(mn.contains("getContentType"), "has method getContentType");
        check(mn.contains("listen"), "has method listen");
        check(mn.contains("setLazyAttachmentOptimization"), "has method setLazyAttachmentOptimization");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
