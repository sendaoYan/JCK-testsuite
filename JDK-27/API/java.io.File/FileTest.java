/*
 * @test
 * @summary java.io.File API conformance: createTempFile, exists/isFile/isDirectory/length,
 *          getName/getAbsolutePath/isAbsolute, and delete.
 * @run main FileTest
 */

import java.io.File;

public class FileTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testFile() throws Exception {
        File tmp = File.createTempFile("jck", ".tmp");
        tmp.deleteOnExit();
        check(tmp.exists(), "exists after create");
        check(tmp.isFile() && !tmp.isDirectory(), "isFile/isDirectory");
        check(tmp.length() == 0, "empty length");
        check(tmp.getName().startsWith("jck") && tmp.getName().endsWith(".tmp"), "getName");
        check(tmp.getAbsolutePath().length() > 0 && tmp.isAbsolute(), "absolute path");
        check("tmp".equals(new File("/tmp").getName()), "getName of directory");

        File del = File.createTempFile("jck-del", ".tmp");
        check(del.delete() && !del.exists(), "delete");
    }

    public static void main(String[] args) throws Exception {
        testFile();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
