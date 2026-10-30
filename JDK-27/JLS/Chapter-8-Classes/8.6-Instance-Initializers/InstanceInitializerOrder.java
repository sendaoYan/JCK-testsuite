/*
 * @test
 * @summary JLS 8.6: an instance initializer runs after the superclass constructor
 *          returns and before the body of the class's own constructor executes.
 * @run main InstanceInitializerOrder
 */

class Super {
    int order;
    Super() { order = 1; }
}

public class InstanceInitializerOrder extends Super {
    { order = 2; }   // instance initializer: after super(), before ctor body

    InstanceInitializerOrder() {
        if (order != 2)
            throw new AssertionError("initializer did not run after super(): " + order);
        order = 3;
    }

    public static void main(String[] args) {
        InstanceInitializerOrder o = new InstanceInitializerOrder();
        if (o.order != 3)
            throw new AssertionError("constructor body did not run last: " + o.order);
        System.out.println("PASS");
    }
}
