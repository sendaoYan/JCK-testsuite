/*
 * @test
 * @summary java.nio.file.FileSystemException conformance: file/other/reason accessors.
 * @run main FileSystemExceptionTest
 */

import java.io.IOException;
import java.nio.file.FileSystemException;

public class FileSystemExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testFileSystemException() {
        check(IOException.class.isAssignableFrom(FileSystemException.class),
              "extends IOException");
        FileSystemException e = new FileSystemException("file", "other", "reason");
        check("file".equals(e.getFile()), "getFile");
        check("other".equals(e.getOtherFile()), "getOtherFile");
        check("reason".equals(e.getReason()), "getReason");
        check(e.getMessage() != null, "getMessage");
        FileSystemException e2 = new FileSystemException("onlyfile");
        check("onlyfile".equals(e2.getFile()), "single-arg getFile");
        check(e2.getOtherFile() == null, "single-arg other null");
    }

    public static void main(String[] args) {
        testFileSystemException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
