/*
 * @test
 * @summary JLS 9.4.2 (Java SE 8): interface methods may be overloaded; the most
 *          specific method is chosen at compile time.
 * @run main InterfaceMethodOverloading
 */

public class InterfaceMethodOverloading {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface I {
        String m(int i);
        String m(String s);
        String m(int i, String s);
    }

    static class Impl implements I {
        public String m(int i) { return "int"; }
        public String m(String s) { return "String"; }
        public String m(int i, String s) { return "int,String"; }
    }

    public static void main(String[] args) {
        I i = new Impl();
        check(i.m(1).equals("int"), "int overload");
        check(i.m("x").equals("String"), "String overload");
        check(i.m(1, "x").equals("int,String"), "two-arg overload");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
