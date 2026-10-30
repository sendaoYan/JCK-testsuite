/*
 * @test
 * @summary JLS 14.3: local classes, interfaces, enums and records may be
 *          declared in a block. Local enums and records (and local interfaces)
 *          are implicitly static, so they can be declared and used in a static
 *          method without capturing an enclosing instance.
 * @run main LocalClassAndInterface
 */

public class LocalClassAndInterface {
    public static void main(String[] args) {
        class LC { int v() { return 1; } }
        interface LI { int v(); }
        enum LE { A, B }
        record LR(int x) {}

        LC lc = new LC();
        if (lc.v() != 1) throw new AssertionError("local class");

        LI li = new LI() { public int v() { return 2; } };
        if (li.v() != 2) throw new AssertionError("local interface");

        if (LE.values().length != 2 || LE.A == LE.B)
            throw new AssertionError("local enum");

        LR lr = new LR(7);
        if (lr.x() != 7) throw new AssertionError("local record");

        System.out.println("PASS");
    }
}
