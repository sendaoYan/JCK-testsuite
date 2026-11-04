/*
 * @test
 * @summary java.net.URI conformance: white-box structure + full contract coverage.
 *          component decomposition (raw vs decoded), all six constructors +
 *          create(), hierarchical vs opaque URIs, resolve/relativize/normalize,
 *          parseServerAuthority, toURL/toASCIIString, equals/hashCode/compareTo
 *          case rules, null semantics, exact URISyntaxException/IAE/NPE paths,
 *          and serialization round-trip.
 * @run main URITest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class URITest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    @SuppressWarnings("unchecked")
    static <T> T roundTrip(T obj) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(obj);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        return (T) ois.readObject();
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.URI");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(Comparable.class.isAssignableFrom(c), "implements Comparable");
        check(java.io.Serializable.class.isAssignableFrom(c), "implements Serializable");
        check(c.getConstructor(String.class) != null, "ctor(String)");
        check(c.getConstructor(String.class, String.class, String.class, int.class, String.class, String.class, String.class) != null, "ctor(7 args)");
        check(c.getMethod("create", String.class) != null, "create");
        check(c.getMethod("resolve", String.class) != null, "resolve(String)");
        check(c.getMethod("normalize") != null, "normalize");
        check(c.getMethod("toASCIIString") != null, "toASCIIString");
        check(c.getMethod("compareTo", URI.class) != null, "compareTo");
    }

    static void testSingleStringComponents() throws Exception {
        URI u = new URI("http://user@example.com:8080/a/b?q=1#frag");
        check("http".equals(u.getScheme()), "getScheme");
        check("user".equals(u.getUserInfo()), "getUserInfo");
        check("example.com".equals(u.getHost()), "getHost");
        check(u.getPort() == 8080, "getPort");
        check("/a/b".equals(u.getPath()), "getPath");
        check("q=1".equals(u.getQuery()), "getQuery");
        check("frag".equals(u.getFragment()), "getFragment");
        check("user@example.com:8080".equals(u.getAuthority()), "getAuthority");
        check(u.isAbsolute(), "isAbsolute true");
        check(!u.isOpaque(), "not opaque");
        check("http://user@example.com:8080/a/b?q=1#frag".equals(u.toString()), "toString");
    }

    static void testRawVsDecoded() throws Exception {
        URI u = new URI("http://user%40x@example.com:80/a%2Fb?q%3D1#f%20g");
        check("user%40x".equals(u.getRawUserInfo()), "rawUserInfo");
        check("user@x".equals(u.getUserInfo()), "decoded userInfo");
        check("/a%2Fb".equals(u.getRawPath()), "rawPath");
        check("/a/b".equals(u.getPath()), "decoded path");
        check("q%3D1".equals(u.getRawQuery()), "rawQuery");
        check("q=1".equals(u.getQuery()), "decoded query");
        check("f%20g".equals(u.getRawFragment()), "rawFragment");
        check("f g".equals(u.getFragment()), "decoded fragment");
        check("user%40x@example.com:80".equals(u.getRawAuthority()), "rawAuthority");
        check("user@x@example.com:80".equals(u.getAuthority()), "decoded authority");
        check("//user%40x@example.com:80/a%2Fb?q%3D1".equals(u.getRawSchemeSpecificPart()), "rawSsp");
        check("//user@x@example.com:80/a/b?q=1".equals(u.getSchemeSpecificPart()), "decoded ssp");
    }

    static void testNonAsciiAndAscii() throws Exception {
        URI u = new URI("http://example.com/ä");
        check("/ä".equals(u.getPath()), "non-ascii decoded path");
        check("/ä".equals(u.getRawPath()), "non-ascii raw path preserves unicode");
        check("http://example.com/ä".equals(u.toString()), "toString preserves unicode");
        check("http://example.com/%C3%A4".equals(u.toASCIIString()), "toASCIIString percent-encodes");
    }

    static void testConstructorValidation() {
        checkThrows(URISyntaxException.class, () -> new URI("1http://x/"), "scheme starts with digit");
        checkThrows(URISyntaxException.class, () -> new URI("http://exa mple.com/x"), "space in authority");
        checkThrows(URISyntaxException.class, () -> new URI("http://example.com/bad space"), "space in path");
        checkThrows(NullPointerException.class, () -> new URI((String) null), "single-string null");
        checkThrows(IllegalArgumentException.class, () -> URI.create("http://exa mple.com/x"), "create wraps URISyntaxException");
        checkThrows(NullPointerException.class, () -> URI.create(null), "create null");
    }

    static void testMultiArgConstructors() throws Exception {
        URI opaque = new URI("mailto", "java-net@example.com", null);
        check(opaque.isOpaque(), "3-arg opaque");
        check(opaque.isAbsolute(), "3-arg absolute");
        check("mailto".equals(opaque.getScheme()), "3-arg scheme");
        check("java-net@example.com".equals(opaque.getSchemeSpecificPart()), "3-arg ssp");
        check(null == opaque.getHost(), "opaque has no host");
        check(null == opaque.getPath(), "opaque has no path");

        URI four = new URI("http", "example.com", "/a", "f");
        check("http".equals(four.getScheme()), "4-arg scheme");
        check("example.com".equals(four.getHost()), "4-arg host");
        check("/a".equals(four.getPath()), "4-arg path");
        check("f".equals(four.getFragment()), "4-arg fragment");
        check("http://example.com/a#f".equals(four.toString()), "4-arg toString");

        URI five = new URI("http", "user@example.com:80", "/a", "q", "f");
        check("user".equals(five.getUserInfo()), "5-arg userInfo");
        check("example.com".equals(five.getHost()), "5-arg host");
        check(five.getPort() == 80, "5-arg port");
        check("http://user@example.com:80/a?q#f".equals(five.toString()), "5-arg toString");

        URI seven = new URI("http", "user", "example.com", 8080, "/a/b", "x=1", "frag");
        check("http://user@example.com:8080/a/b?x=1#frag".equals(seven.toString()), "7-arg build");
        URI noport = new URI("http", "u", "h", -1, "/p", "q", "f");
        check(noport.getPort() == -1, "7-arg port -1 undefined");
        check("http://u@h/p?q#f".equals(noport.toString()), "7-arg -1 omits port");
        URI zeroport = new URI("http", "u", "h", 0, "/p", "q", "f");
        check(zeroport.getPort() == 0, "7-arg port 0");
        check("http://u@h:0/p?q#f".equals(zeroport.toString()), "7-arg port 0 string");
        checkThrows(URISyntaxException.class, () -> new URI("http", "u", "h", -2, "/p", "q", "f"), "7-arg port < -1");
    }

    static void testNullSemantics() throws Exception {
        URI base = new URI("http://a/b/c");
        checkThrows(NullPointerException.class, () -> base.resolve((URI) null), "resolve(URI null)");
        checkThrows(NullPointerException.class, () -> base.relativize((URI) null), "relativize null");
        checkThrows(NullPointerException.class, () -> base.resolve((String) null), "resolve(String null)");
        check("http://a/b/d".equals(base.resolve("d").toString()), "resolve(String) works");
    }

    static void testResolveRelativizeNormalize() throws Exception {
        URI base = new URI("http://a/b/c/d;p?q");
        check("http://a/b/c/g".equals(base.resolve("g").toString()), "resolve relative");
        check("http://a/g".equals(base.resolve("/g").toString()), "resolve absolute path");
        check("http://a/b/c/?y".equals(base.resolve("?y").toString()), "resolve query only");
        check("http://a/b/c/g#s".equals(base.resolve("g#s").toString()), "resolve with fragment");

        check("http://example.com/a/c/d".equals(new URI("http://example.com/a/b/../c/./d").normalize().toString()), "normalize dot segments");

        URI doc = new URI("http://a/b/c/d;p?q");
        check("?q#f".equals(doc.relativize(new URI("http://a/b/c/d;p?q#f")).toString()), "relativize fragment-only");
        check("http://a/b/c/e".equals(doc.relativize(new URI("http://a/b/c/e")).toString()), "relativize different authority path");
        check("c".equals(new URI("a/b").relativize(new URI("a/b/c")).toString()), "relativize nested");
    }

    static void testEqualsHashCodeCompareTo() throws Exception {
        URI u = new URI("http://example.com/a");
        check(u.equals(new URI("http://EXAMPLE.com/a")), "equals host case-insensitive");
        check(u.equals(new URI("HTTP://example.com/a")), "equals scheme case-insensitive");
        check(!u.equals(new URI("http://example.com/a#f")), "equals fragment matters");
        check(!u.equals(null), "equals null false");
        check(!u.equals("http://example.com/a"), "equals non-URI false");
        check(u.hashCode() == new URI("http://EXAMPLE.com/a").hashCode(), "hashCode host case-insensitive");
        check(u.compareTo(new URI("http://example.com/a")) == 0, "compareTo equal");
        check(new URI("http://a/b").compareTo(new URI("http://a/c")) < 0, "compareTo ordering");
        check(new URI("http://a/c").compareTo(new URI("http://a/b")) > 0, "compareTo reverse");
        check(u.compareTo(u) == 0, "compareTo self");
    }

    static void testToURLAndParseServerAuthority() throws Exception {
        URI u = new URI("http://example.com/a");
        check("http://example.com/a".equals(u.toURL().toString()), "toURL");
        checkThrows(IllegalArgumentException.class, () -> new URI("/a/b").toURL(), "toURL relative -> IAE");
        URI server = new URI("http://user@example.com:80/x");
        check(server.parseServerAuthority().equals(server), "parseServerAuthority idempotent");
        check("example.com".equals(server.parseServerAuthority().getHost()), "parseServerAuthority host");
    }

    static void testSerialization() throws Exception {
        URI u = new URI("http://user@example.com:8080/a%20b?q=1#f");
        URI u2 = roundTrip(u);
        check(u.equals(u2), "serialization equals");
        check(u.toString().equals(u2.toString()), "serialization toString");
        check("/a b".equals(u2.getPath()), "serialized decoded path");
    }

    // ==================== 深化批次 A：完整契约矩阵 ====================

    static void checkSame(Object expected, Object actual, String msg) {
        boolean ok = (expected == null) ? actual == null : expected.equals(actual);
        if (!ok)
            throw new AssertionError(msg + ": expected <" + expected + "> but was <" + actual + ">");
        passed++;
    }

    /** Runs a lambda and returns the thrown URISyntaxException (fails if not thrown). */
    static URISyntaxException usx(ThrowingRunnable r, String msg) throws Exception {
        try {
            r.run();
        } catch (URISyntaxException e) {
            return e;
        } catch (Exception e) {
            throw (Exception) e;
        } catch (Throwable t) {
            throw new AssertionError(msg, t);
        }
        throw new AssertionError(msg + ": expected URISyntaxException");
    }

    /**
     * 完整解析矩阵（最大权重）：固定样例集 × 全部组件访问器。
     * 列：input, scheme, ssp, rawSsp, authority, rawAuthority, userInfo, rawUserInfo,
     *     host, port, path, rawPath, query, rawQuery, fragment, rawFragment,
     *     isAbsolute, isOpaque, toString。
     * null = 组件未定义，"" = 组件为空串——两者是不同的契约（如 file:///x 的
     * authority 被 Java 归一为 null；而 http://h 的 path 是 ""）。
     */
    static void testParseMatrixFull() throws Exception {
        Object[][] m = {
            // 0 全组件层级 URI
            {"http://u:p@h:81/a/b?x=1#f", "http", "//u:p@h:81/a/b?x=1", "//u:p@h:81/a/b?x=1",
             "u:p@h:81", "u:p@h:81", "u:p", "u:p", "h", Integer.valueOf(81), "/a/b", "/a/b",
             "x=1", "x=1", "f", "f", Boolean.TRUE, Boolean.FALSE, "http://u:p@h:81/a/b?x=1#f"},
            // 1 相对路径引用
            {"a/b", null, "a/b", "a/b", null, null, null, null, null, Integer.valueOf(-1),
             "a/b", "a/b", null, null, null, null, Boolean.FALSE, Boolean.FALSE, "a/b"},
            // 2 协议相对（network-path）引用
            {"//h/p", null, "//h/p", "//h/p", "h", "h", null, null, "h", Integer.valueOf(-1),
             "/p", "/p", null, null, null, null, Boolean.FALSE, Boolean.FALSE, "//h/p"},
            // 3 不透明 mailto
            {"mailto:John.Doe@example.com", "mailto", "John.Doe@example.com", "John.Doe@example.com",
             null, null, null, null, null, Integer.valueOf(-1), null, null, null, null, null, null,
             Boolean.TRUE, Boolean.TRUE, "mailto:John.Doe@example.com"},
            // 4 不透明 urn
            {"urn:isbn:0-395-36341-2", "urn", "isbn:0-395-36341-2", "isbn:0-395-36341-2",
             null, null, null, null, null, Integer.valueOf(-1), null, null, null, null, null, null,
             Boolean.TRUE, Boolean.TRUE, "urn:isbn:0-395-36341-2"},
            // 5 空 authority：Java 将其归一为 null（不是 ""）
            {"file:///x", "file", "///x", "///x", null, null, null, null, null, Integer.valueOf(-1),
             "/x", "/x", null, null, null, null, Boolean.TRUE, Boolean.FALSE, "file:///x"},
            // 6 userInfo 无端口
            {"foo://info@h/p?q#f", "foo", "//info@h/p?q", "//info@h/p?q", "info@h", "info@h",
             "info", "info", "h", Integer.valueOf(-1), "/p", "/p", "q", "q", "f", "f",
             Boolean.TRUE, Boolean.FALSE, "foo://info@h/p?q#f"},
            // 7 空 path + query：path 为 ""（已定义）
            {"http://h?q", "http", "//h?q", "//h?q", "h", "h", null, null, "h", Integer.valueOf(-1),
             "", "", "q", "q", null, null, Boolean.TRUE, Boolean.FALSE, "http://h?q"},
            // 8 仅 authority：path 同样是 "" 而非 null
            {"http://h", "http", "//h", "//h", "h", "h", null, null, "h", Integer.valueOf(-1),
             "", "", null, null, null, null, Boolean.TRUE, Boolean.FALSE, "http://h"},
            // 9 path 为 "."（保留原文，normalize 才处理）
            {"http://h/.", "http", "//h/.", "//h/.", "h", "h", null, null, "h", Integer.valueOf(-1),
             "/.", "/.", null, null, null, null, Boolean.TRUE, Boolean.FALSE, "http://h/."},
            // 10 不透明 tel
            {"tel:+1-816-555-1212", "tel", "+1-816-555-1212", "+1-816-555-1212",
             null, null, null, null, null, Integer.valueOf(-1), null, null, null, null, null, null,
             Boolean.TRUE, Boolean.TRUE, "tel:+1-816-555-1212"},
            // 11 相对含 ..
            {"../g", null, "../g", "../g", null, null, null, null, null, Integer.valueOf(-1),
             "../g", "../g", null, null, null, null, Boolean.FALSE, Boolean.FALSE, "../g"},
            // 12 相对含中缀 ..
            {"g/../h", null, "g/../h", "g/../h", null, null, null, null, null, Integer.valueOf(-1),
             "g/../h", "g/../h", null, null, null, null, Boolean.FALSE, Boolean.FALSE, "g/../h"},
            // 13 协议相对 + 空 path
            {"//h", null, "//h", "//h", "h", "h", null, null, "h", Integer.valueOf(-1),
             "", "", null, null, null, null, Boolean.FALSE, Boolean.FALSE, "//h"},
            // 14 纯 fragment：path 为 ""（RFC 3986 empty path）
            {"#f", null, "", "", null, null, null, null, null, Integer.valueOf(-1),
             "", "", null, null, "f", "f", Boolean.FALSE, Boolean.FALSE, "#f"},
            // 15 空串：完全合法，除 path/ssp 为 "" 外全 null
            {"", null, "", "", null, null, null, null, null, Integer.valueOf(-1),
             "", "", null, null, null, null, Boolean.FALSE, Boolean.FALSE, ""},
            // 16 query-only 相对引用（合法，非非法）
            {"?query", null, "?query", "?query", null, null, null, null, null, Integer.valueOf(-1),
             "", "", "query", "query", null, null, Boolean.FALSE, Boolean.FALSE, "?query"},
            // 17 相对含 .
            {"./g", null, "./g", "./g", null, null, null, null, null, Integer.valueOf(-1),
             "./g", "./g", null, null, null, null, Boolean.FALSE, Boolean.FALSE, "./g"},
            // 18 不透明 file:x
            {"file:x", "file", "x", "x", null, null, null, null, null, Integer.valueOf(-1),
             null, null, null, null, null, null, Boolean.TRUE, Boolean.TRUE, "file:x"},
        };
        for (Object[] r : m) {
            URI u = new URI((String) r[0]);
            String tag = "[" + r[0] + "]";
            checkSame(r[1],  u.getScheme(),                 tag + " getScheme");
            checkSame(r[2],  u.getSchemeSpecificPart(),      tag + " getSchemeSpecificPart");
            checkSame(r[3],  u.getRawSchemeSpecificPart(),   tag + " getRawSchemeSpecificPart");
            checkSame(r[4],  u.getAuthority(),               tag + " getAuthority");
            checkSame(r[5],  u.getRawAuthority(),            tag + " getRawAuthority");
            checkSame(r[6],  u.getUserInfo(),                tag + " getUserInfo");
            checkSame(r[7],  u.getRawUserInfo(),             tag + " getRawUserInfo");
            checkSame(r[8],  u.getHost(),                    tag + " getHost");
            check(u.getPort() == (Integer) r[9],            tag + " getPort");
            checkSame(r[10], u.getPath(),                    tag + " getPath");
            checkSame(r[11], u.getRawPath(),                 tag + " getRawPath");
            checkSame(r[12], u.getQuery(),                   tag + " getQuery");
            checkSame(r[13], u.getRawQuery(),                tag + " getRawQuery");
            checkSame(r[14], u.getFragment(),                tag + " getFragment");
            checkSame(r[15], u.getRawFragment(),             tag + " getRawFragment");
            check(u.isAbsolute() == (Boolean) r[16],        tag + " isAbsolute");
            check(u.isOpaque() == (Boolean) r[17],          tag + " isOpaque");
            checkSame(r[18], u.toString(),                   tag + " toString");
        }
    }

    /**
     * URISyntaxException 精确矩阵：getReason / getIndex / getInput 三元组。
     * 探针实测（双 JDK）：除注明外全部一致；「Illegal character in authority」的
     * index 双侧不一致（JDK-8=authority 起点，JDK-27=实际非法字符位置），
     * 该两例只断言 reason/input，index 差异见 DESIGN.md 版本差异章。
     */
    static void testExactExceptionMatrix() throws Exception {
        // ---- 双侧一致：断言 reason + index + input + getMessage ----
        String[][] exact = {
            // input, reason, message 后缀
            {"1abc:x", "Illegal character in scheme name", "0"},
            {"1:", "Illegal character in scheme name", "0"},
            {"ht{tp://x/", "Illegal character in scheme name", "2"},
            {"http://exa[mple.com", "Illegal character in hostname", "10"},
            {"//[bad]/x", "IPv6 address too short", "3"},
            {"http://[::1/x", "Expected closing bracket for IPv6 address", "11"},
            {"http://h/a%zz", "Malformed escape pair", "10"},
            {"http://h/a%2", "Malformed escape pair", "10"},
            {"http://h/a\\b", "Illegal character in path", "10"},
        };
        for (String[] r : exact) {
            URISyntaxException e = usx(() -> new URI(r[0]), r[0]);
            int idx = Integer.parseInt(r[2]);
            check(r[1].equals(e.getReason()), r[0] + " reason=" + e.getReason());
            check(e.getIndex() == idx, r[0] + " index=" + e.getIndex() + " expected " + idx);
            check(r[0].equals(e.getInput()), r[0] + " input");
            check((r[1] + " at index " + idx + ": " + r[0]).equals(e.getMessage()),
                    r[0] + " message=" + e.getMessage());
        }
        // ---- 7 参构造器 port < -1：input 是拼装出的串 ----
        URISyntaxException e7 = usx(() -> new URI("http", "u", "h", -2, "/p", "q", "f"), "7arg -2");
        check("Illegal character in port number".equals(e7.getReason()), "7arg -2 reason");
        check(e7.getIndex() == 11, "7arg -2 index=" + e7.getIndex());
        check("http://u@h:-2/p?q#f".equals(e7.getInput()), "7arg -2 input=" + e7.getInput());
        // ---- 3 参 ssp=null：Expected scheme-specific part ----
        URISyntaxException e3 = usx(() -> new URI("mailto", null, null), "3arg null ssp");
        check("Expected scheme-specific part".equals(e3.getReason()), "3arg null ssp reason");
        check(e3.getIndex() == 7, "3arg null ssp index=" + e3.getIndex());
        check("mailto:".equals(e3.getInput()), "3arg null ssp input");
        // ---- 单串 "s:" 与 3 参等价 ----
        URISyntaxException es = usx(() -> new URI("s:"), "s:");
        check("Expected scheme-specific part".equals(es.getReason()), "s: reason");
        check(es.getIndex() == 2, "s: index");
        check("s:".equals(es.getInput()), "s: input");
        // ---- 5 参相对 path + scheme：index 未知(-1) ----
        URISyntaxException e5 = usx(() -> new URI("s", null, "p", null, null), "5arg rel path");
        check("Relative path in absolute URI".equals(e5.getReason()), "5arg rel path reason");
        check(e5.getIndex() == -1, "5arg rel path index=" + e5.getIndex());
        check("s:p".equals(e5.getInput()), "5arg rel path input");
        // ---- 双侧 index 不一致：只断言 reason/input ----
        // "http://exa mple.com/x": JDK-8 index=7(authority 起点) vs JDK-27 index=10(非法字符处)
        URISyntaxException esp = usx(() -> new URI("http://exa mple.com/x"), "space in authority");
        check("Illegal character in authority".equals(esp.getReason()), "space-in-auth reason");
        check("http://exa mple.com/x".equals(esp.getInput()), "space-in-auth input");
        // "http://exa<mple.com/": JDK-8 index=7 vs JDK-27 index=10
        URISyntaxException elt = usx(() -> new URI("http://exa<mple.com/"), "lt in authority");
        check("Illegal character in authority".equals(elt.getReason()), "lt-in-auth reason");
        check("http://exa<mple.com/".equals(elt.getInput()), "lt-in-auth input");
        // ---- registry-based authority：非数字端口不抛，host=null port=-1 ----
        URI rp = new URI("http://h:port/");
        check("h:port".equals(rp.getAuthority()), "registry authority kept");
        check(null == rp.getHost(), "registry host null");
        check(rp.getPort() == -1, "registry port -1");
        // 数字端口超 65535 也照收（构造器不校验上界）
        URI rp2 = new URI("http://u@h:70000/");
        check("h".equals(rp2.getHost()), "out-of-range port host");
        check(rp2.getPort() == 70000, "out-of-range port value");
    }

    /**
     * RFC 3986 §5.3 引用变换表（base = http://a/b/c/d;p?q）。
     * 探针实测双 JDK 一致；注意 Java 遵循 RFC 2396 §5.2，与 RFC 3986 有三处
     * 已知偏差（见 DESIGN.md）：""/"?y" 用 base 目录路径（丢 d;p 段）、绝对
     * path 引用不 normalize、越顶 ".." 保留。
     */
    static void testResolveReferenceTable() throws Exception {
        URI base = new URI("http://a/b/c/d;p?q");
        String[][] t = {
            {"g", "http://a/b/c/g"}, {"./g", "http://a/b/c/g"}, {"g/", "http://a/b/c/g/"},
            {"/g", "http://a/g"}, {"//g", "http://g"}, {"?y", "http://a/b/c/?y"},
            {"g?y", "http://a/b/c/g?y"}, {"#s", "http://a/b/c/d;p?q#s"}, {"g#s", "http://a/b/c/g#s"},
            {"g?y#s", "http://a/b/c/g?y#s"}, {";x", "http://a/b/c/;x"}, {"g;x", "http://a/b/c/g;x"},
            {"g;x?y#s", "http://a/b/c/g;x?y#s"}, {"", "http://a/b/c/"}, {".", "http://a/b/c/"},
            {"./", "http://a/b/c/"}, {"..", "http://a/b/"}, {"../", "http://a/b/"},
            {"../g", "http://a/b/g"}, {"../../", "http://a/"}, {"../..", "http://a/"},
            {"../../../g", "http://a/../g"}, {"/./g", "http://a/./g"}, {"/../g", "http://a/../g"},
            {"g.", "http://a/b/c/g."}, {".g", "http://a/b/c/.g"}, {"g..", "http://a/b/c/g.."},
            {"..g", "http://a/b/c/..g"}, {"./../g", "http://a/b/g"}, {"./g/.", "http://a/b/c/g/"},
            {"g/./h", "http://a/b/c/g/h"}, {"g/../h", "http://a/b/c/h"},
            {"g;x=1/./y", "http://a/b/c/g;x=1/y"}, {"g;x=1/../y", "http://a/b/c/y"},
        };
        for (String[] r : t)
            check(r[1].equals(base.resolve(r[0]).toString()),
                    "resolve(\"" + r[0] + "\")=" + base.resolve(r[0]) + " expected " + r[1]);
    }

    /** resolve 的对象同一性契约（源码保证返回入参本身的三种情形）+ 相对 base。 */
    static void testResolveIdentitySemantics() throws Exception {
        URI base = new URI("http://a/b/c/d;p?q");
        URI absChild = new URI("ftp://x/y");
        check(base.resolve(absChild) == absChild, "resolve(absolute child) returns same object");
        URI absSame = new URI("http://z/q#f");
        check(base.resolve(absSame) == absSame, "resolve(absolute same-scheme child) returns same object");
        URI opChild = new URI("mailto:z@w");
        check(base.resolve(opChild) == opChild, "resolve(opaque child) returns same object");
        URI opBase = new URI("mailto:a@b");
        URI relChild = new URI("z");
        check(opBase.resolve(relChild) == relChild, "opaque base + relative child returns child");
        // 孤立 fragment：同 fragment 返回 base 本身；不同 fragment 复制 base 换 fragment
        URI baseFrag = new URI("http://a/b?q#f");
        URI sameFrag = new URI("#f");
        check(baseFrag.resolve(sameFrag) == baseFrag, "resolve(#same-fragment) returns base itself");
        check("http://a/b?q#g".equals(baseFrag.resolve(new URI("#g")).toString()),
                "resolve(#other-fragment) swaps fragment");
        check("http://a/b#f".equals(new URI("http://a/b").resolve(new URI("#f")).toString()),
                "resolve(#f) keeps base sans query");
        // 相对 base（无 scheme）：正常合并
        check("a/c".equals(new URI("a/b").resolve("c").toString()), "relative base merge");
        check("a/g/h".equals(new URI("a/b").resolve("g/./h").toString()), "relative base dot merge");
        check("../g".equals(new URI("a/b").resolve("../../g").toString()),
                "relative base .. beyond root preserved");
        check("http://a/../..".equals(new URI("http://a/b").resolve("../..").toString()),
                "absolute base .. beyond root preserved");
        check("http://a/b/c/".equals(new URI("http://a/b/c/d").resolve(".").toString()), "resolve .");
        check("http://a/b/".equals(new URI("http://a/b/c/d").resolve("..").toString()), "resolve ..");
        check("http://x/y".equals(new URI("http://a/b/c/d").resolve("//x/y").toString()),
                "child authority wins");
        check("http://a/b/c/?y".equals(new URI("http://a/b/c/d").resolve("?y").toString()),
                "query-only ref uses base dir");
        // resolve(URI(""))) 与 resolve("") 一致
        check(base.resolve(new URI("")).toString().equals(base.resolve("").toString()),
                "resolve(URI empty) == resolve(\"\")");
    }

    /** relativize 全组合：前缀裁剪、不前缀→返回原对象（同一性）、双侧 normalize。 */
    static void testRelativizeMatrix() throws Exception {
        URI doc = new URI("http://a/b/c/d;p?q");
        check("?q#f".equals(doc.relativize(new URI("http://a/b/c/d;p?q#f")).toString()),
                "relativize same doc + fragment");
        check("?q#f".equals(new URI("http://a/b/c/d").relativize(new URI("http://a/b/c/d?q#f")).toString()),
                "relativize same path + query + fragment");
        check("c/d".equals(new URI("http://a/b/").relativize(new URI("http://a/b/c/d")).toString()),
                "relativize nested dirs");
        check("c".equals(new URI("a/b").relativize(new URI("a/b/c")).toString()), "relativize relative");
        check("".equals(new URI("http://a/b/c").relativize(new URI("http://a/b/c")).toString()),
                "relativize self yields empty URI");
        check(new URI("http://a/b/c").relativize(new URI("http://a/b/c")).equals(new URI("")),
                "relativize self equals empty URI");
        check("".equals(new URI("http://a/b").relativize(new URI("http://a/b/")).toString()),
                "relativize dir yields empty path");
        // base 不是 target 的目录前缀（base path 以叶结尾）→ 返回 target 本身（同一对象）
        URI t1 = new URI("http://a/b/c/e");
        check(doc.relativize(t1) == t1, "base leaf not prefix returns target object");
        URI t2 = new URI("http://a/g");
        check(new URI("http://a/b/c/f?q").relativize(t2) == t2,
                "upward ../.. cannot be expressed, returns target");
        URI t3 = new URI("http://x/b");
        check(new URI("http://a/b").relativize(t3) == t3, "different host returns target object");
        URI t4 = new URI("http://a/c");
        check(new URI("ftp://a/b").relativize(t4) == t4, "different scheme returns target object");
        URI t5 = new URI("http://a/c");
        check(new URI("a/b").relativize(t5) == t5,
                "relative base + absolute target returns target");
        URI t6 = new URI("mailto:a@b");
        URI b6 = new URI("mailto:a@b");
        check(b6.relativize(t6) == t6, "opaque base returns target object");
        URI t7 = new URI("g/../h");
        URI b7 = new URI("a/b");
        check(b7.relativize(t7) == t7,
                "non-prefix after normalize returns original child object");
        URI t8 = new URI("http://a/b");
        check(new URI("http://a/b/").relativize(t8) == t8, "dir-base + shorter target returns target");
        // javadoc 恒等式：u.relativize(u.resolve(v)) == v（v 相对）；u.resolve(u.relativize(v)) == v（v 绝对）
        URI u = new URI("http://a/b/");
        URI vrel = new URI("c/d");
        check(u.relativize(u.resolve(vrel)).equals(vrel), "relativize(resolve(v)) round-trip");
        URI vabs = new URI("http://a/b/c/d");
        check(u.resolve(u.relativize(vabs)).equals(vabs), "resolve(relativize(v)) round-trip");
    }

    /** normalize 边界：越顶 ".." 保留（RFC 3986 会裁剪——Java 有意保留）、空 path、相对路径。 */
    static void testNormalizeMatrix() throws Exception {
        String[][] t = {
            {"/a/b/../c", "/a/c"}, {"/a/b/../../c", "/c"}, {"/a/b/../../../c", "/../c"},
            {"/a/./b", "/a/b"}, {"a/b/..", "a/"}, {"a/b/../", "a/"}, {"a/..", ""},
            {"..", ".."}, {"../..", "../.."}, {"../../c", "../../c"}, {"./a", "a"},
            {"a/.", "a/"}, {"a/./b/./c", "a/b/c"}, {"/a/b/c/../../../..", "/.."},
            {"/..", "/.."}, {"/./", "/"}, {"", ""}, {".", ""}, {"/a/b/../..", "/"},
            {"a/b/../../../c", "../c"},
        };
        for (String[] r : t)
            check(r[1].equals(new URI(r[0]).normalize().toString()),
                    "normalize(" + r[0] + ")=" + new URI(r[0]).normalize() + " expected " + r[1]);
        check("http://a/c".equals(new URI("http://a/b/../c").normalize().toString()),
                "normalize whole URI");
        check("http://a/b?../.".equals(new URI("http://a/./b?../.").normalize().toString()),
                "normalize leaves query untouched");
        check("http://h?q".equals(new URI("http://h?q").normalize().toString()),
                "normalize empty path returns equivalent URI");
    }

    /** 已 normal 的 URI，normalize() 返回 this（对象同一性）；未 normal 的必须返回新对象。 */
    static void testNormalizationIsIdempotentWhenAlreadyNormal() throws Exception {
        URI normal = new URI("/a/b/c");
        check(normal.normalize() == normal, "already-normal path returns this");
        URI full = new URI("http://a/b/c?q#f");
        check(full.normalize() == full, "already-normal full URI returns this");
        URI opaque = new URI("mailto:a@b");
        check(opaque.normalize() == opaque, "opaque URI returns this");
        URI emptyPath = new URI("http://h?q");
        check(emptyPath.normalize() == emptyPath, "empty path returns this");
        URI fragOnly = new URI("#f");
        check(fragOnly.normalize() == fragOnly, "fragment-only returns this");
        URI dotted = new URI("./g");
        check(dotted.normalize() != dotted, "non-normal path must return new URI");
        check("g".equals(dotted.normalize().toString()), "./g normalizes to g");
        // 幂等：normalize(normalize(x)) 与 normalize(x) equals 且第二次返回同一对象
        URI once = new URI("/a/b/../../../c").normalize();
        check("/../c".equals(once.toString()), "beyond-root .. preserved");
        check(once.normalize() == once, "second normalize is identity");
        check(new URI("a/b/../../../c").normalize().normalize()
                .equals(new URI("a/b/../../../c").normalize()), "relative idempotent");
    }

    /**
     * equals/hashCode 深规则：scheme/host 大小写不敏感；userInfo/path/query/fragment
     * 大小写敏感且按 raw 形式比较（javadoc：转义十六进制数字不分大小写，但不做解码
     * 等价——%41 不等于 A、%6e 等于 %6E）；IPv6 压缩形式互不相等、hex 大小写相等；
     * port -1 与 80 不等。equals==true ⇒ hashCode 必等。
     */
    static void testEqualsHashCodeEscapes() throws Exception {
        // 不等（raw 形式比较，非解码等价）
        String[][] neq = {
            {"http://h/a%41", "http://h/aA"}, {"http://h/a%41", "http://h/a%61"},
            {"http://h/a%6e", "http://h/an"}, {"http://h/?q%41", "http://h/?qA"},
            {"http://h/#f%41", "http://h/#fA"}, {"http://%75@h/", "http://u@h/"},
            {"http://[::1]:80/", "http://[0:0:0:0:0:0:0:1]:80/"}, {"http://h", "http://h/"},
            {"http://h/x", "http://h:80/x"}, {"http://U@h/", "http://u@h/"},
            {"http://h/?Q", "http://h/?q"}, {"http://h/#F", "http://h/#f"},
            {"mailto:X@y", "mailto:x@y"}, {"mailto:a%41", "mailto:aA"},
            {"http://h/a%2Fb", "http://h/a/b"}, {"//[::1]/x", "//[0:0:0:0:0:0:0:1]/x"},
        };
        for (String[] r : neq) {
            URI a = new URI(r[0]), b = new URI(r[1]);
            check(!a.equals(b), "neq " + r[0] + " vs " + r[1]);
            check(!b.equals(a), "neq symmetric " + r[1] + " vs " + r[0]);
        }
        // 相等（hex 大小写折叠 / 组件大小写规则）
        String[][] eq = {
            {"http://h/a%6e", "http://h/a%6E"}, {"http://[::FFFF:1]/", "http://[::ffff:1]/"},
            {"http://H/x", "http://h/x"}, {"HTTP://h/x", "http://h/x"},
            {"news:comp.lang.java", "news:comp.lang.java"}, {"", ""}, {"#f", "#f"},
        };
        for (String[] r : eq) {
            URI a = new URI(r[0]), b = new URI(r[1]);
            check(a.equals(b), "eq " + r[0] + " vs " + r[1]);
            check(b.equals(a), "eq symmetric " + r[1] + " vs " + r[0]);
            check(a.hashCode() == b.hashCode(), "hashCode contract " + r[0]);
        }
        // equals 传参为 null / 非 URI
        check(!new URI("http://h/").equals(null), "equals(null) false");
        check(!new URI("http://h/").equals("http://h/"), "equals(non-URI) false");
        // opaque vs hierarchical 同 scheme 不等
        check(!new URI("http://h/p").equals(new URI("http://x")), "hierarchical vs opaque unequal");
        // hashCode 稳定
        check(new URI("http://h/a%41").hashCode() == new URI("http://h/a%41").hashCode(),
                "hashCode stable");
    }

    /** compareTo：固定排序表 + 字段序（scheme→ssp/authority→path→query→fragment）。 */
    static void testCompareToOrdering() throws Exception {
        String[] unsorted = {"http://b", "mailto:x", "", "ftp://a", "a/b", "http://a"};
        String[] expected = {"", "a/b", "ftp://a", "http://a", "http://b", "mailto:x"};
        java.util.List<URI> l = new java.util.ArrayList<URI>();
        for (String s : unsorted) l.add(new URI(s));
        java.util.Collections.sort(l);
        for (int i = 0; i < expected.length; i++)
            check(expected[i].equals(l.get(i).toString()),
                    "sorted[" + i + "]=" + l.get(i) + " expected " + expected[i]);
        check(new URI("ftp://a").compareTo(new URI("http://a")) < 0, "scheme order ftp<http");
        check(new URI("http://a").compareTo(new URI("http://b")) < 0, "host order");
        check(new URI("http://b").compareTo(new URI("http://a")) > 0, "host order reverse");
        check(new URI("mailto:x").compareTo(new URI("http://a")) > 0, "scheme order http<mailto");
        check(new URI("").compareTo(new URI("a/b")) < 0, "undefined scheme sorts first");
        check(new URI("a/b").compareTo(new URI("http://a")) < 0, "relative before absolute");
        check(new URI("mailto:a").compareTo(new URI("mailto:b")) < 0, "opaque ssp order");
        check(new URI("http://a/x").compareTo(new URI("http://a/y")) < 0, "path order");
        check(new URI("http://a/?q1").compareTo(new URI("http://a/?q2")) < 0, "query order");
        check(new URI("http://a/#f1").compareTo(new URI("http://a/#f2")) < 0, "fragment order");
        check(new URI("http://a/").compareTo(new URI("http://a/#f")) < 0,
                "undefined fragment sorts first");
        check(new URI("HTTP://z").compareTo(new URI("http://a")) > 0,
                "scheme compared ignoring case, then rest");
        check(new URI("http://A/").compareTo(new URI("http://a/")) == 0,
                "host case-insensitive in compareTo");
        check(new URI("http://a").compareTo(new URI("http://a")) == 0, "compareTo equal 0");
        // JDK-8 与 JDK-27 差异：new URI("http://h/a%6e").compareTo(new URI("http://h/a%6E"))
        // JDK-8 返回非 0（compareTo 与 equals 不一致），JDK-27 返回 0（一致）——不断言。
        checkThrows(NullPointerException.class,
                () -> new URI("http://a").compareTo(null), "compareTo(null) NPE");
    }

    /**
     * toASCIIString/IRI：Java 对非 ASCII 全部百分号编码（host 不做 IDNA punycode，
     * 双侧一致）；round-trip 解码等价但 equals 为 false（equals 按 raw 形式比较）。
     */
    static void testToASCIIStringIRI() throws Exception {
        URI idn = new URI("http://例え.jp/");
        check("http://%E4%BE%8B%E3%81%88.jp/".equals(idn.toASCIIString()),
                "toASCIIString percent-encodes host (no punycode): " + idn.toASCIIString());
        check("http://%E4%BE%8B%E3%81%88@%E4%BE%8B%E3%81%88.jp/"
                .equals(new URI("http://例え@例え.jp/").toASCIIString()),
                "toASCIIString encodes userInfo too");
        check("http://h/%E4%BE%8B%E3%81%88".equals(new URI("http://h/例え").toASCIIString()),
                "non-ascii path encoded as UTF-8 bytes");
        check("http://h/?%E4%BE%8B".equals(new URI("http://h/?例").toASCIIString()),
                "non-ascii query encoded");
        check("http://h/#%E4%BE%8B".equals(new URI("http://h/#例").toASCIIString()),
                "non-ascii fragment encoded");
        check("/%E4%BE%8B".equals(new URI("/例").toASCIIString()), "relative URI encoded");
        URI asciiOnly = new URI("http://h/a%41?q#f");
        check(asciiOnly.toASCIIString().equals(asciiOnly.toString()),
                "already-ASCII toASCIIString == toString");
        // round-trip：解码组件相同，但 equals false（raw 形式不同）
        URI orig = new URI("http://h/#例");
        URI rt = URI.create(orig.toASCIIString());
        check("例".equals(rt.getFragment()), "round-trip fragment decodes back");
        check(!orig.equals(rt), "round-trip not equals (raw comparison)");
        URI origP = new URI("http://h/例え");
        URI rtP = URI.create(origP.toASCIIString());
        check("/例え".equals(rtP.getPath()), "round-trip path decodes back");
        check(!origP.equals(rtP), "round-trip path not equals");
        check("http://h/#%E4%BE%8B".equals(rt.toASCIIString()), "round-trip of round-trip");
        // toASCIIString().toURL() 可用（host 为百分号编码形式）
        check("%E4%BE%8B%E3%81%88.jp".equals(URI.create(idn.toASCIIString()).toURL().getHost()),
                "ascii form usable as URL host");
    }

    /** toURL 边界：fragment 保留、file 空 authority、相对 IAE、未知协议 MFE。 */
    static void testToURLEdgeCases() throws Exception {
        URL u = new URI("http://h:8080/a?b#c").toURL();
        check("http://h:8080/a?b#c".equals(u.toString()), "toURL keeps query+fragment");
        check("c".equals(u.getRef()), "toURL ref");
        check("b".equals(u.getQuery()), "toURL query");
        check("file:/x".equals(new URI("file:///x").toURL().toString()),
                "toURL collapses empty authority");
        check("file".equals(new URI("file:///x").toURL().getProtocol()), "toURL protocol");
        try {
            new URI("//h/x").toURL();
            check(false, "relative toURL should throw");
        } catch (IllegalArgumentException e) {
            check("URI is not absolute".equals(e.getMessage()), "relative toURL message");
        }
        try {
            new URI("urn:isbn:x").toURL();
            check(false, "urn toURL should throw");
        } catch (java.net.MalformedURLException e) {
            check(e.getMessage() != null && e.getMessage().contains("unknown protocol"),
                    "urn toURL message: " + e.getMessage());
        }
    }

    /** 多参构造器边界：5 参 authority 型、全 null、registry authority、port 上界不校验。 */
    static void testConstructorEdgeMatrix() throws Exception {
        URI five = new URI("http", "h:80", "/p", "q", "f");
        check("http://h:80/p?q#f".equals(five.toString()), "5-arg authority toString");
        check("h".equals(five.getHost()), "5-arg authority host");
        check(five.getPort() == 80, "5-arg authority port");
        check("s:/p?q#f".equals(new URI("s", null, "/p", "q", "f").toString()),
                "5-arg null authority hierarchical");
        URI all = new URI(null, null, null, null, null);
        check("".equals(all.toString()), "5-arg all-null -> empty URI");
        check(all.equals(new URI("")), "5-arg all-null equals empty");
        check(all.isOpaque() == false && all.isAbsolute() == false, "5-arg all-null flags");
        check("s:/p".equals(new URI("s", null, "/p", null).toString()), "4-arg null host");
        URI reg = new URI("http", "h:-2", "/p", "q", "f");
        check("http://h:-2/p?q#f".equals(reg.toString()), "5-arg bad port authority kept");
        check(null == reg.getHost(), "5-arg bad port host null (registry-based)");
        check(reg.getPort() == -1, "5-arg bad port -1");
        check(new URI("http", "u", "h", 65535, "/p", null, null).getPort() == 65535,
                "7-arg port 65535");
        check(new URI("http", "u", "h", 65536, "/p", null, null).getPort() == 65536,
                "7-arg port 65536 not validated");
    }

    /** create() 语义：合法、空串、IAE 包装（cause 精确）、null NPE。 */
    static void testCreateSemantics() throws Exception {
        URI empty = URI.create("");
        check("".equals(empty.toString()), "create(\"\") legal");
        check(null == empty.getScheme() && null == empty.getFragment(), "create(\"\") fields");
        URI ok = URI.create("http://h/a");
        check(ok.equals(new URI("http://h/a")), "create equals constructor");
        check(ok.hashCode() == new URI("http://h/a").hashCode(), "create hashCode");
        try {
            URI.create("1abc:x");
            check(false, "create illegal should throw");
        } catch (IllegalArgumentException e) {
            check(e.getCause() instanceof URISyntaxException, "create IAE cause is USX");
            URISyntaxException c = (URISyntaxException) e.getCause();
            check("Illegal character in scheme name".equals(c.getReason()), "cause reason");
            check(c.getIndex() == 0, "cause index");
            check("1abc:x".equals(c.getInput()), "cause input");
            check(e.getMessage().equals(c.getMessage()), "IAE message == cause message");
        }
        try {
            URI.create("http://exa mple.com/x");
            check(false, "create illegal space should throw");
        } catch (IllegalArgumentException e) {
            // index 双侧不一致（JDK-8=7 / JDK-27=10），只断言 cause 类型与 reason
            check(e.getCause() instanceof URISyntaxException, "space cause is USX");
            check("Illegal character in authority".equals(((URISyntaxException) e.getCause()).getReason()),
                    "space cause reason");
        }
        // create(null) NPE：JDK-8 message=null，JDK-27 有详细 message——只断言类型
        checkThrows(NullPointerException.class, () -> URI.create(null), "create(null)");
    }

    /** parseServerAuthority：registry 端口/方括号/IPv6 future 精确异常 + 返回 this。 */
    static void testParseServerAuthorityDeep() throws Exception {
        URISyntaxException e = usx(() -> new URI("//h:xx/").parseServerAuthority(), "psa //h:xx/");
        check("Illegal character in port number".equals(e.getReason()), "psa port reason");
        check(e.getIndex() == 4, "psa port index=" + e.getIndex());
        check("//h:xx/".equals(e.getInput()), "psa port input");
        e = usx(() -> new URI("http://exa[mple/").parseServerAuthority(), "psa bracket");
        check("Illegal character in hostname".equals(e.getReason()), "psa bracket reason");
        check(e.getIndex() == 10, "psa bracket index=" + e.getIndex());
        check("http://exa[mple/".equals(e.getInput()), "psa bracket input");
        URISyntaxException v = usx(() -> new URI("//[v1.a]/x").parseServerAuthority(), "psa ipvfuture");
        check("Malformed IPv6 address".equals(v.getReason()), "psa ipvfuture reason");
        check(v.getIndex() == 3, "psa ipvfuture index=" + v.getIndex());
        // 成功路径全部返回 this（源码：host!=null 或 authority==null 短路）
        URI server = new URI("http://user@example.com:80/x");
        check(server.parseServerAuthority() == server, "already server-based returns this");
        check("//[::1]:80/x".equals(new URI("//[::1]:80/x").parseServerAuthority().toString()),
                "IPv6 authority ok");
        check("[::1]".equals(new URI("//[::1]:80/x").parseServerAuthority().getHost()),
                "IPv6 host with brackets");
        URI noAuth = new URI("a/b");
        check(noAuth.parseServerAuthority() == noAuth, "no authority returns this");
        URI opaque = new URI("mailto:a@b");
        check(opaque.parseServerAuthority() == opaque, "opaque returns this");
    }

    /** 序列化补充：不透明/相对/空/非 ASCII URI round-trip。 */
    static void testSerializationMore() throws Exception {
        URI o = new URI("mailto:John.Doe@example.com");
        URI o2 = roundTrip(o);
        check(o.equals(o2), "opaque serialization equals");
        check(o.isOpaque() == o2.isOpaque(), "opaque flag preserved");
        URI r = new URI("../g?x#f");
        URI r2 = roundTrip(r);
        check(r.equals(r2), "relative serialization equals");
        check("../g".equals(r2.getPath()), "relative path preserved");
        URI e = new URI("");
        check(roundTrip(e).equals(e), "empty URI serialization");
        URI n = new URI("http://h/#例");
        URI n2 = roundTrip(n);
        check(n.equals(n2), "non-ascii serialization equals");
        check("例".equals(n2.getFragment()), "non-ascii fragment preserved");
        check(n2.toASCIIString().equals(n.toASCIIString()), "non-ascii toASCIIString preserved");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testSingleStringComponents();
        testRawVsDecoded();
        testNonAsciiAndAscii();
        testConstructorValidation();
        testMultiArgConstructors();
        testNullSemantics();
        testResolveRelativizeNormalize();
        testEqualsHashCodeCompareTo();
        testToURLAndParseServerAuthority();
        testSerialization();
        testParseMatrixFull();
        testExactExceptionMatrix();
        testResolveReferenceTable();
        testResolveIdentitySemantics();
        testRelativizeMatrix();
        testNormalizeMatrix();
        testNormalizationIsIdempotentWhenAlreadyNormal();
        testEqualsHashCodeEscapes();
        testCompareToOrdering();
        testToASCIIStringIRI();
        testToURLEdgeCases();
        testConstructorEdgeMatrix();
        testCreateSemantics();
        testParseServerAuthorityDeep();
        testSerializationMore();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
