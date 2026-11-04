/*
 * @test
 * @summary java.util.stream.Node.Builder.OfInt conformance: interface hierarchy and members.
 * @run main OfIntTest
 */


public class OfIntTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.util.stream.Node$Builder$OfInt");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> expIfs = new java.util.HashSet<String>();
        expIfs.add("java.util.stream.Node$Builder");
        expIfs.add("java.util.stream.Sink$OfInt");
        java.util.Set<String> actIfs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) actIfs.add(i.getName());
        check(actIfs.equals(expIfs), "direct interfaces");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("accept"), "has method accept");
        check(names.contains("andThen"), "has method andThen");
        check(names.contains("begin"), "has method begin");        System.out.println("PASS (" + passed + " assertions)");
    }
}
