/*
 * @test
 * @summary java.util.stream.Node.OfPrimitive conformance: interface hierarchy and members.
 * @run main OfPrimitiveTest
 */


public class OfPrimitiveTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ===================== 契约深化：Node 契约面反射锁定 =====================
    static void testContracts() throws Exception {
        Class<?> c = Class.forName("java.util.stream.Node$OfPrimitive");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.containsAll(java.util.Arrays.asList("asArray", "spliterator", "count", "forEach",
              "copyInto", "getChild", "getChildCount", "getShape", "truncate")),
              "Node full contract method set present");
        check(java.util.Arrays.stream(c.getMethods())
              .filter(m -> m.getName().equals("spliterator")).count() >= 1,
              "spliterator exposed");
        // Node.Builder 继承 Sink
        Class<?> builder = Class.forName("java.util.stream.Node$Builder");
        check(builder.isInterface(), "Node.Builder is interface");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : builder.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("java.util.stream.Sink"), "Node.Builder extends Sink");
        check(java.util.Arrays.stream(builder.getMethods()).anyMatch(m -> m.getName().equals("build")),
              "Node.Builder has build()");
    }

    public static void main(String[] args) throws Exception {
        testContracts();
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
