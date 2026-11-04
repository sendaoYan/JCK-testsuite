/*
 * @test
 * @summary java.util.jar.JarFile conformance: open/getJarEntry/getManifest/getInputStream.
 * @run main JarFileTest
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public class JarFileTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testJarFile() throws Exception {
        File tmp = File.createTempFile("jcapi", ".jar");
        tmp.deleteOnExit();
        Manifest mf = new Manifest();
        mf.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        JarOutputStream jout = new JarOutputStream(new FileOutputStream(tmp), mf);
        jout.putNextEntry(new JarEntry("hello.txt"));
        jout.write("world".getBytes("UTF-8"));
        jout.closeEntry();
        jout.close();

        JarFile jf = new JarFile(tmp);
        check(jf.size() >= 1, "size >= 1");
        JarEntry entry = jf.getJarEntry("hello.txt");
        check(entry != null && "hello.txt".equals(entry.getName()), "getJarEntry");
        check(jf.getEntry("hello.txt") != null, "getEntry");
        Manifest manifest = jf.getManifest();
        check(manifest != null, "getManifest present");
        check("1.0".equals(manifest.getMainAttributes().getValue(Attributes.Name.MANIFEST_VERSION)),
              "manifest version");
        InputStream in = jf.getInputStream(entry);
        byte[] buf = new byte[100];
        int n = in.read(buf);
        check(n == 5 && "world".equals(new String(buf, 0, n, "UTF-8")), "entry content");
        in.close();
        jf.close();
    }

    public static void main(String[] args) throws Exception {
        testJarFile();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
