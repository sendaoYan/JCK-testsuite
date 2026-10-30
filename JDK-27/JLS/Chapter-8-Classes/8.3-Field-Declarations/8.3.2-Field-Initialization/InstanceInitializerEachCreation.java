/*
 * @test
 * @summary JLS 8.3.2: an instance variable initializer is evaluated and the
 *          assignment performed each time an instance of the class is created.
 * @run main InstanceInitializerEachCreation
 */

class Point {
    int x = next();
    static int count = 0;
    static int next() { return ++count; }
}

public class InstanceInitializerEachCreation {
    public static void main(String[] args) {
        Point a = new Point();
        Point b = new Point();
        if (a.x != 1 || b.x != 2)
            throw new AssertionError("instance initializer must run per creation");
        System.out.println("PASS");
    }
}
