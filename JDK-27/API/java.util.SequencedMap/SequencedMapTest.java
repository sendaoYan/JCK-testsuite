/*
 * @test
 * @summary java.util.SequencedMap conformance: interface and putFirst/firstEntry/pollFirstEntry.
 * @run main SequencedMapTest
 */
import java.util.LinkedHashMap;
import java.util.SequencedMap;

public class SequencedMapTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        check(SequencedMap.class.isInterface(), "is interface");
        SequencedMap<String, Integer> sm = new LinkedHashMap<>();
        sm.putFirst("a", 1);
        sm.putLast("b", 2);
        check("a".equals(sm.firstEntry().getKey()), "firstEntry");
        check("b".equals(sm.lastEntry().getKey()), "lastEntry");
        check("a".equals(sm.sequencedKeySet().getFirst()), "sequencedKeySet");
        check(sm.sequencedValues().getFirst() == 1, "sequencedValues");
        check(sm.sequencedEntrySet().getFirst().getValue() == 1, "sequencedEntrySet");
        check("a".equals(sm.pollFirstEntry().getKey()), "pollFirstEntry");
        check("b".equals(sm.pollLastEntry().getKey()), "pollLastEntry");
        check(sm.isEmpty(), "empty after polls");
        check(sm.reversed() != null, "reversed non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
