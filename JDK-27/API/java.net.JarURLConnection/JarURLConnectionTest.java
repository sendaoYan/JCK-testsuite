/*
 * @test
 * @summary java.net.JarURLConnection conformance: white-box structure + black-box behavior.
 * @run main JarURLConnectionTest
 */

import java.lang.reflect.*;
import java.net.*;
import java.io.*;
import java.util.jar.*;
import java.nio.file.*;

public class JarURLConnectionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface ThrowR { void run() throws Exception; }
    static void fails(Class<? extends Throwable> ex, String m, ThrowR r) {
        try { r.run(); } catch (Throwable t) {
            if (ex.isInstance(t)) { passed++; return; }
            throw new AssertionError(m + " wrong exception " + t);
        }
        throw new AssertionError(m + " no exception");
    }

    // concrete subclass exposing the protected constructor
    static class JC extends JarURLConnection {
        JC(URL u) throws MalformedURLException { super(u); }
        public void connect() { }
        public JarFile getJarFile() { return null; }
        static JC make(String spec) throws Exception {
            return new JC(new URL(spec));
        }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.JarURLConnection");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass().getName().equals("java.net.URLConnection"), "extends URLConnection");
        check(Modifier.isProtected(c.getDeclaredConstructor(URL.class).getModifiers()), "protected ctor(URL)");
        check(c.getDeclaredField("jarFileURLConnection") != null, "jarFileURLConnection field");
        check(c.getMethod("getJarFileURL") != null, "getJarFileURL");
        check(c.getMethod("getEntryName") != null, "getEntryName");
        check(c.getMethod("getJarFile") != null, "getJarFile");
        check(c.getMethod("getManifest") != null, "getManifest");
        check(c.getMethod("getJarEntry") != null, "getJarEntry");
        check(c.getMethod("getAttributes") != null, "getAttributes");
        check(c.getMethod("getMainAttributes") != null, "getMainAttributes");
        check(c.getMethod("getCertificates") != null, "getCertificates");
    }

    static void testBlackBox() throws Exception {
        // URL parsing via protected constructor
        JC whole = JC.make("jar:file:/tmp/x.jar!/");
        check(whole.getJarFileURL().toString().equals("file:/tmp/x.jar"), "jarFileURL whole");
        check(whole.getEntryName() == null, "entryName null for whole jar");

        JC entry = JC.make("jar:file:/tmp/x.jar!/com/foo/Q.class");
        check(entry.getJarFileURL().toString().equals("file:/tmp/x.jar"), "jarFileURL entry");
        check(entry.getEntryName().equals("com/foo/Q.class"), "entryName decoded");

        JC encoded = JC.make("jar:file:/tmp/x.jar!/com/foo/Quux%20Class.class");
        check(encoded.getEntryName().equals("com/foo/Quux Class.class"), "entryName percent-decoded");

        // URL without the !/ separator is malformed
        fails(MalformedURLException.class, "no separator", () -> JC.make("http://example.com/x"));

        // real jar on disk (created under a temp dir, removed in finally)
        Path dir = Files.createTempDirectory("juc");
        Path jar = dir.resolve("t.jar");
        try {
            Manifest mf = new Manifest();
            mf.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
            mf.getMainAttributes().put(Attributes.Name.MAIN_CLASS, "com.example.Main");
            try (JarOutputStream jos = new JarOutputStream(Files.newOutputStream(jar))) {
                jos.putNextEntry(new JarEntry("META-INF/MANIFEST.MF"));
                mf.write(jos);
                jos.closeEntry();
                jos.putNextEntry(new JarEntry("entry.txt"));
                jos.write("hello".getBytes("UTF-8"));
                jos.closeEntry();
            }

            String base = "jar:" + jar.toUri().toURL() + "!/";
            JarURLConnection root = (JarURLConnection) new URL(base).openConnection();
            root.connect();
            check(root.getJarFileURL().equals(jar.toUri().toURL()), "real jarFileURL");
            check(root.getEntryName() == null, "real entryName null");
            check(root.getJarFile() != null, "getJarFile non-null");
            check(root.getJarEntry() == null, "root jarEntry null");
            Manifest m = root.getManifest();
            check(m != null, "manifest non-null");
            check(m.getMainAttributes().getValue(Attributes.Name.MAIN_CLASS).equals("com.example.Main"), "main class attr");
            check(root.getMainAttributes() != null, "getMainAttributes non-null");
            check(root.getMainAttributes().getValue(Attributes.Name.MANIFEST_VERSION).equals("1.0"), "manifest version");
            check(root.getCertificates() == null, "root certificates null");
            check(root.getAttributes() == null, "root attributes null");

            JarURLConnection je = (JarURLConnection) new URL("jar:" + jar.toUri().toURL() + "!/entry.txt").openConnection();
            je.connect();
            check(je.getEntryName().equals("entry.txt"), "entry name");
            JarEntry e = je.getJarEntry();
            check(e != null && e.getName().equals("entry.txt"), "jarEntry");
            check(je.getAttributes() == null, "entry attributes null");
            check(je.getCertificates() == null, "entry certificates null");

            // getInputStream on the entry reads the bytes
            InputStream in = je.getInputStream();
            check(in != null, "entry input stream");
            byte[] got = in.readAllBytes();
            check(new String(got, "UTF-8").equals("hello"), "entry content");
        } finally {
            Files.deleteIfExists(jar);
            Files.deleteIfExists(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
