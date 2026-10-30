/*
 * @test
 * @summary java.util.concurrent.TimeUnit API conformance: conversion and valueOf.
 * @run main TimeUnitTest
 */

import java.util.concurrent.TimeUnit;

public class TimeUnitTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testTimeUnit() {
        check(TimeUnit.SECONDS.toMillis(2) == 2000L, "seconds to millis");
        check(TimeUnit.MILLISECONDS.toSeconds(2000) == 2L, "millis to seconds");
        check(TimeUnit.HOURS.toMinutes(1) == 60L, "hours to minutes");
        check(TimeUnit.MICROSECONDS.convert(1, TimeUnit.MILLISECONDS) == 1000L, "convert");
        check(TimeUnit.valueOf("HOURS") == TimeUnit.HOURS, "valueOf");
    }

    public static void main(String[] args) {
        testTimeUnit();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
