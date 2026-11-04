/*
 * @test
 * @summary JLS 8.4.1: an inner class constructor may declare a receiver
 *          parameter named Identifier.this for the immediately enclosing type.
 * @run main ReceiverParameterInnerConstructor
 */
public class ReceiverParameterInnerConstructor {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    class Inner {
        Inner(ReceiverParameterInnerConstructor ReceiverParameterInnerConstructor.this) { }
    }
    public static void main(String[] args) {
        ReceiverParameterInnerConstructor o = new ReceiverParameterInnerConstructor();
        check(o.new Inner() != null, "receiver parameter in inner class constructor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
