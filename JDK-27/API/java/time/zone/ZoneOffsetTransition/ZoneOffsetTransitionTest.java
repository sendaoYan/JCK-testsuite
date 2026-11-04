/*
 * @test
 * @summary java.time.zone.ZoneOffsetTransition conformance: white-box structure + black-box behavior.
 * @run main ZoneOffsetTransitionTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.zone.ZoneOffsetTransition;

public class ZoneOffsetTransitionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Class<? extends Throwable> t, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable e) {
            if (t.isInstance(e)) { passed++; return; }
            throw new AssertionError(msg + ": expected " + t.getSimpleName()
                    + " got " + e.getClass().getSimpleName(), e);
        }
        throw new AssertionError(msg + ": expected " + t.getSimpleName() + " but nothing thrown");
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.time.zone.ZoneOffsetTransition");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(!c.isInterface(), "not interface");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(!c.isEnum(), "not enum");
        check(c.getSuperclass() == Object.class, "superclass Object");
        check(Comparable.class.isAssignableFrom(c), "implements Comparable");
        check(java.io.Serializable.class.isAssignableFrom(c), "implements Serializable");

        boolean hasOf = false;
        for (Method m : c.getDeclaredMethods()) {
            if (m.getName().equals("of") && m.getParameterCount() == 3) hasOf = true;
        }
        check(hasOf, "static of(LocalDateTime, ZoneOffset, ZoneOffset) present");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        ZoneOffsetTransition gap =
            ZoneOffsetTransition.of(LocalDateTime.of(2024, 3, 31, 2, 0),
                                    ZoneOffset.ofHours(1), ZoneOffset.ofHours(2));
        check(gap.getDateTimeBefore().equals(LocalDateTime.of(2024, 3, 31, 2, 0)),
              "gap datetime before");
        check(gap.getDateTimeAfter().equals(LocalDateTime.of(2024, 3, 31, 3, 0)),
              "gap datetime after");
        check(gap.getInstant().equals(Instant.parse("2024-03-31T01:00:00Z")), "gap instant");
        check(gap.toEpochSecond() == gap.getInstant().getEpochSecond(), "toEpochSecond");
        check(gap.getOffsetBefore().equals(ZoneOffset.ofHours(1)), "gap offset before");
        check(gap.getOffsetAfter().equals(ZoneOffset.ofHours(2)), "gap offset after");
        check(gap.getDuration().equals(Duration.ofHours(1)), "gap duration");
        check(gap.isGap(), "is gap");
        check(!gap.isOverlap(), "not overlap");
        // In a gap no offset is valid at the skipped local time.
        check(!gap.isValidOffset(ZoneOffset.ofHours(1)), "gap +1 not valid");
        check(!gap.isValidOffset(ZoneOffset.ofHours(2)), "gap +2 not valid");
        check(gap.toString().contains("Gap"), "gap toString");

        ZoneOffsetTransition overlap =
            ZoneOffsetTransition.of(LocalDateTime.of(2024, 10, 27, 3, 0),
                                    ZoneOffset.ofHours(2), ZoneOffset.ofHours(1));
        check(overlap.isOverlap(), "is overlap");
        check(!overlap.isGap(), "not gap");
        check(overlap.getDateTimeBefore().equals(LocalDateTime.of(2024, 10, 27, 3, 0)),
              "overlap datetime before");
        check(overlap.getDateTimeAfter().equals(LocalDateTime.of(2024, 10, 27, 2, 0)),
              "overlap datetime after");
        check(overlap.isValidOffset(ZoneOffset.ofHours(1)), "overlap +1 valid");
        check(overlap.isValidOffset(ZoneOffset.ofHours(2)), "overlap +2 valid");
        check(!overlap.isValidOffset(ZoneOffset.ofHours(3)), "overlap +3 invalid");
        check(overlap.toString().contains("Overlap"), "overlap toString");

        check(gap.compareTo(overlap) < 0, "gap before overlap by instant");

        ZoneOffsetTransition gap2 =
            ZoneOffsetTransition.of(LocalDateTime.of(2024, 3, 31, 2, 0),
                                    ZoneOffset.ofHours(1), ZoneOffset.ofHours(2));
        check(gap.equals(gap2), "equals equal transitions");
        check(gap.hashCode() == gap2.hashCode(), "hashCode equal transitions");
        check(!gap.equals(overlap), "not equal to overlap");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepZOT();
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


    static void testDeepZOT() {
        ZoneOffset before = ZoneOffset.ofHours(1);
        ZoneOffset after = ZoneOffset.ofHours(2);
        ZoneOffsetTransition gap = ZoneOffsetTransition.of(
            LocalDateTime.of(2024, 3, 31, 2, 0), before, after);
        ZoneOffsetTransition ovl = ZoneOffsetTransition.of(
            LocalDateTime.of(2024, 10, 27, 3, 0), after, before);
        xeq(gap.toString(), "Transition[Gap at 2024-03-31T02:00+01:00 to +02:00]", "gap toString");
        xeq(ovl.toString(), "Transition[Overlap at 2024-10-27T03:00+02:00 to +01:00]", "overlap toString");
        xeq(gap.getInstant().toString(), "2024-03-31T01:00:00Z", "gap instant UTC");
        xeq(gap.getDateTimeBefore().toString(), "2024-03-31T02:00", "gap dtBefore");
        xeq(gap.getDateTimeAfter().toString(), "2024-03-31T03:00", "gap dtAfter");
        check(gap.getOffsetBefore().equals(before) && gap.getOffsetAfter().equals(after), "gap offsets");
        xeq(gap.getDuration().toString(), "PT1H", "gap duration");
        check(gap.getDuration().equals(Duration.ofHours(1)), "gap duration eq 1h");
        check(gap.isGap() && !gap.isOverlap(), "gap flags");
        check(ovl.isOverlap() && !ovl.isGap(), "overlap flags");
        xeq(ovl.getDuration().toString(), "PT-1H", "overlap duration negative");
        check(!gap.isValidOffset(before), "gap rejects old offset");
        check(!gap.isValidOffset(after), "gap rejects new offset too");
        check(!gap.isValidOffset(ZoneOffset.UTC), "gap rejects UTC");
        check(ovl.isValidOffset(before), "overlap accepts offsetBefore");
        check(ovl.isValidOffset(after), "overlap accepts offsetAfter");
        check(!ovl.isValidOffset(ZoneOffset.UTC), "overlap rejects UTC");
        check(gap.compareTo(ovl) < 0 && ovl.compareTo(gap) > 0, "compareTo by instant");
        check(!gap.equals(ovl), "not equals");
        ZoneOffsetTransition gap2 = ZoneOffsetTransition.of(LocalDateTime.of(2024, 3, 31, 2, 0), before, after);
        check(gap.equals(gap2) && gap.hashCode() == gap2.hashCode(), "equals/hashCode");
        check(gap.toEpochSecond() == 1711846800L, "gap epochSecond");
        check(gap.toEpochSecond() == gap.getInstant().getEpochSecond(), "epochSecond equals instant");
        xact(() -> ZoneOffsetTransition.of(null, before, after), NullPointerException.class, "transition", "of null ldt");
        xact(() -> ZoneOffsetTransition.of(LocalDateTime.of(2024, 3, 31, 2, 0), before, before), IllegalArgumentException.class,
            "Offsets must not be equal", "of same offsets");
        check(ZoneOffsetTransition.of(LocalDateTime.of(2024, 3, 31, 2, 0), after, before).isOverlap(), "reverse direction overlap");
    }

}
