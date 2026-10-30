/*
 * @test
 * @summary JLS 9.1.2: all parameterizations of a generic interface share the same
 *          interface at run time (type erasure).
 * @run main GenericInterfaceSharedRuntime
 */

public class GenericInterfaceSharedRuntime {
    interface Box<T> { T get(); }

    static class StringBox implements Box<String> {
        public String get() { return "x"; }
    }

    static class IntegerBox implements Box<Integer> {
        public Integer get() { return 1; }
    }

    public static void main(String[] args) {
        Box<String> s = new StringBox();
        Box<Integer> i = new IntegerBox();
        if (!"x".equals(s.get())) throw new AssertionError("string box");
        if (i.get() != 1) throw new AssertionError("integer box");
        // Both parameterizations are implemented by the single erased Box interface.
        if (StringBox.class.getInterfaces()[0] != Box.class)
            throw new AssertionError("StringBox interface");
        if (IntegerBox.class.getInterfaces()[0] != Box.class)
            throw new AssertionError("IntegerBox interface");
        if (StringBox.class.getInterfaces()[0] != IntegerBox.class.getInterfaces()[0])
            throw new AssertionError("shared runtime interface");
        System.out.println("PASS");
    }
}
