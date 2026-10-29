/*
 * @test
 * @summary JLS 6.6.2.2: a protected constructor is accessible via an anonymous
 *          class instance creation expression new C(...){...} from another
 *          package.
 * @compile Base.java Main.java
 * @run main p2.Main
 */
package p2;

import p1.Base;

public class Main {
    public static void main(String[] args) {
        Base b = new Base() { };   // anonymous class instance creation: permitted
        if (b == null) throw new AssertionError("anonymous subclass of Base");

        System.out.println("PASS");
    }
}
