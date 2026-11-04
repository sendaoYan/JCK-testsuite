package client;

import p.Point;

public class Test {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        // p.Point is in module mC, read by mA only because mB requires
        // transitive mC (JLS 7.7.1).
        Point p = new Point();
        check(p.id == 42, "transitive readability of mC's exported package");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
