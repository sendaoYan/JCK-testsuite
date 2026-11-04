/*
 * @test
 * @summary java.nio.file.FileVisitResult conformance: enum constants.
 * @run main FileVisitResultTest
 */

import java.nio.file.FileVisitResult;

public class FileVisitResultTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testFileVisitResult() {
        check(FileVisitResult.valueOf("CONTINUE") == FileVisitResult.CONTINUE, "valueOf CONTINUE");
        check(FileVisitResult.valueOf("TERMINATE") == FileVisitResult.TERMINATE, "valueOf TERMINATE");
        check(FileVisitResult.valueOf("SKIP_SIBLINGS") == FileVisitResult.SKIP_SIBLINGS, "valueOf SKIP_SIBLINGS");
        check(FileVisitResult.valueOf("SKIP_SUBTREE") == FileVisitResult.SKIP_SUBTREE, "valueOf SKIP_SUBTREE");
        check(FileVisitResult.values().length >= 4, "at least 4 values");
        check(FileVisitResult.class.isEnum(), "is enum");
    }

    public static void main(String[] args) {
        testFileVisitResult();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
