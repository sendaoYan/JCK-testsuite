/*
 * @test
 * @summary java.nio.file.StandardOpenOption conformance: enum constants.
 * @run main StandardOpenOptionTest
 */

import java.nio.file.StandardOpenOption;

public class StandardOpenOptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testStandardOpenOption() {
        check(StandardOpenOption.valueOf("READ") == StandardOpenOption.READ, "valueOf READ");
        check(StandardOpenOption.valueOf("WRITE") == StandardOpenOption.WRITE, "valueOf WRITE");
        check(StandardOpenOption.valueOf("APPEND") == StandardOpenOption.APPEND, "valueOf APPEND");
        check(StandardOpenOption.valueOf("TRUNCATE_EXISTING") == StandardOpenOption.TRUNCATE_EXISTING, "valueOf TRUNCATE_EXISTING");
        check(StandardOpenOption.valueOf("CREATE") == StandardOpenOption.CREATE, "valueOf CREATE");
        check(StandardOpenOption.valueOf("CREATE_NEW") == StandardOpenOption.CREATE_NEW, "valueOf CREATE_NEW");
        check(StandardOpenOption.valueOf("DELETE_ON_CLOSE") == StandardOpenOption.DELETE_ON_CLOSE, "valueOf DELETE_ON_CLOSE");
        check(StandardOpenOption.valueOf("SPARSE") == StandardOpenOption.SPARSE, "valueOf SPARSE");
        check(StandardOpenOption.valueOf("SYNC") == StandardOpenOption.SYNC, "valueOf SYNC");
        check(StandardOpenOption.valueOf("DSYNC") == StandardOpenOption.DSYNC, "valueOf DSYNC");
        check(StandardOpenOption.values().length >= 10, "at least 10 values");
        check(StandardOpenOption.class.isEnum(), "is enum");
    }

    public static void main(String[] args) {
        testStandardOpenOption();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
