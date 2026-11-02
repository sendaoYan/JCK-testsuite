/*
 * @test
 * @summary java.awt.RenderingHints conformance: Map-backed key/value store.
 * @run main RenderingHintsTest
 */


import java.awt.RenderingHints;
public class RenderingHintsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) {
        RenderingHints h = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        check(RenderingHints.VALUE_ANTIALIAS_ON.equals(h.get(RenderingHints.KEY_ANTIALIASING)), "get put value");
        check(h.containsKey(RenderingHints.KEY_ANTIALIASING), "containsKey");
        check(h.size() == 1, "size");
        h.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        check(h.size() == 2, "size after second put");
        check(RenderingHints.VALUE_RENDER_QUALITY.equals(h.get(RenderingHints.KEY_RENDERING)), "second value");
        h.clear();
        check(h.isEmpty(), "clear");
        RenderingHints copy = (RenderingHints) h.clone();
        check(copy != h && copy.isEmpty(), "clone independent");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
