/*
 * @test
 * @summary jdk.xml.internal.SecuritySupport conformance: internal package reflection.
 * @modules java.xml/jdk.xml.internal:+open
 * @run main SecuritySupportTest
 */
public class SecuritySupportTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.xml.internal.SecuritySupport", false, SecuritySupportTest.class.getClassLoader());
        check(c.getName().equals("jdk.xml.internal.SecuritySupport"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        java.util.Set<String> ifs = new java.util.HashSet<>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 0, "interface-count");
        java.util.Set<String> ec = new java.util.HashSet<>();
        java.util.Set<String> fd = new java.util.HashSet<>();
        java.util.Set<String> md = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) {
            if (f.isEnumConstant()) ec.add(f.getName());
            else if (java.lang.reflect.Modifier.isPublic(f.getModifiers()) || java.lang.reflect.Modifier.isProtected(f.getModifiers())) fd.add(f.getName());
        }
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (java.lang.reflect.Modifier.isPublic(m.getModifiers()) || java.lang.reflect.Modifier.isProtected(m.getModifiers())) md.add(m.getName());
        }
        check(ec.size() == 0, "enum-constant-count");
        check(fd.size() == 1, "field-count");
        check(fd.contains("NEWLINE"), "field-NEWLINE");
        check(md.size() == 19, "method-count");
        check(md.contains("checkAccess"), "method-checkAccess");
        check(md.contains("doesFileExist"), "method-doesFileExist");
        check(md.contains("getClassLoader"), "method-getClassLoader");
        check(md.contains("getClassSource"), "method-getClassSource");
        check(md.contains("getContextClassLoader"), "method-getContextClassLoader");
        check(md.contains("getErrorMessage"), "method-getErrorMessage");
        check(md.contains("getFileInputStream"), "method-getFileInputStream");
        check(md.contains("getInputStream"), "method-getInputStream");
        check(md.contains("getJAXPSystemProperty"), "method-getJAXPSystemProperty");
        check(md.contains("getParentClassLoader"), "method-getParentClassLoader");
        check(md.contains("getResource"), "method-getResource");
        check(md.contains("getResourceAsStream"), "method-getResourceAsStream");
        check(md.contains("getResourceBundle"), "method-getResourceBundle");
        check(md.contains("getSystemClassLoader"), "method-getSystemClassLoader");
        check(md.contains("isDirectory"), "method-isDirectory");
        check(md.contains("isFile"), "method-isFile");
        check(md.contains("isFileExists"), "method-isFileExists");
        check(md.contains("readConfig"), "method-readConfig");
        check(md.contains("sanitizePath"), "method-sanitizePath");
        deep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static Object inv(Object t, String n, Object... a) throws Exception {
        Class<?> start = t instanceof Class ? (Class<?>) t : t.getClass();
        for (Class<?> k = start; k != null; k = k.getSuperclass()) {
            for (java.lang.reflect.Method m : k.getDeclaredMethods()) {
                if (!m.getName().equals(n) || m.getParameterCount() != a.length) continue;
                boolean ok = true;
                for (int i = 0; i < a.length; i++) {
                    Class<?> p = m.getParameterTypes()[i];
                    if (a[i] == null) { if (p.isPrimitive()) { ok = false; break; } continue; }
                    Class<?> ac = a[i].getClass();
                    if (p.isPrimitive()) { if (!(ac == Integer.class || ac == Boolean.class)) { ok = false; break; } }
                    else if (!p.isAssignableFrom(ac)) { ok = false; break; }
                }
                if (ok) { m.setAccessible(true); return m.invoke(t instanceof Class ? null : t, a); }
            }
        }
        throw new NoSuchMethodException(n);
    }

    static void deep() throws Exception {
        Class<?> c = Class.forName("jdk.xml.internal.SecuritySupport", true, SecuritySupportTest.class.getClassLoader());
        // NEWLINE captured from the VM
        java.lang.reflect.Field nl = c.getDeclaredField("NEWLINE"); nl.setAccessible(true);
        check(nl.get(null).equals(System.lineSeparator()), "v-NEWLINE");
        // private ctor: utility class
        try { c.getDeclaredConstructor().newInstance(); check(false, "private-ctor"); }
        catch (IllegalAccessException | java.lang.reflect.InvocationTargetException e) { check(true, "private-ctor-blocked"); }
        // getJAXPSystemProperty(String)
        System.setProperty("dxml27.ss1", "v1");
        check(inv(c, "getJAXPSystemProperty", "dxml27.ss1").equals("v1"), "gsp-hit");
        check(inv(c, "getJAXPSystemProperty", "dxml27.nope") == null, "gsp-miss-null");
        System.clearProperty("dxml27.ss1");
        // typed getJAXPSystemProperty(Class, String, String)
        check(inv(c, "getJAXPSystemProperty", Integer.class, "dxml27.int", "5").equals(5), "typed-int-default");
        check(inv(c, "getJAXPSystemProperty", Boolean.class, "dxml27.bool", "false").equals(Boolean.FALSE), "typed-bool-default");
        check(inv(c, "getJAXPSystemProperty", String.class, "dxml27.str", "dflt").equals("dflt"), "typed-string-default");
        System.setProperty("dxml27.int", "18");
        System.setProperty("dxml27.bool", "true");
        check(inv(c, "getJAXPSystemProperty", Integer.class, "dxml27.int", "5").equals(18), "typed-int-sysprop");
        check(inv(c, "getJAXPSystemProperty", Boolean.class, "dxml27.bool", "false").equals(Boolean.TRUE), "typed-bool-sysprop");
        System.clearProperty("dxml27.int");
        System.clearProperty("dxml27.bool");
        // readConfig delegates to the JAXP configuration file (absent key -> null)
        check(inv(c, "readConfig", "dxml27.nope.cfg") == null, "readConfig-miss");
        // sanitizePath
        check(inv(c, "sanitizePath", (Object) null).equals(""), "sanitize-null");
        check(inv(c, "sanitizePath", "a/b/c.xml").equals("c.xml"), "sanitize-unix");
        check(inv(c, "sanitizePath", "c:\\x\\y.dtd").equals("y.dtd"), "sanitize-windows");
        check(inv(c, "sanitizePath", "y.dtd").equals("y.dtd"), "sanitize-plain");
        check(inv(c, "sanitizePath", "/foo").equals("/foo"), "sanitize-leading-slash-kept");
        check(inv(c, "sanitizePath", "a/b/").equals(""), "sanitize-trailing");
        // checkAccess protocol vetting: returns the rejected protocol or null when allowed
        check(inv(c, "checkAccess", (Object) null, "http", "all") == null, "access-null-id");
        check(inv(c, "checkAccess", "http://x", "all", "all") == null, "access-all-keyword");
        check(inv(c, "checkAccess", "http://x", "http", "all") == null, "access-allowed");
        check(inv(c, "checkAccess", "http://x", "file", "all").equals("http"), "access-rejected");
        check(inv(c, "checkAccess", "relpath", "file", "all") == null, "access-nocolon-file");
        check(inv(c, "checkAccess", "relpath", "http", "all").equals("file"), "access-nocolon-rejected");
        check(inv(c, "checkAccess", "jrt:java.xml/x", "file", "all") == null, "access-jrt-maps-file");
        check(inv(c, "checkAccess", "jrt:java.xml/x", "http", "all").equals("file"), "access-jrt-rejected");
        check(inv(c, "checkAccess", "jar:http://h/p!/y", "http", "all") == null, "access-jar-allowed");
        check(inv(c, "checkAccess", "jar:http://h/p!/y", "file", "all").equals("http"), "access-jar-rejected");
        check(inv(c, "checkAccess", "http://x", " FILE , http ", "all") == null, "access-case-ws-list");
        check(inv(c, "checkAccess", "https://x", " FILE , http ", "all").equals("https"), "access-case-ws-list-miss");
        check(inv(c, "checkAccess", "https://x", "ALL", "all") == null, "access-any-keyword-case");
        check(inv(c, "checkAccess", "https://x", "file", "ALL").equals("https"), "access-keyword-case-strict");
        try { inv(c, "checkAccess", "::::", "file", "all"); check(false, "access-malformed"); }
        catch (java.lang.reflect.InvocationTargetException ite) { check(ite.getCause() instanceof java.net.MalformedURLException, "access-malformed-MUE"); }
        // file predicates and stream round trip
        java.nio.file.Path tmp = java.nio.file.Files.createTempFile("dxml27-ss", ".txt");
        java.nio.file.Files.write(tmp, "hello".getBytes("UTF-8"));
        java.io.File f = tmp.toFile();
        try {
            check(!(Boolean) inv(c, "isDirectory", f), "isDirectory-false");
            check((Boolean) inv(c, "isFileExists", f), "isFileExists-true");
            check((Boolean) inv(c, "isFile", f), "isFile-true");
            check((Boolean) inv(c, "doesFileExist", f), "doesFileExist-true");
            Object fis = inv(c, "getFileInputStream", f);
            check(fis instanceof java.io.FileInputStream, "fis-type");
            check(((java.io.FileInputStream) fis).read() == 'h', "fis-first-byte");
            ((java.io.FileInputStream) fis).close();
        } finally {
            java.nio.file.Files.deleteIfExists(tmp);
        }
        check(!(Boolean) inv(c, "doesFileExist", f), "deleted-not-exists");
        // resource lookup: leading-slash vs class-relative semantics
        check(inv(c, "getResource", "SecuritySupport.class").toString().startsWith("jrt:/java.xml/"), "getResource-jrt");
        java.io.InputStream rs = (java.io.InputStream) inv(c, "getResourceAsStream", "jdk/xml/internal/SecuritySupport.class");
        check(rs != null, "getResourceAsStream-abs-nonnull");
        rs.close();
        check(inv(c, "getResourceAsStream", "SecuritySupport.class") == null, "getResourceAsStream-rel-null");
        // resource bundle lookup from inside the java.xml module + fallback error
        Object rb = inv(c, "getResourceBundle", "com.sun.org.apache.xerces.internal.impl.msg.SAXMessages");
        check(rb != null, "bundle-nonnull");
        Object rb2 = inv(c, "getResourceBundle", "com.sun.org.apache.xerces.internal.impl.msg.SAXMessages", java.util.Locale.US);
        check(rb2 != null, "bundle-locale-nonnull");
        try { inv(c, "getResourceBundle", "no.such.BUNDLE"); check(false, "bundle-bogus"); }
        catch (java.lang.reflect.InvocationTargetException ite) {
            check(ite.getCause() instanceof java.util.MissingResourceException, "bundle-bogus-MRE");
            check(ite.getCause().getMessage().contains("Could not load any resource bundle by no.such.BUNDLE"), "bundle-bogus-message");
        }
        // getErrorMessage: MessageFormat with the real parser bundle (same text the parser emits)
        Object msg = inv(c, "getErrorMessage", null, "com.sun.org.apache.xerces.internal.impl.msg.SAXMessages",
                "feature-not-recognized", new Object[]{"http://x/y"});
        check(msg.equals("Feature 'http://x/y' is not recognized."), "getErrorMessage-format");
        Object msg2 = inv(c, "getErrorMessage", java.util.Locale.US,
                "com.sun.org.apache.xerces.internal.impl.msg.XMLMessages", "AccessExternalDTD",
                new Object[]{"nonexistent-dxml27.dtd", "file"});
        check(msg2.equals("External DTD: Failed to read external DTD 'nonexistent-dxml27.dtd', because 'file' access is not allowed due to restriction set by the accessExternalDTD property."), "getErrorMessage-AccessExternalDTD");
        // classloaders
        check(inv(c, "getContextClassLoader") == Thread.currentThread().getContextClassLoader(), "ctx-cl");
        check(inv(c, "getClassLoader") == Thread.currentThread().getContextClassLoader(), "getClassLoader-tccl");
        check(inv(c, "getSystemClassLoader") == ClassLoader.getSystemClassLoader(), "sys-cl");
        check(inv(c, "getParentClassLoader", ClassLoader.getSystemClassLoader())
                == ClassLoader.getSystemClassLoader().getParent(), "parent-cl");
        check(inv(c, "getParentClassLoader", ClassLoader.getSystemClassLoader().getParent()) == null, "parent-cl-null");
        check(inv(c, "getResourceAsStream", SecuritySupportTest.class.getClassLoader(), "SecuritySupportTest.class") != null, "cl-resource-hit");
        check(inv(c, "getResourceAsStream", null, "SecuritySupport.class") != null, "cl-null-classloader-fallback");
        // code source: named-module classes report no code source
        check(inv(c, "getClassSource", c).equals("(no code source)"), "classsource-module");
        check(inv(c, "getClassSource", SecuritySupportTest.class).toString().startsWith("file:"), "classsource-classpath");
        // real-XML linkage: the AccessExternalDTD message text equals what a restricted parse throws
        javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        dbf.setAttribute(javax.xml.XMLConstants.ACCESS_EXTERNAL_DTD, "");
        try {
            dbf.newDocumentBuilder().parse(new java.io.ByteArrayInputStream(
                    "<?xml version='1.0'?><!DOCTYPE r SYSTEM 'nonexistent-dxml27.dtd'><r/>".getBytes("UTF-8")));
            check(false, "restricted-parse-throws");
        } catch (org.xml.sax.SAXParseException spe) {
            check(spe.getMessage().equals(msg2), "parser-message-matches-bundle");
        }
    }
}
