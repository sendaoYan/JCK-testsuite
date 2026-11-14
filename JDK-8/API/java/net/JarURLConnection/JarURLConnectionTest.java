/*
 * @test
 * @summary java.net.JarURLConnection conformance: white-box structure + black-box behavior.
 *          Deep contract coverage: real jar built on the fly (whole-jar and entry URLs,
 *          manifest main + per-entry attributes, content length, input stream contents,
 *          certificates, permissions, content type), entry-name decomposition incl.
 *          percent-decoding and malformed specs, and the abstract-subclass default
 *          delegation matrix (NPE paths when getJarFile() returns null).
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

    static final String A_TXT = "hello world"; // 11 bytes

    // concrete subclass exposing the protected constructor
    static class JC extends JarURLConnection {
        JC(URL u) throws MalformedURLException { super(u); }
        public void connect() { }
        public JarFile getJarFile() { return null; }
        static JC make(String spec) throws Exception {
            return new JC(new URL(spec));
        }
    }

    // abstract-style subclass overriding every accessor that can return null
    static class NullEntry extends JarURLConnection {
        NullEntry(URL u) throws MalformedURLException { super(u); }
        public void connect() { connected = true; }
        public JarFile getJarFile() { return null; }
        public JarEntry getJarEntry() { return null; }
    }

    /** Builds /tmp/net-c-JarURLConnection/t.jar with main + per-entry manifest attrs. */
    static File buildJar(File dir) throws Exception {
        Manifest mf = new Manifest();
        mf.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        mf.getMainAttributes().put(Attributes.Name.MAIN_CLASS, "com.example.Main");
        Attributes ea = new Attributes();
        ea.putValue("Custom-Attr", "yes");
        mf.getEntries().put("a.txt", ea);
        JarOutputStream jos = new JarOutputStream(new FileOutputStream(new File(dir, "t.jar")), mf);
        jos.putNextEntry(new JarEntry("a.txt"));
        jos.write(A_TXT.getBytes("UTF-8"));
        jos.closeEntry();
        jos.putNextEntry(new JarEntry("sub/b.txt"));
        jos.write("bye".getBytes("UTF-8"));
        jos.closeEntry();
        jos.close();
        return new File(dir, "t.jar");
    }

    // Windows 上 JarFileFactory 会缓存已打开的 jar（URLConnection 关闭后句柄仍持有），
    // 直接 delete 抛 ERROR_SHARING_VIOLATION。清理不是被测契约：多次 GC 重试后
    // 登记 deleteOnExit。
    static void deleteBestEffort(java.nio.file.Path p) {
        for (int i = 0; i < 10; i++) {
            try {
                java.nio.file.Files.deleteIfExists(p);
                return;
            } catch (IOException e) {
                System.gc();
                try { Thread.sleep(50); } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
        p.toFile().deleteOnExit();
    }

    static void deleteBestEffort(File f) {
        deleteBestEffort(f.toPath());
    }

    static String readAll(InputStream in) throws IOException {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        byte[] buf = new byte[64];
        int n;
        while ((n = in.read(buf)) > 0) bo.write(buf, 0, n);
        in.close();
        return new String(bo.toByteArray(), "UTF-8");
    }

    // ------------------------------------------------------------------
    // original structure checks (kept from previous version)
    // ------------------------------------------------------------------
    static void testOriginalStructure() throws Exception {
        Class<?> c = Class.forName("java.net.JarURLConnection");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(!c.isInterface(), "not interface");
        check("java.net.URLConnection".equals(c.getSuperclass().getName()), "superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("addRequestProperty"), "has method addRequestProperty");
        check(names.contains("connect"), "has method connect");
        check(names.contains("getAllowUserInteraction"), "has method getAllowUserInteraction");
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
        check(Modifier.isAbstract(c.getMethod("getJarFile").getModifiers()), "getJarFile abstract");
        check(c.getMethod("getManifest") != null, "getManifest");
        check(c.getMethod("getJarEntry") != null, "getJarEntry");
        check(c.getMethod("getAttributes") != null, "getAttributes");
        check(c.getMethod("getMainAttributes") != null, "getMainAttributes");
        check(c.getMethod("getCertificates") != null, "getCertificates");
        check(URLConnection.class.isAssignableFrom(c), "instanceof URLConnection");
    }

    // ------------------------------------------------------------------
    // entry-name / jar-file-URL decomposition via the protected constructor
    // ------------------------------------------------------------------
    static void testSpecParsing() throws Exception {
        JC whole = JC.make("jar:file:/tmp/x.jar!/");
        check(whole.getJarFileURL().toString().equals("file:/tmp/x.jar"), "jarFileURL whole");
        check(whole.getEntryName() == null, "entryName null for whole jar");

        JC entry = JC.make("jar:file:/tmp/x.jar!/com/foo/Q.class");
        check(entry.getJarFileURL().toString().equals("file:/tmp/x.jar"), "jarFileURL entry");
        check(entry.getEntryName().equals("com/foo/Q.class"), "entryName decoded");

        JC encoded = JC.make("jar:file:/tmp/x.jar!/com/foo/Quux%20Class.class");
        check(encoded.getEntryName().equals("com/foo/Quux Class.class"), "entryName percent-decoded");

        JC nestedDir = JC.make("jar:file:/tmp/x.jar!/dir/");
        check(nestedDir.getEntryName().equals("dir/"), "directory entry name kept");

        // URL without the !/ separator is malformed
        fails(MalformedURLException.class, "no separator", () -> JC.make("http://example.com/x"));
        fails(MalformedURLException.class, "no separator jar spec", () -> JC.make("jar:file:/tmp/x.jar"));
        // non-file inner URL is fine at construction time
        JC inner = JC.make("jar:http://example.com/x!/");
        check(inner.getJarFileURL().getProtocol().equals("http"), "inner http jarFileURL");
        check(inner.getEntryName() == null, "inner http entryName null");
    }

    // ------------------------------------------------------------------
    // real jar: whole-jar URL
    // ------------------------------------------------------------------
    static void testWholeJar(File jar) throws Exception {
        URL base = new URL("jar:" + jar.toURI().toURL() + "!/");
        URLConnection raw = base.openConnection();
        check(raw instanceof JarURLConnection, "openConnection yields JarURLConnection");
        JarURLConnection wj = (JarURLConnection) raw;
        wj.connect();
        wj.connect(); // idempotent
        check(wj.getEntryName() == null, "whole entryName null");
        check(wj.getJarFileURL().equals(jar.toURI().toURL()), "whole jarFileURL equals file URL");
        JarFile jf1 = wj.getJarFile();
        JarFile jf2 = wj.getJarFile();
        check(jf1 == jf2, "getJarFile cached: same instance on second call");
        check(wj.getManifest() != null, "whole manifest non-null");
        check("1.0".equals(wj.getMainAttributes().getValue(Attributes.Name.MANIFEST_VERSION)),
                "manifest version");
        check("com.example.Main".equals(wj.getMainAttributes().getValue(Attributes.Name.MAIN_CLASS)),
                "main class attr");
        check(wj.getJarEntry() == null, "whole jarEntry null");
        check(wj.getAttributes() == null, "whole attributes null");
        check(wj.getCertificates() == null, "whole certificates null");
        check(wj.getContentLengthLong() == jar.length(), "whole content length equals jar size");
        check(wj.getContentLength() == (int) jar.length(), "whole content length int");
        check("x-java/jar".equals(wj.getContentType()), "jar content type");
        check(wj.getLastModified() > 0, "jar lastModified positive");
        check(wj.getPermission() instanceof java.io.FilePermission, "jar permission FilePermission");
        check(wj.getPermission().getName() != null && !wj.getPermission().getName().isEmpty(),
                "jar permission name non-empty (path form platform-dependent)");
        check(wj.getPermission().getActions().equals("read"), "jar permission read");
        Object content = wj.getContent();
        check(content instanceof JarFile, "whole getContent is a JarFile");
        // whole-jar getInputStream is rejected (probed both JDKs: "no entry name specified")
        fails(IOException.class, "whole getInputStream requires entry", () -> wj.getInputStream());
        // manifest entry visible through the JarFile as well
        check(jf1.getEntry("a.txt") != null, "a.txt visible in JarFile");
        check(jf1.getManifest() != null, "JarFile manifest");
        jf1.close();
    }

    // ------------------------------------------------------------------
    // real jar: entry URLs
    // ------------------------------------------------------------------
    static void testEntry(File jar) throws Exception {
        URL u = new URL("jar:" + jar.toURI().toURL() + "!/a.txt");
        JarURLConnection je = (JarURLConnection) u.openConnection();
        je.connect();
        check("a.txt".equals(je.getEntryName()), "entry name");
        check(je.getJarFileURL().equals(jar.toURI().toURL()), "entry jarFileURL");
        JarEntry e = je.getJarEntry();
        check(e != null && "a.txt".equals(e.getName()), "jarEntry name");
        check(e.getSize() == A_TXT.length(), "jarEntry size");
        check(e.getTime() > 0, "jarEntry time positive");
        check(je.getContentLengthLong() == (long) A_TXT.length(), "entry content length long");
        check(je.getContentLength() == A_TXT.length(), "entry content length int");
        // per-entry attributes read from the manifest section
        Attributes at = je.getAttributes();
        check(at != null, "entry attributes from manifest");
        check("yes".equals(at.getValue("Custom-Attr")), "custom manifest attr value");
        // getMainAttributes delegates to the manifest main section even for entry URLs
        Attributes main = je.getMainAttributes();
        check(main != null && "com.example.Main".equals(main.getValue(Attributes.Name.MAIN_CLASS)),
                "entry getMainAttributes is manifest main section");
        check(je.getCertificates() == null, "unsigned entry certificates null");
        InputStream in = je.getInputStream();
        check(in != null, "entry input stream");
        check(A_TXT.equals(readAll(in)), "entry content round-trip");
        InputStream in2 = je.getInputStream();
        check(in2 != null, "second input stream new instance (not cached)");
        check(A_TXT.equals(readAll(in2)), "second read same content");
        check("text/plain".equals(je.getContentType()), "entry content type from entry name");
        JarFile jf = je.getJarFile();
        check(jf != null, "entry getJarFile non-null");
        jf.close();
        // subdirectory entry
        JarURLConnection sub = (JarURLConnection) new URL("jar:" + jar.toURI().toURL() + "!/sub/b.txt").openConnection();
        sub.connect();
        check("sub/b.txt".equals(sub.getEntryName()), "nested entry name");
        check(sub.getContentLength() == 3, "nested entry length");
        check("bye".equals(readAll(sub.getInputStream())), "nested entry content");
        sub.getJarFile().close();
    }

    // ------------------------------------------------------------------
    // missing entry in an existing jar
    // ------------------------------------------------------------------
    static void testMissingEntry(File jar) throws Exception {
        JarURLConnection mj = (JarURLConnection) new URL("jar:" + jar.toURI().toURL() + "!/nope.txt").openConnection();
        // message text differs between JDK-8 ("JAR entry nope.txt not found in <path>")
        // and JDK-27 ("JAR entry not found in jar file"); assert the type only
        fails(FileNotFoundException.class, "missing entry getInputStream", () -> mj.getInputStream());
        URLConnection mj2 = new URL("jar:" + jar.toURI().toURL() + "!/nope.txt").openConnection();
        check(mj2.getContentLengthLong() == -1L, "missing entry content length -1");
        URLConnection mj3 = new URL("jar:" + jar.toURI().toURL() + "!/nope.txt").openConnection();
        check(mj3.getContentLength() == -1, "missing entry content length int -1");
    }

    // ------------------------------------------------------------------
    // abstract-subclass default delegation when getJarFile() yields null
    // ------------------------------------------------------------------
    static void testNullDelegationDefaults() throws Exception {
        NullEntry aj = new NullEntry(new URL("jar:file:/tmp/nonexistent.jar!/a.txt"));
        check("a.txt".equals(aj.getEntryName()), "null-delegation entryName");
        check(aj.getJarFileURL().toString().equals("file:/tmp/nonexistent.jar"), "null-delegation jarFileURL");
        fails(NullPointerException.class, "getManifest null getJarFile", () -> aj.getManifest());
        fails(NullPointerException.class, "getMainAttributes null getJarFile", () -> aj.getMainAttributes());
        // with getJarEntry() overridden to null: getAttributes/getCertificates return null
        check(aj.getAttributes() == null, "getAttributes null when getJarEntry null");
        check(aj.getCertificates() == null, "getCertificates null when getJarEntry null");
        check(aj.getContentLength() == -1, "getContentLength -1 without connection");
        check(aj.getContentLengthLong() == -1L, "getContentLengthLong -1 without connection");
        check(aj.getContentType() == null, "getContentType null before connect");
        // getJarEntry overridden to null means getAttributes short-circuits without NPE
        check(aj.getJarEntry() == null, "overridden getJarEntry null");
    }

    static void testBlackBox(File dir) throws Exception {
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
        Path d = Files.createTempDirectory("juc");
        Path jar = d.resolve("t.jar");
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
            ByteArrayOutputStream gb = new ByteArrayOutputStream();
            byte[] gbuf = new byte[32];
            int gn;
            while ((gn = in.read(gbuf)) > 0) gb.write(gbuf, 0, gn);
            check(new String(gb.toByteArray(), "UTF-8").equals("hello"), "entry content");
        } finally {
            deleteBestEffort(jar);
            deleteBestEffort(d);
        }
    }

    public static void main(String[] args) throws Exception {
        testOriginalStructure();
        File dir = new File("/tmp/net-c-JarURLConnection");
        dir.mkdirs();
        File jar = buildJar(dir);
        try {
            testWhiteBox();
            testSpecParsing();
            testWholeJar(jar);
            testEntry(jar);
            testMissingEntry(jar);
            testNullDelegationDefaults();
            testBlackBox(dir);
        } finally {
            File[] kids = dir.listFiles();
            if (kids != null) for (File x : kids) deleteBestEffort(x);
            deleteBestEffort(dir);
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
