/*
 * @test
 * @summary Property-based conformance supplement (jqwik 1.10.2, fixed seed):
 *          java.time.Duration/Period ofXxx -> toXxx round-trips.
 * @library /lib/jqwik-api-1.10.2.jar /lib/jqwik-engine-1.10.2.jar
 * @run main TimePropertyTest
 */

import java.time.Duration;
import java.time.Period;
import java.util.Random;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.RandomGenerator;

public class TimePropertyTest {
    static final long SEED = 20261104L;
    static final int SAMPLES = 300;
    // Keep values well within long range so no duration nanos overflow occurs.
    static final long L_RANGE = 1_000_000_000_000L; // 1e12
    static final int I_RANGE = 10_000;

    static int props;

    static void check(boolean cond, String msg) {
        if (!cond) {
            throw new AssertionError(msg);
        }
    }

    public static void main(String[] args) {
        // Duration round-trips
        RandomGenerator<Long> secGen = Arbitraries.longs().between(-L_RANGE, L_RANGE).generator(1000);
        RandomGenerator<Long> msGen = Arbitraries.longs().between(-L_RANGE, L_RANGE).generator(1000);
        RandomGenerator<Long> nsGen = Arbitraries.longs().between(-L_RANGE, L_RANGE).generator(1000);

        // 1. Duration.ofSeconds(s).toSeconds() == s
        {
            Random r = new Random(SEED);
            for (int i = 0; i < SAMPLES; i++) {
                long s = secGen.next(r).value();
                check(Duration.ofSeconds(s).toSeconds() == s,
                      "Duration.ofSeconds/toSeconds round-trip failed: " + s);
            }
            props++;
        }

        // 2. Duration.ofMillis(ms).toMillis() == ms
        {
            Random r = new Random(SEED);
            for (int i = 0; i < SAMPLES; i++) {
                long ms = msGen.next(r).value();
                check(Duration.ofMillis(ms).toMillis() == ms,
                      "Duration.ofMillis/toMillis round-trip failed: " + ms);
            }
            props++;
        }

        // 3. Duration.ofNanos(ns).toNanos() == ns
        {
            Random r = new Random(SEED);
            for (int i = 0; i < SAMPLES; i++) {
                long ns = nsGen.next(r).value();
                check(Duration.ofNanos(ns).toNanos() == ns,
                      "Duration.ofNanos/toNanos round-trip failed: " + ns);
            }
            props++;
        }

        // Period round-trips
        RandomGenerator<Integer> yGen = Arbitraries.integers().between(-I_RANGE, I_RANGE).generator(1000);
        RandomGenerator<Integer> mGen = Arbitraries.integers().between(-I_RANGE, I_RANGE).generator(1000);
        RandomGenerator<Integer> dGen = Arbitraries.integers().between(-I_RANGE, I_RANGE).generator(1000);

        // 4. Period.ofYears(y).getYears() == y
        {
            Random r = new Random(SEED);
            for (int i = 0; i < SAMPLES; i++) {
                int y = yGen.next(r).value();
                check(Period.ofYears(y).getYears() == y,
                      "Period.ofYears/getYears round-trip failed: " + y);
            }
            props++;
        }

        // 5. Period.ofMonths(m).getMonths() == m
        {
            Random r = new Random(SEED);
            for (int i = 0; i < SAMPLES; i++) {
                int m = mGen.next(r).value();
                check(Period.ofMonths(m).getMonths() == m,
                      "Period.ofMonths/getMonths round-trip failed: " + m);
            }
            props++;
        }

        // 6. Period.ofDays(d).getDays() == d
        {
            Random r = new Random(SEED);
            for (int i = 0; i < SAMPLES; i++) {
                int d = dGen.next(r).value();
                check(Period.ofDays(d).getDays() == d,
                      "Period.ofDays/getDays round-trip failed: " + d);
            }
            props++;
        }

        System.out.println("PASS (" + props + " properties)");
    }
}
