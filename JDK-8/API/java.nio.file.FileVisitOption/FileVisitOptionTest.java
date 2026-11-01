/*
 * @test
 * @summary java.nio.file.FileVisitOption conformance: enum constants.
 * @run main FileVisitOptionTest
 */

import java.nio.file.FileVisitOption;

public class FileVisitOptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testFileVisitOption() {
        check(FileVisitOption.valueOf("FOLLOW_LINKS") == FileVisitOption.FOLLOW_LINKS, "valueOf FOLLOW_LINKS");
        check(FileVisitOption.values().length >= 1, "at least 1 values");
        check(FileVisitOption.class.isEnum(), "is enum");
    }

    public static void main(String[] args) {
        testFileVisitOption();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
