/*
 * @test
 * @summary JLS 6.6.2.2: a protected constructor is accessible via a superclass
 *          constructor invocation super(...) from a subclass in another package.
 * @compile Base.java Sub.java Main.java
 * @run main p2.Main
 */
package p2;

public class Main {
    public static void main(String[] args) {
        Sub s = new Sub();
        if (s == null) throw new AssertionError("Sub constructed via super()");

        System.out.println("PASS");
    }
}
