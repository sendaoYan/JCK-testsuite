/*
 * @test
 * @summary java.time.ZoneId API conformance: full contract coverage.
 *          factories (of(String), of(String,Map), ofOffset, from, systemDefault,
 *          getAvailableZoneIds, SHORT_IDS) + exact exception matrix, null
 *          semantics, getId/getDisplayName/getRules/normalized, equality across
 *          offset and region subtypes, toString, and serialization round-trip +
 *          serialVersionUID.
 * @run main ZoneIdTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.time.zone.ZoneRulesException;
import java.util.Locale;
import java.util.Set;
import java.time.Instant;

public class ZoneIdTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    static ZoneId roundTrip(ZoneId v) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        new ObjectOutputStream(bos).writeObject(v);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        return (ZoneId) ois.readObject();
    }

    static void testFactories() {
        check(ZoneId.of("UTC").getId().equals("UTC"), "of UTC");
        check(ZoneId.of("Z").equals(ZoneOffset.UTC), "of Z == ZoneOffset.UTC");
        check(ZoneId.of("+05:00").equals(ZoneOffset.ofHours(5)), "of offset");
        check(ZoneId.of("-05:00").equals(ZoneOffset.ofHours(-5)), "of negative offset");
        check(ZoneId.of("America/New_York").getId().equals("America/New_York"), "of region");
        check(ZoneId.of("PST", ZoneId.SHORT_IDS).getId().equals("America/Los_Angeles"), "of short id map");
        check(ZoneId.ofOffset("UTC", ZoneOffset.ofHours(2)).getId().equals("UTC+02:00"), "ofOffset prefixed");
        check(ZoneId.ofOffset("", ZoneOffset.UTC).equals(ZoneOffset.UTC), "ofOffset empty prefix");
        check(ZoneId.ofOffset("", ZoneOffset.ofHours(3)).getId().equals("+03:00"), "ofOffset empty prefix offset");
        check(ZoneId.from(ZonedDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC"))).getId().equals("UTC"),
                "from ZonedDateTime");
        check(ZoneId.from(java.time.OffsetDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(5)))
                .equals(ZoneOffset.ofHours(5)), "from OffsetDateTime");
        Set<String> ids = ZoneId.getAvailableZoneIds();
        check(ids.contains("UTC") && ids.contains("America/New_York"), "getAvailableZoneIds");
        check(ZoneId.systemDefault() != null && ZoneId.systemDefault().getId() != null, "systemDefault");
        check(ZoneId.SHORT_IDS != null && !ZoneId.SHORT_IDS.isEmpty(), "SHORT_IDS non-empty");
    }

    static void testGetDisplayName() {
        check(ZoneOffset.ofHours(5).getDisplayName(TextStyle.FULL, Locale.US).equals("+05:00"),
                "displayName fixed offset");
        check(ZoneOffset.ofHours(-5).getDisplayName(TextStyle.SHORT, Locale.US).equals("-05:00"),
                "displayName negative offset");
        check(ZoneId.of("UTC").getRules() != null, "getRules non-null");
        check(ZoneId.of("UTC").getRules().isFixedOffset(), "UTC fixed offset");
        check(!ZoneId.of("America/New_York").getRules().isFixedOffset(), "region not fixed");
    }

    static void testNormalized() {
        check(ZoneId.of("UTC").normalized().equals(ZoneOffset.UTC), "UTC normalized to UTC offset");
        check(ZoneOffset.ofHours(5).normalized().equals(ZoneOffset.ofHours(5)), "offset normalized self");
        check(ZoneId.of("America/New_York").normalized().equals(ZoneId.of("America/New_York")),
                "region normalized self");
    }

    static void testEquality() {
        check(ZoneId.of("UTC").equals(ZoneId.of("UTC")), "equals same region");
        check(ZoneId.of("UTC").hashCode() == ZoneId.of("UTC").hashCode(), "hashCode");
        check(ZoneOffset.UTC.equals(ZoneId.of("Z")), "ZoneOffset.UTC equals of(Z)");
        check(!ZoneId.of("UTC").equals(ZoneId.of("America/New_York")), "equals different");
        check(ZoneId.of("UTC").equals(null) == false, "equals null");
        check(ZoneId.of("UTC").equals("UTC") == false, "equals other type");
        check(ZoneId.of("UTC").toString().equals("UTC"), "toString UTC");
        check(ZoneOffset.UTC.toString().equals("Z"), "toString Z");
    }

    static void testSerialization() throws Exception {
        check(ObjectStreamClass.lookup(ZoneId.class).getSerialVersionUID() == 8352817235686L,
                "serialVersionUID");
        ZoneId[] vals = { ZoneId.of("UTC"), ZoneOffset.UTC, ZoneId.of("America/New_York"),
                ZoneOffset.ofHours(5) };
        for (ZoneId v : vals) {
            check(roundTrip(v).equals(v), "serialization round-trip " + v);
        }
    }

    static void testErrors() {
        checkThrows(DateTimeException.class, () -> ZoneId.of("Invalid/Zone"), "of invalid");
        checkThrows(DateTimeException.class, () -> ZoneId.from(LocalDateTime.of(2024, 1, 1, 0, 0)),
                "from LocalDateTime no zone");
        checkThrows(NullPointerException.class, () -> ZoneId.of(null), "of null");
        checkThrows(NullPointerException.class, () -> ZoneId.of("UTC", null), "of null map");
        checkThrows(NullPointerException.class, () -> ZoneId.ofOffset(null, ZoneOffset.UTC), "ofOffset null prefix");
        checkThrows(NullPointerException.class, () -> ZoneId.ofOffset("UTC", null), "ofOffset null offset");
        checkThrows(NullPointerException.class, () -> ZoneId.from(null), "from null");
    }

    static void testNullSemantics() {
        checkThrows(NullPointerException.class, () -> ZoneId.of("UTC").getDisplayName(TextStyle.FULL, null),
                "getDisplayName null locale");
    }

    public static void main(String[] args) throws Exception {
        testFactories();
        testGetDisplayName();
        testNormalized();
        testEquality();
        testSerialization();
        testErrors();
        testNullSemantics();
        testDeepZoneId();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ==== contract deepening harness (appended) ====
    interface DeepRun { void run() throws Throwable; }

    static void xact(DeepRun r, Class<?> type, String exactMsg, String tag) {
        try {
            r.run();
            throw new AssertionError(tag + ": expected " + type.getSimpleName() + " but nothing was thrown");
        } catch (AssertionError e) {
            throw e;
        } catch (Throwable t) {
            if (!type.isInstance(t))
                throw new AssertionError(tag + ": expected " + type.getSimpleName() + " but got "
                        + t.getClass().getName() + ": " + t.getMessage(), t);
            if (exactMsg != null && !exactMsg.equals(t.getMessage()))
                throw new AssertionError(tag + ": " + type.getSimpleName() + " message <" + t.getMessage()
                        + "> want <" + exactMsg + ">");
            passed++;
        }
    }

    static void xeq(Object got, Object want, String tag) {
        if (got == null ? want != null : !got.equals(want))
            throw new AssertionError(tag + ": got <" + got + "> want <" + want + ">");
        passed++;
    }


    static void testDeepZoneId() {
        ZoneId ber = ZoneId.of("Europe/Berlin");
        check(ber.getId().equals("Europe/Berlin") && ber.toString().equals("Europe/Berlin"), "id/toString");
        check(!ber.getRules().isFixedOffset(), "Berlin not fixed");
        check(ZoneId.of("UTC").getId().equals("UTC"), "UTC id");
        xeq(ZoneId.of("UTC").normalized().toString(), "Z", "UTC normalized to Z");
        xeq(ZoneId.of("GMT").normalized().toString(), "Z", "GMT normalized to Z");
        check(ZoneId.of("GMT").getRules().isFixedOffset(), "GMT fixed");
        ZoneId g1 = ZoneId.of("GMT+01:00");
        check(g1.getId().equals("GMT+01:00") && g1.getRules().isFixedOffset()
              && g1.getRules().getOffset(Instant.EPOCH).equals(ZoneOffset.ofHours(1)), "GMT+01:00 prefix id");
        check(ZoneId.of("UTC+01:00").getId().equals("UTC+01:00"), "UTC+01:00 prefix id");
        xeq(ZoneId.ofOffset("GMT", ZoneOffset.ofHours(-5)).toString(), "GMT-05:00", "ofOffset");
        xeq(ZoneId.ofOffset("UTC", ZoneOffset.ofHours(3)).toString(), "UTC+03:00", "ofOffset UTC");
        xact(() -> ZoneId.ofOffset("Bogus", ZoneOffset.ofHours(1)), IllegalArgumentException.class,
            "prefix should be GMT, UTC or UT, is: Bogus", "ofOffset bad prefix");
        check(ZoneId.from(ZonedDateTime.ofStrict(LocalDateTime.of(2024, 2, 29, 10, 15, 30),
              ZoneOffset.ofHours(1), ber)) == ber, "from");
        xact(() -> ZoneId.of("Bogus/Zone"), ZoneRulesException.class, "Unknown time-zone ID: Bogus/Zone", "unknown region id");
        xact(() -> ZoneId.of("ABC"), ZoneRulesException.class, "Unknown time-zone ID: ABC", "3-letter id unmapped");
        xact(() -> ZoneId.of("EST"), ZoneRulesException.class, "Unknown time-zone ID: EST", "EST unmapped without map");
        xact(() -> ZoneId.of("+01:00:99"), DateTimeException.class,
            "Zone offset seconds not in valid range: value 99 is not in the range -59 to 59", "offset secs 99");
        xact(() -> ZoneId.of(null), NullPointerException.class, null, "of null");
        xact(() -> ZoneId.of(""), DateTimeException.class, "Invalid ID for ZoneOffset, invalid format: ", "empty id falls to offset parse");
         xeq(ZoneId.SHORT_IDS.get("EST"), "-05:00", "SHORT_IDS EST JDK8");
         xeq(ZoneId.SHORT_IDS.get("MST"), "-07:00", "SHORT_IDS MST JDK8");
         xeq(ZoneId.SHORT_IDS.get("HST"), "-10:00", "SHORT_IDS HST JDK8");
        xeq(ZoneId.SHORT_IDS.get("PST"), "America/Los_Angeles", "SHORT_IDS PST same");
        xeq(ZoneId.SHORT_IDS.get("JST"), "Asia/Tokyo", "SHORT_IDS JST same");
        xeq(ZoneId.SHORT_IDS.get("CNT"), "America/St_Johns", "SHORT_IDS CNT same");
        check(ZoneId.of("EST", ZoneId.SHORT_IDS).getId().equals((String) ZoneId.SHORT_IDS.get("EST")), "of EST with map");
        xeq(ber.getDisplayName(TextStyle.FULL, java.util.Locale.ENGLISH), "Central European Time", "display FULL EN");
        xeq(ber.getDisplayName(TextStyle.SHORT, java.util.Locale.ENGLISH), "CET", "display SHORT EN");
        xeq(ber.getDisplayName(TextStyle.NARROW, java.util.Locale.ENGLISH), "Europe/Berlin", "display NARROW falls back to id");
        check(ZoneId.getAvailableZoneIds().contains("UTC"), "available UTC");
        check(ZoneId.getAvailableZoneIds().contains("GMT"), "available GMT");
        check(ZoneId.getAvailableZoneIds().contains("Europe/Berlin"), "available Berlin");
        check(ZoneId.getAvailableZoneIds().contains("Asia/Shanghai"), "available Shanghai");
        check(ZoneId.getAvailableZoneIds().size() > 500, "available size");
        ZoneId utc = ZoneId.of("UTC");
        check(ZoneId.of("UTC").equals(ZoneId.of("UTC")), "equals");
        check(ZoneId.of("UTC").hashCode() == ZoneId.of("UTC").hashCode(), "hashCode");
        check(ZoneId.systemDefault() != null && ZoneId.systemDefault().getId().length() > 0, "systemDefault nonnull id");
        check(ZoneId.getAvailableZoneIds().contains(ZoneId.systemDefault().getId()), "systemDefault is available id");
        xeq(ZoneId.of("+01:00").getId(), "+01:00", "offset id accepted");
        check(ZoneId.of("+01:00").getRules().isFixedOffset(), "offset id fixed rules");
    }

}
