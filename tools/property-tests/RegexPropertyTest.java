/*
 * @test
 * @summary Property-based conformance supplement (jqwik 1.10.2, fixed seed):
 *          java.util.regex.Pattern/Matcher invariants.
 * @library /lib/jqwik-api-1.10.2.jar /lib/jqwik-engine-1.10.2.jar
 * @run main RegexPropertyTest
 */

import java.util.Arrays;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.RandomGenerator;

public class RegexPropertyTest {
    // Deterministic: fixed seed + fixed generator genSize, so the input
    // sequence (and therefore every counterexample) is identical across runs
    // and across JDKs (the jqwik jars are shared, and java.util.Random is
    // version-stable). Locale/TimeZone are pinned; no default charset is ever
    // consulted (all conversions are char-based).
    static final long SEED = 20261110L;
    static final int SAMPLES = 300;

    static int props;

    static void check(boolean cond, String msg) {
        if (!cond) {
            throw new AssertionError(msg);
        }
    }

    static final char[] DELIMS = { ',', '-', '_', '.', ':', ';', '/' };

    static String lit(Random r, RandomGenerator<Character> letterGen, int n) {
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            sb.append(letterGen.next(r).value());
        }
        return sb.toString();
    }

    // A pattern that is valid by construction: quoted literals, simple
    // alternation / quantifier / character class / non-capturing group.
    static String validPattern(Random r, RandomGenerator<Character> letterGen) {
        switch (r.nextInt(6)) {
            case 0: return Pattern.quote(lit(r, letterGen, 1 + r.nextInt(4)));
            case 1: return "(" + Pattern.quote(lit(r, letterGen, 1 + r.nextInt(4))) + ")";
            case 2: return Pattern.quote(lit(r, letterGen, 1 + r.nextInt(3)))
                    + "|" + Pattern.quote(lit(r, letterGen, 1 + r.nextInt(3)));
            case 3: return Pattern.quote(lit(r, letterGen, 1 + r.nextInt(3))) + "*";
            case 4: return "(?:" + Pattern.quote(lit(r, letterGen, 1 + r.nextInt(3))) + ")+";
            default: return "[" + lit(r, letterGen, 2 + r.nextInt(2)) + "]";
        }
    }

    static final String[] ILLEGAL = {
        "(", ")", "[", "[a", "a{2,1}", "*", "+", "?", "\\", "a{1,x}", "[z-a]", "(?"
    };

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        RandomGenerator<Character> letterGen =
                Arbitraries.integers().between(97, 122).map(i -> (char) (int) i).generator(1000);

        // 1. Compiling the same pattern twice yields equal Pattern objects with
        //    identical flags and identical match behaviour.
        {
            Random r = new Random(SEED);
            for (int i = 0; i < SAMPLES; i++) {
                String p = validPattern(r, letterGen);
                Pattern a = Pattern.compile(p);
                Pattern b = Pattern.compile(p);
                check(a.pattern().equals(p),
                      "Pattern.pattern() must return the compiled source at sample " + i + ": p=" + p);
                check(a.pattern().equals(b.pattern()) && a.flags() == b.flags(),
                      "repeated compile must agree at sample " + i + ": p=" + p);
                String in = lit(r, letterGen, 1 + r.nextInt(6));
                boolean ma = a.matcher(in).matches();
                boolean mb = b.matcher(in).matches();
                check(ma == mb,
                      "repeated compile match mismatch at sample " + i
                      + ": p=" + p + ", in=" + in + ", first=" + ma + ", second=" + mb);
            }
            props++;
        }

        // 2. split + String.join(delim) round-trips for inputs with no empty
        //    segments (non-empty segments joined by a single delimiter).
        {
            Random r = new Random(SEED);
            for (int i = 0; i < SAMPLES; i++) {
                String d = String.valueOf(DELIMS[r.nextInt(DELIMS.length)]);
                int m = 1 + r.nextInt(6);
                String[] segs = new String[m];
                for (int k = 0; k < m; k++) {
                    segs[k] = lit(r, letterGen, 1 + r.nextInt(3));
                }
                String s = String.join(d, segs);
                String[] parts = Pattern.compile(Pattern.quote(d)).split(s);
                check(parts.length == m && Arrays.equals(parts, segs),
                      "split must recover non-empty segments at sample " + i
                      + ": d=" + d + ", s=" + s + ", expected=" + Arrays.toString(segs)
                      + ", actual=" + Arrays.toString(parts));
                String joined = String.join(d, parts);
                check(joined.equals(s),
                      "split+join round-trip failed at sample " + i
                      + ": d=" + d + ", expected=" + s + ", actual=" + joined);
            }
            props++;
        }

        // 3. groupCount equals the number of capturing parentheses.
        {
            Random r = new Random(SEED);
            for (int i = 0; i < SAMPLES; i++) {
                int k = r.nextInt(6); // 0..5 capturing groups
                String p;
                if (k == 0) {
                    p = Pattern.quote(lit(r, letterGen, 1 + r.nextInt(4)));
                } else {
                    StringBuilder sb = new StringBuilder();
                    for (int g = 0; g < k; g++) {
                        sb.append('(')
                          .append(Pattern.quote(lit(r, letterGen, 1 + r.nextInt(3))))
                          .append(')');
                    }
                    p = sb.toString();
                }
                Pattern compiled = Pattern.compile(p);
                check(compiled.matcher("x").groupCount() == k,
                      "Matcher.groupCount mismatch at sample " + i + ": p=" + p
                      + ", expected=" + k + ", actual=" + compiled.matcher("x").groupCount());
            }
            props++;
        }

        // 4. matches/find/lookingAt respect the region: they behave on the
        //    region substring exactly as the corresponding String predicates.
        {
            Random r = new Random(SEED);
            for (int i = 0; i < SAMPLES; i++) {
                String L = lit(r, letterGen, 1 + r.nextInt(3));
                String region = lit(r, letterGen, 1 + r.nextInt(8));
                String prefix = lit(r, letterGen, r.nextInt(4));
                String suffix = lit(r, letterGen, r.nextInt(4));
                String hay = prefix + region + suffix;
                int start = prefix.length();
                int end = start + region.length();

                boolean expMatches = region.equals(L);
                boolean expFind = region.indexOf(L) >= 0;
                boolean expLookingAt = region.startsWith(L);

                boolean gotMatches = Pattern.compile(Pattern.quote(L)).matcher(hay).region(start, end).matches();
                boolean gotFind = Pattern.compile(Pattern.quote(L)).matcher(hay).region(start, end).find();
                boolean gotLookingAt = Pattern.compile(Pattern.quote(L)).matcher(hay).region(start, end).lookingAt();

                check(gotMatches == expMatches,
                      "matches/region mismatch at sample " + i + ": L=" + L + ", hay=" + hay
                      + ", region=[" + start + "," + end + "), expected=" + expMatches
                      + ", actual=" + gotMatches);
                check(gotFind == expFind,
                      "find/region mismatch at sample " + i + ": L=" + L + ", hay=" + hay
                      + ", region=[" + start + "," + end + "), expected=" + expFind
                      + ", actual=" + gotFind);
                check(gotLookingAt == expLookingAt,
                      "lookingAt/region mismatch at sample " + i + ": L=" + L + ", hay=" + hay
                      + ", region=[" + start + "," + end + "), expected=" + expLookingAt
                      + ", actual=" + gotLookingAt);
            }
            props++;
        }

        // 5. Malformed patterns reliably throw PatternSyntaxException.
        {
            for (int i = 0; i < ILLEGAL.length; i++) {
                String p = ILLEGAL[i];
                try {
                    Pattern.compile(p);
                    check(false, "expected PatternSyntaxException at sample " + i + ": p=" + p);
                } catch (PatternSyntaxException expected) {
                    // expected
                }
            }
            props++;
        }

        System.out.println("PASS (" + props + " properties)");
    }
}
