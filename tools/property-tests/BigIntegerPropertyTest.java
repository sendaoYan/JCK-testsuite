/*
 * @test
 * @summary Property-based conformance supplement (jqwik 1.10.2, fixed seed):
 *          BigInteger add/multiply commutativity and associativity.
 * @library /lib/jqwik-api-1.10.2.jar /lib/jqwik-engine-1.10.2.jar
 * @run main BigIntegerPropertyTest
 */

import java.math.BigInteger;
import java.util.Random;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.RandomGenerator;

public class BigIntegerPropertyTest {
    // Deterministic hard requirement: every run uses the same seed and same
    // generator genSize, so the exact input sequence (and therefore outcome)
    // is identical across runs.
    static final long SEED = 20261104L;
    static final int SAMPLES = 300;
    static final BigInteger RANGE = new BigInteger("1000000000000"); // +/- 1e12

    static int props;

    static void check(boolean cond, String msg) {
        if (!cond) {
            throw new AssertionError(msg);
        }
    }

    public static void main(String[] args) {
        Arbitrary<BigInteger> arb = Arbitraries.bigIntegers().between(RANGE.negate(), RANGE);
        RandomGenerator<BigInteger> gen = arb.generator(1000);

        // 1. add commutativity: a + b == b + a
        {
            Random r = new Random(SEED);
            for (int i = 0; i < SAMPLES; i++) {
                BigInteger a = gen.next(r).value();
                BigInteger b = gen.next(r).value();
                check(a.add(b).equals(b.add(a)),
                      "BigInteger.add commutativity failed at sample " + i + ": a=" + a + ", b=" + b);
            }
            props++;
        }

        // 2. add associativity: (a + b) + c == a + (b + c)
        {
            Random r = new Random(SEED);
            for (int i = 0; i < SAMPLES; i++) {
                BigInteger a = gen.next(r).value();
                BigInteger b = gen.next(r).value();
                BigInteger c = gen.next(r).value();
                check(a.add(b).add(c).equals(a.add(b.add(c))),
                      "BigInteger.add associativity failed at sample " + i);
            }
            props++;
        }

        // 3. multiply commutativity: a * b == b * a
        {
            Random r = new Random(SEED);
            for (int i = 0; i < SAMPLES; i++) {
                BigInteger a = gen.next(r).value();
                BigInteger b = gen.next(r).value();
                check(a.multiply(b).equals(b.multiply(a)),
                      "BigInteger.multiply commutativity failed at sample " + i);
            }
            props++;
        }

        // 4. multiply associativity: (a * b) * c == a * (b * c)
        {
            Random r = new Random(SEED);
            for (int i = 0; i < SAMPLES; i++) {
                BigInteger a = gen.next(r).value();
                BigInteger b = gen.next(r).value();
                BigInteger c = gen.next(r).value();
                check(a.multiply(b).multiply(c).equals(a.multiply(b.multiply(c))),
                      "BigInteger.multiply associativity failed at sample " + i);
            }
            props++;
        }

        // 5. compareTo antisymmetry: sign(a.compareTo(b)) == -sign(b.compareTo(a))
        {
            Random r = new Random(SEED);
            for (int i = 0; i < SAMPLES; i++) {
                BigInteger a = gen.next(r).value();
                BigInteger b = gen.next(r).value();
                check(Integer.signum(a.compareTo(b)) == -Integer.signum(b.compareTo(a)),
                      "BigInteger.compareTo antisymmetry failed at sample " + i + ": a=" + a + ", b=" + b);
            }
            props++;
        }

        // 6. compareTo transitivity: a <= b and b <= c imply a <= c
        {
            Random r = new Random(SEED);
            for (int i = 0; i < SAMPLES; i++) {
                BigInteger a = gen.next(r).value();
                BigInteger b = gen.next(r).value();
                BigInteger c = gen.next(r).value();
                if (a.compareTo(b) <= 0 && b.compareTo(c) <= 0) {
                    check(a.compareTo(c) <= 0,
                          "BigInteger.compareTo transitivity failed at sample " + i
                          + ": a=" + a + ", b=" + b + ", c=" + c);
                }
            }
            props++;
        }

        // 7. toString -> new BigInteger round-trips in base 10 and every radix 2..36
        {
            Random r = new Random(SEED);
            for (int i = 0; i < SAMPLES; i++) {
                BigInteger a = gen.next(r).value();
                check(new BigInteger(a.toString()).equals(a),
                      "BigInteger toString(10) round-trip failed at sample " + i + ": a=" + a);
                for (int radix = Character.MIN_RADIX; radix <= Character.MAX_RADIX; radix++) {
                    check(new BigInteger(a.toString(radix), radix).equals(a),
                          "BigInteger toString(radix) round-trip failed at sample " + i
                          + ": a=" + a + ", radix=" + radix);
                }
            }
            props++;
        }

        // 8. gcd commutativity and gcd(a,a) == abs(a)
        {
            Random r = new Random(SEED);
            for (int i = 0; i < SAMPLES; i++) {
                BigInteger a = gen.next(r).value();
                BigInteger b = gen.next(r).value();
                check(a.gcd(b).equals(b.gcd(a)),
                      "BigInteger.gcd commutativity failed at sample " + i + ": a=" + a + ", b=" + b);
                check(a.gcd(a).equals(a.abs()),
                      "BigInteger.gcd(a,a) == abs(a) failed at sample " + i + ": a=" + a);
            }
            props++;
        }

        // 9. gcd divides both operands (when non-zero)
        {
            Random r = new Random(SEED);
            for (int i = 0; i < SAMPLES; i++) {
                BigInteger a = gen.next(r).value();
                BigInteger b = gen.next(r).value();
                BigInteger g = a.gcd(b);
                if (g.signum() != 0) {
                    check(a.mod(g).signum() == 0 && b.mod(g).signum() == 0,
                          "BigInteger.gcd divisibility failed at sample " + i
                          + ": a=" + a + ", b=" + b + ", g=" + g);
                }
            }
            props++;
        }

        // 10. abs/signum invariants
        {
            Random r = new Random(SEED);
            for (int i = 0; i < SAMPLES; i++) {
                BigInteger a = gen.next(r).value();
                check(a.abs().signum() >= 0,
                      "abs must be non-negative at sample " + i + ": a=" + a);
                check(a.abs().abs().equals(a.abs()),
                      "abs idempotence failed at sample " + i + ": a=" + a);
                check(a.negate().abs().equals(a.abs()),
                      "negate().abs() == abs() failed at sample " + i + ": a=" + a);
                check(a.signum() >= -1 && a.signum() <= 1,
                      "signum out of range at sample " + i + ": a=" + a);
                check(a.signum() == -a.negate().signum(),
                      "signum(a) == -signum(-a) failed at sample " + i + ": a=" + a);
            }
            props++;
        }

        System.out.println("PASS (" + props + " properties)");
    }
}
