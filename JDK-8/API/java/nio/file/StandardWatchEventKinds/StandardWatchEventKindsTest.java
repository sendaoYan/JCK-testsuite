/*
 * @test
 * @summary java.nio.file.StandardWatchEventKinds conformance: event kinds.
 * @run main StandardWatchEventKindsTest
 */

import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;

public class StandardWatchEventKindsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testStandardWatchEventKinds() {
        check(StandardWatchEventKinds.ENTRY_CREATE != null, "ENTRY_CREATE");
        check(StandardWatchEventKinds.ENTRY_DELETE != null, "ENTRY_DELETE");
        check(StandardWatchEventKinds.ENTRY_MODIFY != null, "ENTRY_MODIFY");
        check(StandardWatchEventKinds.OVERFLOW != null, "OVERFLOW");
        check("ENTRY_CREATE".equals(StandardWatchEventKinds.ENTRY_CREATE.name()),
              "ENTRY_CREATE name");
        check(StandardWatchEventKinds.ENTRY_CREATE instanceof WatchEvent.Kind,
              "is WatchEvent.Kind");
    }

    public static void main(String[] args) {
        testStandardWatchEventKinds();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
