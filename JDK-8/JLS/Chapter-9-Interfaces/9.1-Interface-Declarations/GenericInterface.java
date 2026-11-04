/*
 * @test
 * @summary JLS 9.1.2 (Java SE 8): an interface may be generic; type arguments are
 *          substituted at each use and erased at runtime.
 * @run main GenericInterface
 */

public class GenericInterface {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface Box<T> {
        T get();
    }

    static class StringBox implements Box<String> {
        public String get() { return "hello"; }
    }
    static class IntBox implements Box<Integer> {
        public Integer get() { return 42; }
    }

    public static void main(String[] args) {
        Box<String> sb = new StringBox();
        Box<Integer> ib = new IntBox();
        check(sb.get().equals("hello"), "String type argument");
        check(ib.get().equals(42), "Integer type argument");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
