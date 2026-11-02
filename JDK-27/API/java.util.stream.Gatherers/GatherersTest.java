/*
 * @test
 * @summary java.util.stream.Gatherers conformance: windowFixed, windowSliding, fold, scan, mapConcurrent.
 * @run main GatherersTest
 */

import java.util.List;
import java.util.stream.Gatherers;
import java.util.stream.Stream;

public class GatherersTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWindowFixed() {
        List<List<Integer>> got = Stream.of(1, 2, 3, 4, 5).gather(Gatherers.windowFixed(2)).toList();
        check(got.equals(List.of(List.of(1, 2), List.of(3, 4), List.of(5))),
              "windowFixed(2) with partial last window");
    }

    static void testWindowSliding() {
        List<List<Integer>> got = Stream.of(1, 2, 3, 4, 5).gather(Gatherers.windowSliding(2)).toList();
        check(got.equals(List.of(List.of(1, 2), List.of(2, 3), List.of(3, 4), List.of(4, 5))),
              "windowSliding(2) overlapping windows");
    }

    static void testFold() {
        List<Integer> got = Stream.of(1, 2, 3, 4, 5).gather(Gatherers.fold(() -> 0, Integer::sum)).toList();
        check(got.equals(List.of(15)), "fold reduces to a single element");
    }

    static void testScan() {
        List<Integer> got = Stream.of(1, 2, 3, 4, 5).gather(Gatherers.scan(() -> 0, Integer::sum)).toList();
        check(got.equals(List.of(1, 3, 6, 10, 15)), "scan emits running totals");
    }

    static void testMapConcurrent() {
        List<Integer> got = Stream.of(1, 2, 3, 4, 5)
                .gather(Gatherers.mapConcurrent(4, x -> x * 2))
                .sorted()
                .toList();
        check(got.equals(List.of(2, 4, 6, 8, 10)), "mapConcurrent applies the mapping");
    }

    static void testInvalidSizes() {
        boolean threw;
        threw = false;
        try { Gatherers.windowFixed(0); } catch (IllegalArgumentException e) { threw = true; }
        check(threw, "windowFixed(0) throws IllegalArgumentException");
        threw = false;
        try { Gatherers.windowSliding(0); } catch (IllegalArgumentException e) { threw = true; }
        check(threw, "windowSliding(0) throws IllegalArgumentException");
        threw = false;
        try { Gatherers.windowSliding(-1); } catch (IllegalArgumentException e) { threw = true; }
        check(threw, "windowSliding(-1) throws IllegalArgumentException");
    }

    static void testClassShape() {
        check(!Gatherers.class.isInterface() && !Gatherers.class.isEnum(), "Gatherers is a class");
        check(java.lang.reflect.Modifier.isFinal(Gatherers.class.getModifiers()), "Gatherers is final");
    }

    public static void main(String[] args) {
        testWindowFixed();
        testWindowSliding();
        testFold();
        testScan();
        testMapConcurrent();
        testInvalidSizes();
        testClassShape();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
