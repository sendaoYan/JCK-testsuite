/*
 * @test
 * @summary java.time.InstantSource conformance: system/fixed/offset/tick/withZone.
 * @run main InstantSourceTest
 */
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.InstantSource;
import java.time.ZoneId;

public class InstantSourceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        check(InstantSource.class.isInterface(), "is interface");
        Instant fixed = Instant.ofEpochSecond(100);
        InstantSource src = InstantSource.fixed(fixed);
        check(src.instant().equals(fixed), "fixed instant");
        check(src.millis() == 100000L, "millis");
        check(InstantSource.system().instant() != null, "system instant");
        Clock clock = src.withZone(ZoneId.of("UTC"));
        check(clock != null && clock.instant().equals(fixed), "withZone");
        check(InstantSource.offset(src, Duration.ofSeconds(5)).instant().getEpochSecond() == 105, "offset");
        check(InstantSource.tick(src, Duration.ofSeconds(10)).instant().getEpochSecond() == 100, "tick");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
