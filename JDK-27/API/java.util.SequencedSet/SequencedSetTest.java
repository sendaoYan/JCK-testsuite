/*
 * @test
 * @summary java.util.SequencedSet conformance: interface and addFirst/reversed.
 * @run main SequencedSetTest
 */
import java.util.LinkedHashSet;
import java.util.SequencedSet;

public class SequencedSetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        check(SequencedSet.class.isInterface(), "is interface");
        SequencedSet<Integer> ss = new LinkedHashSet<>();
        ss.add(1); ss.add(2); ss.add(3);
        check(ss.getFirst() == 1 && ss.getLast() == 3, "getFirst/getLast");
        ss.addFirst(0);
        check(ss.getFirst() == 0, "addFirst");
        check(ss.reversed().getFirst() == 3, "reversed");
        check(ss.removeLast() == 3, "removeLast");
        check(ss.removeFirst() == 0, "removeFirst");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
