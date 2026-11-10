/*
 * @test
 * @summary Property-based conformance supplement (jqwik 1.10.2, fixed seed):
 *          java.time.Duration/Period ofXxx -> toXxx round-trips.
 * @library /lib/jqwik-api-1.10.2.jar /lib/jqwik-engine-1.10.2.jar
 * @run main TimePropertyTest
 */

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

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
        // Fixed locale/time-zone for determinism (no wall-clock reads; only
        // java.time APIs with an explicit fixed ZoneOffset.UTC are used).
        Locale.setDefault(Locale.US);
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        // Duration round-trips
        RandomGenerator<Long> secGen = Arbitraries.longs().between(-L_RANGE, L_RANGE).generator(1000);
        RandomGenerator<Long> msGen = Arbitraries.longs().between(-L_RANGE, L_RANGE).generator(1000);
        RandomGenerator<Long> nsGen = Arbitraries.longs().between(-L_RANGE, L_RANGE).generator(1000);

        // 1. Duration.ofSeconds(s).getSeconds() == s (getSeconds is JDK-8 safe;
        //    Duration.toSeconds() only became public in JDK 9)
        {
            Random r = new Random(SEED);
            for (int i = 0; i < SAMPLES; i++) {
                long s = secGen.next(r).value();
                check(Duration.ofSeconds(s).getSeconds() == s,
                      "Duration.ofSeconds/getSeconds round-trip failed: " + s);
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

        // 7. Duration.plus commutativity + addition law (range chosen to avoid
        //    any long overflow in the sum).
        {
            Random r = new Random(SEED);
            RandomGenerator<Long> plusGen =
                    Arbitraries.longs().between(-1_000_000_000L, 1_000_000_000L).generator(1000);
            for (int i = 0; i < SAMPLES; i++) {
                long s1 = plusGen.next(r).value();
                long s2 = plusGen.next(r).value();
                Duration d1 = Duration.ofSeconds(s1);
                Duration d2 = Duration.ofSeconds(s2);
                check(d1.plus(d2).equals(d2.plus(d1)),
                      "Duration.plus commutativity failed at sample " + i + ": s1=" + s1 + ", s2=" + s2);
                check(d1.plus(d2).getSeconds() == s1 + s2,
                      "Duration.plus addition law failed at sample " + i
                      + ": s1=" + s1 + ", s2=" + s2 + ", expected=" + (s1 + s2)
                      + ", actual=" + d1.plus(d2).getSeconds());
            }
            props++;
        }

        // 8. Duration.plus associativity
        {
            Random r = new Random(SEED);
            RandomGenerator<Long> plusGen =
                    Arbitraries.longs().between(-1_000_000_000L, 1_000_000_000L).generator(1000);
            for (int i = 0; i < SAMPLES; i++) {
                long s1 = plusGen.next(r).value();
                long s2 = plusGen.next(r).value();
                long s3 = plusGen.next(r).value();
                Duration d1 = Duration.ofSeconds(s1);
                Duration d2 = Duration.ofSeconds(s2);
                Duration d3 = Duration.ofSeconds(s3);
                check(d1.plus(d2).plus(d3).equals(d1.plus(d2.plus(d3))),
                      "Duration.plus associativity failed at sample " + i
                      + ": s1=" + s1 + ", s2=" + s2 + ", s3=" + s3);
            }
            props++;
        }

        // 9. Instant <-> LocalDateTime round-trip under a fixed UTC offset
        {
            Random r = new Random(SEED);
            RandomGenerator<Integer> nanoGen =
                    Arbitraries.integers().between(0, 999_999_999).generator(1000);
            for (int i = 0; i < SAMPLES; i++) {
                long sec = secGen.next(r).value();
                int nano = nanoGen.next(r).value();
                Instant inst = Instant.ofEpochSecond(sec, nano);
                LocalDateTime ldt = LocalDateTime.ofInstant(inst, ZoneOffset.UTC);
                Instant back = ldt.toInstant(ZoneOffset.UTC);
                check(back.equals(inst),
                      "Instant <-> LocalDateTime round-trip failed at sample " + i
                      + ": sec=" + sec + ", nano=" + nano + ", inst=" + inst + ", back=" + back);
            }
            props++;
        }

        // 10. Period.of does not normalize: getters return the exact inputs
        {
            Random r = new Random(SEED);
            for (int i = 0; i < SAMPLES; i++) {
                int y = yGen.next(r).value();
                int m = mGen.next(r).value();
                int d = dGen.next(r).value();
                Period p = Period.of(y, m, d);
                check(p.getYears() == y && p.getMonths() == m && p.getDays() == d,
                      "Period.of must not normalize at sample " + i
                      + ": y=" + y + ", m=" + m + ", d=" + d + ", actual=" + p);
            }
            props++;
        }

        // 11. Period.normalized preserves total months and days
        {
            Random r = new Random(SEED);
            for (int i = 0; i < SAMPLES; i++) {
                int y = yGen.next(r).value();
                int m = mGen.next(r).value();
                int d = dGen.next(r).value();
                Period p = Period.of(y, m, d);
                Period n = p.normalized();
                check(n.toTotalMonths() == (long) y * 12 + m,
                      "Period.normalized must preserve total months at sample " + i
                      + ": p=" + p + ", normalized=" + n);
                check(n.getDays() == d,
                      "Period.normalized must preserve days at sample " + i
                      + ": p=" + p + ", normalized=" + n);
            }
            props++;
        }

        // 12. Period.normalized is idempotent and keeps months in (-12, 12)
        {
            Random r = new Random(SEED);
            for (int i = 0; i < SAMPLES; i++) {
                int y = yGen.next(r).value();
                int m = mGen.next(r).value();
                int d = dGen.next(r).value();
                Period n = Period.of(y, m, d).normalized();
                check(n.normalized().equals(n),
                      "Period.normalized idempotence failed at sample " + i + ": n=" + n);
                check(Math.abs(n.getMonths()) <= 11,
                      "Period.normalized months out of range at sample " + i + ": n=" + n);
            }
            props++;
        }

        System.out.println("PASS (" + props + " properties)");
    }
}
