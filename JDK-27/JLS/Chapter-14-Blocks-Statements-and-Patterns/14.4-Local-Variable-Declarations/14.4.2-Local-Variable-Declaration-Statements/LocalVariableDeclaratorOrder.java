/*
 * @test
 * @summary JLS 14.4.2: a local variable declaration statement processes its
 *          declarators left to right, and each initializer (except the first) is
 *          evaluated only if the preceding one completes normally.
 * @run main LocalVariableDeclaratorOrder
 */

public class LocalVariableDeclaratorOrder {
    static StringBuilder log = new StringBuilder();

    static int track(int v) { log.append(v); return v; }
    static int boom() { log.append('X'); throw new RuntimeException("boom"); }

    public static void main(String[] args) {
        int a = track(1), b = track(2), c = track(3);
        if (!"123".equals(log.toString()))
            throw new AssertionError("declarators not left-to-right: " + log);

        log.setLength(0);
        try {
            int x = track(4), y = boom(), z = track(5);
        } catch (RuntimeException e) {
            if (!"boom".equals(e.getMessage())) throw new AssertionError("reason");
        }
        // z's initializer was not evaluated because y's initializer did not complete
        // normally.
        if (!"4X".equals(log.toString()))
            throw new AssertionError("later initializer evaluated: " + log);

        System.out.println("PASS");
    }
}
