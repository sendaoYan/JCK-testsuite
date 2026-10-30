/*
 * @test
 * @summary JLS 8.1.1.1: a subclass of an abstract class that is not itself
 *          abstract may be instantiated, running the abstract class's
 *          constructor and instance-variable field initializers.
 * @run main AbstractClassSubclassInstance
 */

abstract class Point {
    int x = 1, y = 2;
    abstract void alert();
}

class SimplePoint extends Point {
    void alert() { x = x + 10; }
}

public class AbstractClassSubclassInstance {
    public static void main(String[] args) {
        Point p = new SimplePoint();
        if (p.x != 1 || p.y != 2) throw new AssertionError("field initializers");
        p.alert();
        if (p.x != 11) throw new AssertionError("abstract method dispatch");

        System.out.println("PASS");
    }
}
