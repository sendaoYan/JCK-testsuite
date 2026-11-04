/*
 * @test
 * @summary com.sun.istack.internal.FragmentContentHandler conformance: hierarchy and declared members via reflection.
 * @run main FragmentContentHandlerTest
 */

public class FragmentContentHandlerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.istack.internal.FragmentContentHandler", false, null);
        check(c.getName().equals("com.sun.istack.internal.FragmentContentHandler"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("org.xml.sax.helpers.XMLFilterImpl"), "superclass org.xml.sax.helpers.XMLFilterImpl");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("endDocument"), "declares endDocument");
        check(ms.contains("startDocument"), "declares startDocument");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
