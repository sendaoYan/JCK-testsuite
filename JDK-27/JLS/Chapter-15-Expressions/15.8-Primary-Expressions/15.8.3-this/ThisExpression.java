/*
 * @test
 * @summary JLS 15.8.3: the keyword this denotes the receiver object; inside a
 *          lambda body its value is the same as in the surrounding context.
 * @run main ThisExpression
 */

public class ThisExpression {
    int value = 42;

    void check() {
        if (this.value != 42) throw new AssertionError("this.value");

        Runnable r = () -> {
            if (this.value != 42)
                throw new AssertionError("this in lambda differs from enclosing this");
        };
        r.run();
    }

    public static void main(String[] args) {
        new ThisExpression().check();
        System.out.println("PASS");
    }
}
