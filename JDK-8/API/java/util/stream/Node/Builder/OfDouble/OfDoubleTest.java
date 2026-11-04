/*
 * @test
 * @summary java.util.stream.Node.Builder.OfDouble conformance: interface hierarchy and members.
 * @run main OfDoubleTest
 */


public class OfDoubleTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ===================== 契约深化：Node.Builder 原语特化面反射锁定 =====================
    static void testContracts() throws Exception {
        Class<?> c = Class.forName("java.util.stream.Node$Builder$OfDouble");
        check(c.isInterface(), "is interface");
        // accept(原语) 重载存在
        boolean primAccept = false;
        for (java.lang.reflect.Method m : c.getMethods()) {
            if (m.getName().equals("accept") && m.getParameterCount() == 1
                && m.getParameterTypes()[0] == double.class) primAccept = true;
        }
        check(primAccept, "accept(class) primitive overload present");
        check(java.util.Arrays.stream(c.getMethods()).anyMatch(m -> m.getName().equals("build")),
              "build() present");
        boolean extendsSink = false;
        for (Class<?> i : c.getInterfaces()) if (i.getName().startsWith("java.util.stream.Sink")) extendsSink = true;
        check(extendsSink, "extends a Sink specialization");
    }

    public static void main(String[] args) throws Exception {
        testContracts();
        Class<?> c = Class.forName("java.util.stream.Node$Builder$OfDouble");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> expIfs = new java.util.HashSet<String>();
        expIfs.add("java.util.stream.Node$Builder");
        expIfs.add("java.util.stream.Sink$OfDouble");
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
