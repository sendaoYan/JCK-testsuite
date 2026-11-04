/*
 * @test
 * @summary Property-based conformance supplement (jqwik 1.10.2, fixed seed):
 *          String charAt/substring relationships.
 * @library /lib/jqwik-api-1.10.2.jar /lib/jqwik-engine-1.10.2.jar
 * @run main StringPropertyTest
 */

import java.util.Random;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.RandomGenerator;

public class StringPropertyTest {
    static final long SEED = 20261104L;
    static final int SAMPLES = 300;
    static final int MAX_LEN = 64;

    static int props;

    static void check(boolean cond, String msg) {
        if (!cond) {
            throw new AssertionError(msg);
        }
    }

    // Build a string by drawing its length and then its characters from
    // independently seeded jqwik generators. We deliberately avoid the
    // composite Arbitraries.strings() (it needs a jqwik lifecycle thread) and
    // assemble from scalar generators, which works in a plain main.
    static String randomString(Random r, RandomGenerator<Integer> lenGen, RandomGenerator<Character> chGen) {
        int len = lenGen.next(r).value();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(chGen.next(r).value());
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        RandomGenerator<Integer> lenGen = Arbitraries.integers().between(0, MAX_LEN).generator(1000);
        RandomGenerator<Character> chGen = Arbitraries.integers().between(32, 126)
                .map(i -> (char) (int) i)
                .generator(1000);

        // 1. charAt(i) == substring(i, i+1).charAt(0) for every valid index
        {
            Random r = new Random(SEED);
            for (int i = 0; i < SAMPLES; i++) {
                String s = randomString(r, lenGen, chGen);
                for (int k = 0; k < s.length(); k++) {
                    check(s.charAt(k) == s.substring(k, k + 1).charAt(0),
                          "charAt/substring relation failed: s='" + s + "', k=" + k);
                }
            }
            props++;
        }

        // 2. s.equals(s.substring(0, i) + s.substring(i)) for every split point
        {
            Random r = new Random(SEED);
            for (int i = 0; i < SAMPLES; i++) {
                String s = randomString(r, lenGen, chGen);
                for (int k = 0; k <= s.length(); k++) {
                    check(s.equals(s.substring(0, k) + s.substring(k)),
                          "substring split/concat failed: s='" + s + "', k=" + k);
                }
            }
            props++;
        }

        // 3. substring(i, j).length() == j - i for 0 <= i <= j <= length()
        {
            Random r = new Random(SEED);
            for (int i = 0; i < SAMPLES; i++) {
                String s = randomString(r, lenGen, chGen);
                int n = s.length();
                int a = r.nextInt(n + 1);
                int b = r.nextInt(n + 1);
                int lo = Math.min(a, b);
                int hi = Math.max(a, b);
                check(s.substring(lo, hi).length() == hi - lo,
                      "substring length relation failed: s='" + s + "', lo=" + lo + ", hi=" + hi);
            }
            props++;
        }

        System.out.println("PASS (" + props + " properties)");
    }
}
