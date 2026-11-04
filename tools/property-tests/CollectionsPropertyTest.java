/*
 * @test
 * @summary Property-based conformance supplement (jqwik 1.10.2, fixed seed):
 *          Collections.sort idempotence + ordering, List add -> size monotonicity.
 * @library /lib/jqwik-api-1.10.2.jar /lib/jqwik-engine-1.10.2.jar
 * @run main CollectionsPropertyTest
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.RandomGenerator;

public class CollectionsPropertyTest {
    static final long SEED = 20261104L;
    static final int SAMPLES = 300;
    static final int MAX_SIZE = 100;

    static int props;

    static void check(boolean cond, String msg) {
        if (!cond) {
            throw new AssertionError(msg);
        }
    }

    static List<Integer> randomList(Random r, RandomGenerator<Integer> sizeGen, RandomGenerator<Integer> elemGen) {
        int size = sizeGen.next(r).value();
        List<Integer> l = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            l.add(elemGen.next(r).value());
        }
        return l;
    }

    public static void main(String[] args) {
        RandomGenerator<Integer> sizeGen = Arbitraries.integers().between(0, MAX_SIZE).generator(1000);
        RandomGenerator<Integer> elemGen = Arbitraries.integers().between(-1000, 1000).generator(1000);

        // 1. Collections.sort is idempotent: sorting an already-sorted list is a no-op
        {
            Random r = new Random(SEED);
            for (int i = 0; i < SAMPLES; i++) {
                List<Integer> once = randomList(r, sizeGen, elemGen);
                Collections.sort(once);
                List<Integer> twice = new ArrayList<>(once);
                Collections.sort(twice);
                check(once.equals(twice), "Collections.sort idempotence failed at sample " + i);
            }
            props++;
        }

        // 2. Collections.sort produces non-decreasing order
        {
            Random r = new Random(SEED);
            for (int i = 0; i < SAMPLES; i++) {
                List<Integer> l = randomList(r, sizeGen, elemGen);
                Collections.sort(l);
                for (int k = 1; k < l.size(); k++) {
                    check(l.get(k - 1).compareTo(l.get(k)) <= 0,
                          "Collections.sort ordering failed at sample " + i + ", index " + k);
                }
            }
            props++;
        }

        // 3. List.add increments size by exactly one, and final size equals adds count
        {
            Random r = new Random(SEED);
            for (int i = 0; i < SAMPLES; i++) {
                List<Integer> l = new ArrayList<>();
                int adds = sizeGen.next(r).value();
                for (int k = 0; k < adds; k++) {
                    int before = l.size();
                    l.add(elemGen.next(r).value());
                    check(l.size() == before + 1, "List.add must increment size by exactly 1");
                }
                check(l.size() == adds, "List final size must equal number of adds");
            }
            props++;
        }

        System.out.println("PASS (" + props + " properties)");
    }
}
