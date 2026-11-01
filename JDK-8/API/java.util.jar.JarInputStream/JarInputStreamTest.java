/*
 * @test
 * @summary java.util.jar.JarInputStream conformance: read jar entries.
 * @run main JarInputStreamTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

public class JarInputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testJarInputStream() throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        JarOutputStream jout = new JarOutputStream(bout);
        jout.putNextEntry(new JarEntry("a.txt"));
        jout.write("aaa".getBytes("UTF-8"));
        jout.closeEntry();
        jout.close();

        JarInputStream jin = new JarInputStream(new ByteArrayInputStream(bout.toByteArray()));
        check(jin.getManifest() == null, "no manifest");
        JarEntry e1 = jin.getNextJarEntry();
        check(e1 != null && "a.txt".equals(e1.getName()), "first jar entry");
        byte[] buf = new byte[10];
        int n = jin.read(buf);
        check(n == 3, "read entry length");
        check(jin.getNextJarEntry() == null, "no more entries");
        jin.close();
    }

    public static void main(String[] args) throws Exception {
        testJarInputStream();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
