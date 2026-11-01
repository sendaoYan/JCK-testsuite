/*
 * @test
 * @summary java.nio.file.FileStore conformance: name/type/space.
 * @run main FileStoreTest
 */

import java.nio.file.FileStore;
import java.nio.file.FileSystems;

public class FileStoreTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testFileStore() throws Exception {
        FileStore store = FileSystems.getDefault().getFileStores().iterator().next();
        check(store.name() != null, "name");
        check(store.type() != null, "type");
        check(store.getTotalSpace() >= 0, "getTotalSpace");
        check(store.getUsableSpace() >= 0, "getUsableSpace");
        check(store.getUnallocatedSpace() >= 0, "getUnallocatedSpace");
        check(store.toString() != null, "toString");
    }

    public static void main(String[] args) throws Exception {
        testFileStore();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
