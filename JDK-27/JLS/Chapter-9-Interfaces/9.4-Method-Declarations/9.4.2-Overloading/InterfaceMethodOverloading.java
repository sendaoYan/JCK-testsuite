/*
 * @test
 * @summary JLS 9.4.2 (Example 9.4.2-1): methods of an interface may be overloaded
 *          with signatures that are not override-equivalent.
 * @run main InterfaceMethodOverloading
 */

interface PointInterface {
    void move(int dx, int dy);
}
interface RealPointInterface extends PointInterface {
    void move(float dx, float dy);
    void move(double dx, double dy);
}

public class InterfaceMethodOverloading {
    static class Impl implements RealPointInterface {
        public void move(int dx, int dy) {}
        public void move(float dx, float dy) {}
        public void move(double dx, double dy) {}
    }

    public static void main(String[] args) {
        RealPointInterface r = new Impl();
        r.move(1, 2);
        r.move(1.0f, 2.0f);
        r.move(1.0, 2.0);
        System.out.println("PASS");
    }
}
