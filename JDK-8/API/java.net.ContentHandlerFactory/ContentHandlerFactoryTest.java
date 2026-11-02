/*
 * @test
 * @summary java.net.ContentHandlerFactory conformance: interface hierarchy and members.
 * @run main ContentHandlerFactoryTest
 */


public class ContentHandlerFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.net.ContentHandlerFactory");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("createContentHandler"), "has method createContentHandler");        System.out.println("PASS (" + passed + " assertions)");
    }
}
