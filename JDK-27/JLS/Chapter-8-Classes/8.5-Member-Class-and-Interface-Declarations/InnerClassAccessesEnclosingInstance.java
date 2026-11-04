/*
 * @test
 * @summary JLS 8.5: an inner class can refer to instance members of its enclosing
 *          class by simple name, and can name the enclosing instance explicitly
 *          as Outer.this.
 * @run main InnerClassAccessesEnclosingInstance
 */

public class InnerClassAccessesEnclosingInstance {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    int x = 10;
    class Inner {
        int read() { return x; }
        int readExplicit() { return InnerClassAccessesEnclosingInstance.this.x; }
        void bump() { x = 20; }
    }

    public static void main(String[] args) {
        InnerClassAccessesEnclosingInstance o = new InnerClassAccessesEnclosingInstance();
        Inner i = o.new Inner();
        check(i.read() == 10, "inner reads enclosing field by simple name");
        check(i.readExplicit() == 10, "Outer.this names enclosing instance");
        i.bump();
        check(o.x == 20, "inner mutates enclosing field");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
