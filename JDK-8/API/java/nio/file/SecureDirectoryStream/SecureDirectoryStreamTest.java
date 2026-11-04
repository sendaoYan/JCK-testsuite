/*
 * @test
 * @summary java.nio.file.SecureDirectoryStream conformance: hierarchy and methods.
 * @run main SecureDirectoryStreamTest
 */

import java.nio.file.SecureDirectoryStream;
import java.nio.file.DirectoryStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class SecureDirectoryStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testSecureDirectoryStream() {
        check(SecureDirectoryStream.class.isInterface(), "is interface");
        check(DirectoryStream.class.isAssignableFrom(SecureDirectoryStream.class), "extends DirectoryStream");
        Set<String> names = new HashSet<String>();
        for (Method m : SecureDirectoryStream.class.getMethods()) names.add(m.getName());
        String[] required = { "newDirectoryStream", "newByteChannel", "deleteFile", "deleteDirectory", "move", "getFileAttributeView" };
        for (String n : required) check(names.contains(n), "has method " + n);
    }

    public static void main(String[] args) {
        testSecureDirectoryStream();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
