/*
 * @test
 * @summary java.nio.file.WatchService conformance: create/poll/close.
 * @run main WatchServiceTest
 */

import java.nio.file.FileSystems;
import java.nio.file.WatchService;

public class WatchServiceTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWatchService() throws Exception {
        WatchService ws = FileSystems.getDefault().newWatchService();
        check(ws != null, "newWatchService");
        check(ws.poll() == null, "poll initially null");
        ws.close();
        check(true, "close");
    }

    public static void main(String[] args) throws Exception {
        testWatchService();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
