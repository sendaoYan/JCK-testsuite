/*
 * @test
 * @summary JLS 14.8: only statement expressions (assignment, increment,
 *          decrement, method invocation, class instance creation) may be used as
 *          expression statements; any resulting value is discarded.
 * @run main ExpressionStatement
 */

public class ExpressionStatement {
    public static void main(String[] args) {
        int x = 0;
        x = 1;              // assignment
        x++;                // postfix increment
        ++x;                // prefix increment
        x--;                // postfix decrement
        --x;                // prefix decrement
        new StringBuilder();// class instance creation, value discarded
        Integer.valueOf(5); // method invocation, value discarded
        if (x != 1)
            throw new AssertionError("statement expressions: x == " + x);
        System.out.println("PASS");
    }
}
