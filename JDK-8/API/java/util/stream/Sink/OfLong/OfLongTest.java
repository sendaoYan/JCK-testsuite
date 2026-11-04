/*
 * @test
 * @summary java.util.stream.Sink.OfLong conformance: interface hierarchy and members.
 * @run main OfLongTest
 */


public class OfLongTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ===================== 契约深化：默认方法语义（begin/end/cancellationRequested）反射锁定 =====================
    static void testContracts() throws Exception {
        Class<?> c = Class.forName("java.util.stream.Sink");
        java.util.Set<String> defaults = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (m.isDefault()) defaults.add(m.getName());
        }
        check(defaults.contains("begin"), "begin is default (no-op)");
        check(defaults.contains("end"), "end is default (no-op)");
        check(defaults.contains("cancellationRequested"), "cancellationRequested is default (false)");
        check(defaults.contains("accept"), "accept(Object) is default (throws IllegalStateException)");
        java.lang.reflect.Method cc = c.getDeclaredMethod("cancellationRequested");
        check(cc.getReturnType() == boolean.class, "cancellationRequested returns boolean");
        check(c.getDeclaredMethod("begin", long.class).getReturnType() == void.class, "begin(long) returns void");
        check(c.getDeclaredMethod("end").getReturnType() == void.class, "end() returns void");
    }

    public static void main(String[] args) throws Exception {
        testContracts();
        Class<?> c = Class.forName("java.util.stream.Sink$OfLong");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> expIfs = new java.util.HashSet<String>();
        expIfs.add("java.util.function.LongConsumer");
        expIfs.add("java.util.stream.Sink");
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
