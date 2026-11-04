/*
 * @test
 * @summary java.util.stream.Sink.ChainedLong conformance: abstract class hierarchy and members.
 * @run main ChainedLongTest
 */


public class ChainedLongTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ===================== 契约深化：ChainedLong 链式 Sink 契约面反射锁定 =====================
    static void testContracts() throws Exception {
        Class<?> c = Class.forName("java.util.stream.Sink$ChainedLong");
        check(!c.isInterface(), "ChainedLong is an abstract class (not interface)");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        // 构造语义：单个构造器接收被包裹的下游 Sink
        java.lang.reflect.Constructor<?>[] ctors = c.getDeclaredConstructors();
        check(ctors.length == 1, "single declared constructor (wraps downstream)");
        // 生命周期方法委托下游
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("begin") && names.contains("end") && names.contains("cancellationRequested"),
              "begin/end/cancellationRequested available");
    }

    public static void main(String[] args) throws Exception {
        testContracts();
        Class<?> c = Class.forName("java.util.stream.Sink$ChainedLong");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(!c.isInterface(), "not interface");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        java.util.Set<String> expIfs = new java.util.HashSet<String>();
        expIfs.add("java.util.stream.Sink$OfLong");
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
