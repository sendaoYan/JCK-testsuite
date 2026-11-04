/*
 * @test
 * @summary java.nio.file.spi.FileSystemProvider conformance: hierarchy and methods.
 * @run main FileSystemProviderTest
 */

import java.nio.file.spi.FileSystemProvider;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class FileSystemProviderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testFileSystemProvider() {
        check(Modifier.isAbstract(FileSystemProvider.class.getModifiers()), "is abstract");

        Set<String> names = new HashSet<String>();
        for (Method m : FileSystemProvider.class.getMethods()) names.add(m.getName());
        String[] required = { "installedProviders", "getScheme", "newFileSystem", "getFileSystem", "getPath", "newInputStream", "newOutputStream", "newFileChannel", "newByteChannel", "newDirectoryStream", "createDirectory", "delete", "copy", "move", "isSameFile", "isHidden", "getFileStore", "checkAccess", "getFileAttributeView", "readAttributes", "setAttribute" };
        for (String n : required) check(names.contains(n), "has method " + n);
    }

    public static void main(String[] args) {
        testFileSystemProvider();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
