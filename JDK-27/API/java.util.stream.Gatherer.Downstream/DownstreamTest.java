/*
 * @test
 * @summary java.util.stream.Gatherer.Downstream conformance: push and isRejecting.
 * @run main DownstreamTest
 */

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Gatherer;

public class DownstreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static final class DefaultDownstream implements Gatherer.Downstream<Integer> {
        final List<Integer> collected = new ArrayList<>();
        public boolean push(Integer e) { collected.add(e); return true; }
    }

    static final class RejectingDownstream implements Gatherer.Downstream<Integer> {
        final List<Integer> collected = new ArrayList<>();
        boolean rejecting;
        public boolean push(Integer e) { collected.add(e); return !rejecting; }
        public boolean isRejecting() { return rejecting; }
    }

    static void testPushAndDefaultIsRejecting() {
        DefaultDownstream d = new DefaultDownstream();
        check(!d.isRejecting(), "default isRejecting() is false");
        check(d.push(1), "push returns true while accepting");
        check(d.push(2), "push returns true again");
        check(d.collected.equals(List.of(1, 2)), "push delivered both elements");
    }

    static void testRejecting() {
        RejectingDownstream d = new RejectingDownstream();
        d.rejecting = true;
        check(d.isRejecting(), "isRejecting() reflects rejecting state");
        check(d.push(3) == false, "push returns false while rejecting");
        check(d.collected.equals(List.of(3)), "push still recorded the element");
    }

    static void testInterfaceShape() {
        check(Gatherer.Downstream.class.isInterface(), "Downstream is an interface");
        long abstractCount = java.util.Arrays.stream(Gatherer.Downstream.class.getMethods())
                .filter(m -> java.lang.reflect.Modifier.isAbstract(m.getModifiers()))
                .count();
        check(abstractCount == 1, "exactly one abstract method (push)");
    }

    public static void main(String[] args) {
        testPushAndDefaultIsRejecting();
        testRejecting();
        testInterfaceShape();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
