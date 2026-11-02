/*
 * @test
 * @summary java.util.SequencedCollection conformance: interface and reversed/addFirst/addLast/removeFirst.
 * @run main SequencedCollectionTest
 */
import java.util.ArrayList;
import java.util.List;
import java.util.SequencedCollection;

public class SequencedCollectionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        check(SequencedCollection.class.isInterface(), "is interface");
        SequencedCollection<Integer> sc = new ArrayList<>(List.of(1, 2, 3));
        check(sc.getFirst() == 1 && sc.getLast() == 3, "getFirst/getLast");
        sc.addFirst(0);
        check(sc.getFirst() == 0, "addFirst");
        sc.addLast(4);
        check(sc.getLast() == 4, "addLast");
        check(sc.removeFirst() == 0 && sc.getFirst() == 1, "removeFirst");
        check(sc.removeLast() == 4 && sc.getLast() == 3, "removeLast");
        check(sc.reversed().getFirst() == 3 && sc.reversed().getLast() == 1, "reversed");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
