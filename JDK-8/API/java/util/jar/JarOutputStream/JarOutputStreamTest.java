/*
 * @test
 * @summary java.util.jar.JarOutputStream conformance: write entry + manifest.
 * @run main JarOutputStreamTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public class JarOutputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testJarOutputStream() throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        Manifest mf = new Manifest();
        mf.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        JarOutputStream jout = new JarOutputStream(bout, mf);
        jout.putNextEntry(new JarEntry("e.txt"));
        jout.write("data".getBytes("UTF-8"));
        jout.closeEntry();
        jout.finish();
        jout.close();
        check(bout.size() > 0, "jar output produced");

        JarInputStream jin = new JarInputStream(new ByteArrayInputStream(bout.toByteArray()));
        check(jin.getManifest() != null, "manifest written");
        jin.close();
    }

    public static void main(String[] args) throws Exception {
        testJarOutputStream();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
