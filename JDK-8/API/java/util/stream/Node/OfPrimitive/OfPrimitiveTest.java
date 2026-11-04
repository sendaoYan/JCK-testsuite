/*
 * @test
 * @summary java.util.stream.Node.OfPrimitive conformance: interface hierarchy and members.
 * @run main OfPrimitiveTest
 */


public class OfPrimitiveTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.util.stream.Node$OfPrimitive");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> expIfs = new java.util.HashSet<String>();
        expIfs.add("java.util.stream.Node");
        java.util.Set<String> actIfs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) actIfs.add(i.getName());
        check(actIfs.equals(expIfs), "direct interfaces");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("asArray"), "has method asArray");
        check(names.contains("asPrimitiveArray"), "has method asPrimitiveArray");
        check(names.contains("copyInto"), "has method copyInto");        System.out.println("PASS (" + passed + " assertions)");
    }
}
