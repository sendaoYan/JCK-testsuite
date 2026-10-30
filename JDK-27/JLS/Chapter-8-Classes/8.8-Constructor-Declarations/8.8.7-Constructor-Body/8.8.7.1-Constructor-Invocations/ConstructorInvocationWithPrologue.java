/*
 * @test
 * @summary JLS 8.8.7.1: a constructor body may contain a prologue of statements
 *          preceding an explicit superclass constructor invocation, as long as
 *          those statements do not refer to the object under construction.
 * @run main ConstructorInvocationWithPrologue
 */

class Super {
    int s;
    Super(int s) { this.s = s; }
}

public class ConstructorInvocationWithPrologue {
    static class Sub extends Super {
        Sub() {
            int x = 41;
            int y = x + 1;   // prologue: local variables, no 'this' or instance fields
            super(y);
        }
    }

    public static void main(String[] args) {
        Sub s = new Sub();
        if (s.s != 42) throw new AssertionError("s = " + s.s);
        System.out.println("PASS");
    }
}
