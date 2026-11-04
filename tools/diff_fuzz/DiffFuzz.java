import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lightweight differential-fuzzing harness: JDK-8 vs JDK-27, black-box,
 * no bytecode instrumentation of bootstrap classes, no JQF/Randoop.
 *
 * Determinism contract:
 *  - single fixed Random seed (SEED);
 *  - every case is generated in a fixed order, so both JDK runs emit the
 *    exact same line sequence;
 *  - each stdout line is "category<TAB>index<TAB>escaped-input<TAB>escaped-result";
 *  - a leading "META" line reports the JDK version (skip it when diffing).
 *
 * Build (with JDK-8 javac so the class runs on both JDKs):
 *   $JAVA8_HOME/bin/javac -encoding UTF-8 -d out DiffFuzz.java
 * Run once per JDK:
 *   $JAVA_HOME/bin/java -Dfile.encoding=UTF-8 -cp out DiffFuzz > out/<jdk>.txt
 * Then diff out/jdk8.txt vs out/jdk27.txt (ignore META lines).
 */
public class DiffFuzz {

    static final long SEED = 0x51D1FF2026L;
    static final Random R = new Random(SEED);

    // Input alphabets for deterministic random generation.
    static final String NUM_CHARS = "0123456789abcdefABCDEFxX+-.eEpPfF_ ,'\"\\\t\n\r";
    static final String STR_CHARS =
        "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 \\\t\n\r" +
        ".-_+*/:;{}[]()<>!@#$%^&'\"é中€ İıﬁ";
    static final String PROP_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 =:.#!\\\t\n\r\u0000";

    static final String[] SUBJECTS = {
        "", "a", "abc", "aaab", "123", "hello world", "  \t\n", "İı",
        "Aé", "word end", "abc123", "中文"
    };

    static final String[] REGEX_FRAG = {
        "a", "b", "c", "x", "y", "z", "\\d", "\\D", "\\w", "\\W", "\\s", "\\S",
        ".", "[a-z]", "[^a]", "[0-9]", "[abc]", "a*", "a+", "a?", "a*?", "a+?", "a??",
        "a{1,3}", "a{2}", "a{2,}", "a{0,3}?", "a|b", "(ab)", "(a)(b)", "(?<n>a)",
        "(?:ab)", "(?=a)", "(?!a)", "(?<=a)", "(?<!a)", "^", "$", "\\b", "\\B", "\\A",
        "\\z", "(?i)", "(?u)", "(?s)", "(?m)", "[a-z&&[^b]]", "\\p{L}", "\\p{Lu}",
        "\\p{javaLowerCase}", "\\p{IsLatin}", "\\R", "\\h", "\\v", "\\X", "\\cA",
        "\\u0041", "\\x41", "\\Qabc\\E", "a{2,1}", "[z-a]", "(", "[", "\\", "*a",
        "a++", "a*+", "a?+", "(?<1>a)"
    };

    static final String[] DEC_PARSE_SEEDS = {
        "0", "1", "1.23", "-1.23", "1,234.5", "1234.567", "1E3", "1e3", "1E", ".5",
        "5.", "abc", "", " ", "12,34,56", "1.2.3", "٤٥.٦", "NaN", "Infinity", "1e309"
    };

    public static void main(String[] args) throws Exception {
        System.out.println("META\tjdk=" + System.getProperty("java.version") + "\tseed=" + SEED);
        fuzzInteger();
        fuzzLong();
        fuzzDouble();
        fuzzFloat();
        fuzzRegex();
        fuzzNumberFormat();
        fuzzDecimalFormat();
        fuzzURI();
        fuzzURL();
        fuzzProperties();
        System.out.flush();
    }

    // ---------------------------------------------------------------- emit

    static void emit(String cat, int idx, String input, String result) {
        System.out.println(cat + "\t" + idx + "\t" + esc(input) + "\t" + esc(result));
    }

    static String esc(String s) {
        if (s == null) return "\\x00";
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\': sb.append("\\\\"); break;
                case '\t': sb.append("\\t"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                default:
                    if (c < 0x20 || c == 0x7f) {
                        sb.append("\\x").append(hex2(c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }

    static String hex2(int c) {
        String h = Integer.toHexString(c & 0xff);
        return h.length() == 1 ? "0" + h : h;
    }

    static String randStr(String alphabet, int maxLen) {
        int len = R.nextInt(maxLen + 1);
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(alphabet.charAt(R.nextInt(alphabet.length())));
        }
        return sb.toString();
    }

    static String randRegex(int maxFrags) {
        int n = R.nextInt(maxFrags) + 1;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            if (R.nextInt(5) == 0) {
                sb.append(randStr("ab0\\[]().?*+{}|", 4));
            } else {
                sb.append(REGEX_FRAG[R.nextInt(REGEX_FRAG.length)]);
            }
        }
        return sb.toString();
    }

    // ---------------------------------------------------------- Integer

    static void fuzzInteger() {
        String[] seeds = {
            "0", "-0", "+0", "1", "-1", "2147483647", "2147483648", "-2147483648",
            "-2147483649", " 1", "1 ", "\t1", "1\n", "", " ", "0x10", "0X10", "-0x10",
            "+0x10", "0x", "-0x", "010", "-010", "0b10", "1_000", "+", "-", "--1", "++1",
            "1.0", "1e2", "١٢٣", "１２３", "9223372036854775807", "null", "\u0000",
            "0x80000000", "-0x80000000", "0x7fffffff", "2147483647 ", " ", "0x1L",
            "1L", "0.0", "-0.0", "Infinity", "NaN"
        };
        int idx = 0;
        for (String s : seeds) emit("Integer.parseInt", idx++, s, intParse(s, 10));
        for (int i = 0; i < 1500; i++) {
            String s = randStr(NUM_CHARS, 24);
            emit("Integer.parseInt", idx++, s, intParse(s, 10));
        }

        idx = 0;
        for (String s : seeds) emit("Integer.decode", idx++, s, intDecode(s));
        for (int i = 0; i < 800; i++) {
            String s = randStr(NUM_CHARS, 20);
            emit("Integer.decode", idx++, s, intDecode(s));
        }

        int[] radixes = {2, 8, 10, 16, 36, 3, 20, 1, 37, 0};
        idx = 0;
        for (int i = 0; i < 800; i++) {
            String s = randStr(NUM_CHARS, 20);
            int r = radixes[R.nextInt(radixes.length)];
            emit("Integer.parseInt-radix", idx++, s + "|radix=" + r, intParse(s, r));
        }
    }

    static String intParse(String s, int radix) {
        try { return "ok=" + Integer.parseInt(s, radix); }
        catch (Exception e) { return "EXC=" + e.getClass().getSimpleName(); }
    }

    static String intDecode(String s) {
        try { return "ok=" + Integer.decode(s); }
        catch (Exception e) { return "EXC=" + e.getClass().getSimpleName(); }
    }

    // ------------------------------------------------------------ Long

    static void fuzzLong() {
        String[] seeds = {
            "0", "-0", "+0", "1", "-1", "9223372036854775807", "9223372036854775808",
            "-9223372036854775808", "-9223372036854775809", " 1", "1 ", "\t1", "1\n", "",
            " ", "0x10", "0X10", "-0x10", "+0x10", "0x", "-0x", "010", "-010", "0b10",
            "1_000", "+", "-", "--1", "++1", "1.0", "1e2", "١٢٣", "１２３", "null", "\u0000",
            "0x8000000000000000", "-0x8000000000000000", "0x7fffffffffffffff", "2147483647 ",
            " ", "0x1L", "1L", "0.0", "-0.0", "Infinity", "NaN", "0x7fffffffffffffffL"
        };
        int idx = 0;
        for (String s : seeds) emit("Long.parseLong", idx++, s, longParse(s, 10));
        for (int i = 0; i < 1500; i++) {
            String s = randStr(NUM_CHARS, 24);
            emit("Long.parseLong", idx++, s, longParse(s, 10));
        }

        idx = 0;
        for (String s : seeds) emit("Long.decode", idx++, s, longDecode(s));
        for (int i = 0; i < 800; i++) {
            String s = randStr(NUM_CHARS, 20);
            emit("Long.decode", idx++, s, longDecode(s));
        }

        int[] radixes = {2, 8, 10, 16, 36, 3, 20, 1, 37, 0};
        idx = 0;
        for (int i = 0; i < 800; i++) {
            String s = randStr(NUM_CHARS, 20);
            int r = radixes[R.nextInt(radixes.length)];
            emit("Long.parseLong-radix", idx++, s + "|radix=" + r, longParse(s, r));
        }
    }

    static String longParse(String s, int radix) {
        try { return "ok=" + Long.parseLong(s, radix); }
        catch (Exception e) { return "EXC=" + e.getClass().getSimpleName(); }
    }

    static String longDecode(String s) {
        try { return "ok=" + Long.decode(s); }
        catch (Exception e) { return "EXC=" + e.getClass().getSimpleName(); }
    }

    // ------------------------------------------------------------ Double

    static void fuzzDouble() {
        String[] seeds = {
            "0", "-0", "0.0", "-0.0", "1e309", "-1e309", "1e-324", "4.9e-324", "NaN",
            "-NaN", "Infinity", "-Infinity", "0x1.0p0", "0x1.8p1", "0x1p-1022", "0x1p1024",
            "1f", "1d", "1.0f", ".5", "5.", "1_0.0", " 1.0", "1.0 ", "\n", "\t1.0", "٠.٥",
            "१.५", "1,000", "1e", "1e+", "1e-", "e1", ".", "+", "-", "", " ", "0x", "0x1p",
            "0x1.p0", "0xp0", "0x.p1", "0x1.0p", "0x1.8p1f", "NaNf", "Infinityd", "0x1.8p1d",
            "0X1.8P1", "1e999999999", "1e-999999999", "0x1p-1074", "0x0.0000000000001p-1022",
            "\u0000", "  +1.0", "- 1.0"
        };
        int idx = 0;
        for (String s : seeds) emit("Double.parseDouble", idx++, s, doubleParse(s));
        for (int i = 0; i < 2000; i++) {
            String s = randStr(NUM_CHARS, 28);
            emit("Double.parseDouble", idx++, s, doubleParse(s));
        }

        idx = 0;
        for (String s : seeds) emit("Double.valueOf", idx++, s, doubleValueOf(s));
        for (int i = 0; i < 1200; i++) {
            String s = randStr(NUM_CHARS, 24);
            emit("Double.valueOf", idx++, s, doubleValueOf(s));
        }
    }

    static String doubleParse(String s) {
        try { return "ok=" + Long.toHexString(Double.doubleToLongBits(Double.parseDouble(s))); }
        catch (Exception e) { return "EXC=" + e.getClass().getSimpleName(); }
    }

    static String doubleValueOf(String s) {
        try { return "ok=" + Long.toHexString(Double.doubleToLongBits(Double.valueOf(s))); }
        catch (Exception e) { return "EXC=" + e.getClass().getSimpleName(); }
    }

    // ------------------------------------------------------------ Float

    static void fuzzFloat() {
        String[] seeds = {
            "0", "-0", "0.0", "-0.0", "1e39", "1e-46", "NaN", "Infinity", "-Infinity",
            "1f", "1.0f", "0x1.0p0", "0x1.8p1", ".5", "5.", " 1.0", "1.0 ", "", " ",
            "3.4028235e38", "1.4e-45", "1e", "1e+", "1e-", "0x1p-149", "0x1p128"
        };
        int idx = 0;
        for (String s : seeds) emit("Float.parseFloat", idx++, s, floatParse(s));
        for (int i = 0; i < 500; i++) {
            String s = randStr(NUM_CHARS, 20);
            emit("Float.parseFloat", idx++, s, floatParse(s));
        }
    }

    static String floatParse(String s) {
        try { return "ok=" + Integer.toHexString(Float.floatToIntBits(Float.parseFloat(s))); }
        catch (Exception e) { return "EXC=" + e.getClass().getSimpleName(); }
    }

    // ------------------------------------------------------------ Regex

    static void fuzzRegex() {
        String[] seeds = {
            "a", "a*", "a+", "a?", "a|b", "(a|b)c", "(a)(b)", "(?<name>a)", "\\d+", "\\w+",
            "\\s+", "[a-z]+", "[^a]+", "a{2,4}", "a{2,}", "a{2}", "a{0}", ".*", ".+", "^a$",
            "\\bword\\b", "\\B", "\\A", "\\z", "\\Z", "(?i)abc", "abc(?i)", "(?u)abc", "(?s).*",
            "(?m)^a", "(?x)a b c #comment\n", "\\p{Lower}+", "\\p{javaLowerCase}+", "\\p{Lu}",
            "\\P{Lu}", "[\\p{L}&&[^a]]", "\\R", "\\h+", "\\v+", "\\X", "\\p{IsLatin}+",
            "\\p{script=Latin}+", "\\x{1F600}", "\\u0061", "\\cA", "[a\\-z]", "[]]", "[^]]",
            "[-a]", "[a-]", "[a-b-c]", "[\\d&&\\w]", "(a)\\1", "(a)\\2", "(?:(a)|(b))\\1",
            "(?>a|ab)b", "(?=a)a", "(?!a)b", "(?<=a)b", "(?<!a)b", "a??", "a*?", "a+?", "a{2,4}?",
            "[a-z&&[def]]", "(?#comment)a", "\\Q...\\E", "\\c", "\\", "[", "(", "(a", "a)",
            "*a", "a**", "{1,2}", "a{2,1}", "[z-a]", "\\p{NoSuch}", "(?<1>a)", "(?<a-b>c)",
            "a++", "a*+", "a?+", "(?i)İ", "(?iu)İ", "(?i)ı", "(?iu)ı",
            "(?i)ﬁ", "(?iu)ﬁ", "(?u)\\w", "(?U)\\w"
        };

        int idx = 0;
        for (String p : seeds) {
            emit("Pattern.compile", idx, p, patternCompile(p, 0));
            emit("Pattern.compile-ci", idx, p, patternCompile(p, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE));
            emit("Pattern.compile-uc", idx, p, patternCompile(p, Pattern.UNICODE_CHARACTER_CLASS));
            for (String s : SUBJECTS) {
                emit("Regex.match-seed", idx, "pat=" + p + "|subj=" + s, regexMatch(p, s));
            }
            idx++;
        }

        for (int i = 0; i < 600; i++) {
            String p = randRegex(18);
            emit("Pattern.compile-rand", i, p, patternCompile(p, 0));
            String s = SUBJECTS[R.nextInt(SUBJECTS.length)];
            emit("Regex.match-rand", i, "pat=" + p + "|subj=" + s, regexMatch(p, s));
        }
    }

    static String patternCompile(String p, int flags) {
        try { Pattern.compile(p, flags); return "ok"; }
        catch (Exception e) { return "EXC=" + e.getClass().getSimpleName(); }
    }

    static String regexMatch(String p, String s) {
        try {
            Pattern pat = Pattern.compile(p);
            Matcher m = pat.matcher(s);
            boolean found = m.find();
            StringBuilder sb = new StringBuilder();
            sb.append("find=").append(found);
            if (found) {
                sb.append("|start=").append(m.start()).append("|end=").append(m.end());
                sb.append("|gc=").append(m.groupCount());
                sb.append("|g0=").append(m.group());
                for (int g = 1; g <= m.groupCount(); g++) {
                    sb.append("|g").append(g).append("=").append(m.group(g));
                }
            }
            Matcher m2 = pat.matcher(s);
            sb.append("|matches=").append(m2.matches());
            return sb.toString();
        } catch (Exception e) {
            return "EXC=" + e.getClass().getSimpleName();
        }
    }

    // -------------------------------------------------------- NumberFormat

    static void fuzzNumberFormat() {
        Locale loc = Locale.US;
        NumberFormat[] fmts = {
            NumberFormat.getNumberInstance(loc),
            NumberFormat.getIntegerInstance(loc),
            NumberFormat.getCurrencyInstance(loc),
            NumberFormat.getPercentInstance(loc)
        };
        String[] names = {"number", "integer", "currency", "percent"};

        int idx = 0;
        for (int i = 0; i < 600; i++) {
            String s = randStr(NUM_CHARS, 20);
            for (int f = 0; f < fmts.length; f++) {
                emit("NumberFormat.parse", idx++, names[f] + "|in=" + s, nfParse(fmts[f], s));
            }
        }

        double[] vals = {
            0, -0.0, 1, -1, 1234.5, 0.5, 123456789.123456, Double.NaN,
            Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, 2.5, 3.5,
            0.0001, 1e20, -1e-10, 42, -42, 0.99999999
        };
        for (int f = 0; f < fmts.length; f++) {
            for (int v = 0; v < vals.length; v++) {
                emit("NumberFormat.format", idx++, names[f] + "|v=" + vals[v], nfFormat(fmts[f], vals[v]));
            }
        }

        for (int i = 0; i < 400; i++) {
            long bits = R.nextLong();
            double d = Double.longBitsToDouble(bits);
            int f = R.nextInt(fmts.length);
            emit("NumberFormat.format-rand", i, names[f] + "|bits=" + Long.toHexString(bits), nfFormat(fmts[f], d));
        }
    }

    static String numNorm(Number n) {
        if (n instanceof Long) return "L:" + n.longValue();
        if (n instanceof Integer || n instanceof Short || n instanceof Byte) return "I:" + n.intValue();
        if (n instanceof Double) return "D:" + Long.toHexString(Double.doubleToLongBits(n.doubleValue()));
        if (n instanceof BigDecimal) return "BD:" + n.toString();
        return n.getClass().getSimpleName() + ":" + n.toString();
    }

    static String nfParse(NumberFormat fmt, String s) {
        String plain;
        try { plain = "ok=" + numNorm(fmt.parse(s)); }
        catch (Exception e) { plain = "EXC=" + e.getClass().getSimpleName(); }
        ParsePosition pp = new ParsePosition(0);
        Number n = fmt.parse(s, pp);
        String ppv = (n == null) ? "null" : numNorm(n);
        return plain + "|pp=" + ppv + "@" + pp.getIndex();
    }

    static String nfFormat(NumberFormat fmt, double v) {
        try { return "ok=" + fmt.format(v); }
        catch (Exception e) { return "EXC=" + e.getClass().getSimpleName(); }
    }

    // -------------------------------------------------------- DecimalFormat

    static void fuzzDecimalFormat() {
        String[] patterns = {
            "0", "0.00", "#,##0.###", "0.##E0", "#,##0.00;(#,##0.00)", "00000", "0%",
            "¤#,##0.00", "#.###", "0.###E0", "###.#", "0.0#####", "0E0"
        };
        int idx = 0;
        for (String pat : patterns) {
            DecimalFormat df = new DecimalFormat(pat);
            for (String s : DEC_PARSE_SEEDS) {
                emit("DecimalFormat.parse", idx++, "pat=" + pat + "|in=" + s, dfParse(df, s));
            }
            for (int i = 0; i < 80; i++) {
                String s = randStr(NUM_CHARS, 18);
                emit("DecimalFormat.parse-rand", idx++, "pat=" + pat + "|in=" + s, dfParse(df, s));
            }
            double[] vals = {
                0, 1, -1, 1234.5, 1234567.89, 0.5, 0.0001, 1e20, Double.NaN,
                Double.POSITIVE_INFINITY, 2.5, 3.5, 42, -0.0
            };
            for (double v : vals) {
                emit("DecimalFormat.format", idx++, "pat=" + pat + "|v=" + v, dfFormat(df, v));
            }
        }
    }

    static String dfParse(DecimalFormat df, String s) {
        String plain;
        try { plain = "ok=" + numNorm(df.parse(s)); }
        catch (Exception e) { plain = "EXC=" + e.getClass().getSimpleName(); }
        ParsePosition pp = new ParsePosition(0);
        Number n = df.parse(s, pp);
        String ppv = (n == null) ? "null" : numNorm(n);
        return plain + "|pp=" + ppv + "@" + pp.getIndex();
    }

    static String dfFormat(DecimalFormat df, double v) {
        try { return "ok=" + df.format(v); }
        catch (Exception e) { return "EXC=" + e.getClass().getSimpleName(); }
    }

    // ---------------------------------------------------------------- URI

    static void fuzzURI() {
        String[] seeds = {
            "http://example.com", "http://example.com/", "http://example.com/path",
            "http://example.com/path?q=1#frag", "https://user:pass@host:8080/a/b?c=d#e",
            "ftp://ftp.example.com/file", "file:///etc/passwd", "file://localhost/etc/passwd",
            "mailto:user@example.com", "urn:isbn:096139210x", "//host/path", "/abs/path",
            "rel/path", "", " ", "http://", "http:///", "http://[::1]:8080/",
            "http://[2001:db8::1]/", "http://[::1", "http://[]", "http://host:-1",
            "http://host:99999", "http://host:80", "a:b", "a:", "http://example.com/%zz",
            "http://example.com/%20", "http://example.com/\\", "http://example.com/{}",
            "http://example.com/<>", "http://example.com/|", "http://example.com/^",
            "http://example.com/`", "x", "?", "#", "/", "a/b/c/../../d", "http://example.com/..",
            "http://example.com/./", "//", "http://中文.中国",
            "http://host with space/", "tel:+1-816-555-1212", "jar:http://example.com/x.jar!/a/b",
            "http://[v1.fe80::a]/", "http://[fe80::1%25eth0]/", "ws://example.com",
            "http://example.com:65535/", "http://example.com:65536/"
        };
        int idx = 0;
        for (String s : seeds) emit("URI", idx++, s, uriResult(s));
        for (int i = 0; i < 1500; i++) {
            String s = randStr(STR_CHARS, 40);
            emit("URI-rand", i, s, uriResult(s));
        }
    }

    static String uriResult(String s) {
        try {
            URI u = new URI(s);
            return "scheme=" + u.getScheme()
                 + "|host=" + u.getHost()
                 + "|port=" + u.getPort()
                 + "|path=" + u.getPath()
                 + "|query=" + u.getQuery()
                 + "|frag=" + u.getFragment()
                 + "|opaque=" + u.isOpaque()
                 + "|rawAuth=" + u.getRawAuthority();
        } catch (Exception e) {
            if (e instanceof URISyntaxException) {
                URISyntaxException use = (URISyntaxException) e;
                return "EXC=URISyntaxException|reason=" + use.getReason() + "|idx=" + use.getIndex();
            }
            return "EXC=" + e.getClass().getSimpleName();
        }
    }

    // ---------------------------------------------------------------- URL

    static void fuzzURL() {
        String[] seeds = {
            "http://example.com", "https://example.com:8443/a/b?c=d#e", "ftp://ftp.example.com/file",
            "file:///etc/passwd", "file://localhost/etc/passwd", "file://server/share",
            "jar:file:///x.jar!/a", "http://[::1]:8080/", "http://[2001:db8::1]/",
            "http://user:pass@host/", "http://host:80/", "http://host:-1/", "http://host:99999/",
            "http://host:0/", "mailto:user@example.com", "http://", "http:///path", "http:path",
            "a:b", "unknownproto://x", "http://example.com:8080", "//host/path",
            "http://example.com/a b", "http://example.com/%", "http://example.com/%zz",
            "file:////x", "http://example.com#frag", "http://example.com?q=1",
            "http://example.com.", "http://.example.com", "http://example..com",
            "http://-bad.com", "http://exa mple.com", "http://host/path/"
        };
        int idx = 0;
        for (String s : seeds) emit("URL", idx++, s, urlResult(s));
        for (int i = 0; i < 1500; i++) {
            String s = randStr(STR_CHARS, 40);
            emit("URL-rand", i, s, urlResult(s));
        }
    }

    static String urlResult(String s) {
        try {
            URL u = new URL(s);
            return "proto=" + u.getProtocol()
                 + "|host=" + u.getHost()
                 + "|port=" + u.getPort()
                 + "|path=" + u.getPath()
                 + "|query=" + u.getQuery()
                 + "|ref=" + u.getRef()
                 + "|file=" + u.getFile()
                 + "|defPort=" + u.getDefaultPort();
        } catch (Exception e) {
            return "EXC=" + e.getClass().getSimpleName();
        }
    }

    // ------------------------------------------------------------ Properties

    static void fuzzProperties() {
        String[] seeds = {
            "a=b", "a = b", "a: b", "a=b\nc=d", "# comment\na=b", "! comment\na=b", "a=b\n",
            "a=b\\", "key=\\u0041", "key=\\u12", "key=\\uZZZZ", "key=\\ux", "key=\\u",
            "key=\\u123", "key=\\u12345", " a = b", "a = b ", "a  =  b", "a\\ b=c", "a\\=b=c",
            "a\\:b=c", "a=\\ b", "a=b\\\nc=d", "a=b\\\n c=d", "a=b\\\n\\ c=d", "=b", "a=", "=",
            "", "\n\n\n", "a=b\r\nc=d", "a=b\r", "a=b\u0000c", "\u0000=b", "a\u0000=b",
            "key=value with \\u0000", "a=1 b=2", "a=1\\tb", "\\u4e2d\\u6587=value", "a=\\u4e2d",
            "space key = v", "a = b = c", "a=#notcomment", "a#b=c", "a=\\q", "a=b\\q",
            "key = value with trailing backslash \\", "a=b\n\n\n", "\t a=b"
        };
        int idx = 0;
        for (String s : seeds) {
            emit("Properties.load-reader", idx, s, propsReader(s));
            emit("Properties.load-stream", idx, s, propsStream(s));
            idx++;
        }
        for (int i = 0; i < 1500; i++) {
            String s = randStr(PROP_CHARS, 40);
            emit("Properties.load-reader-rand", i, s, propsReader(s));
            emit("Properties.load-stream-rand", i, s, propsStream(s));
        }
    }

    static String propsSorted(Properties p) {
        List<String> keys = new ArrayList<String>(p.stringPropertyNames());
        Collections.sort(keys);
        StringBuilder sb = new StringBuilder("ok");
        for (String k : keys) {
            sb.append("|").append(k).append("=").append(p.getProperty(k));
        }
        return sb.toString();
    }

    static String propsReader(String content) {
        try {
            Properties p = new Properties();
            p.load(new StringReader(content));
            return propsSorted(p);
        } catch (Exception e) {
            return "EXC=" + e.getClass().getSimpleName();
        }
    }

    static String propsStream(String content) {
        try {
            Properties p = new Properties();
            p.load(new ByteArrayInputStream(content.getBytes("ISO-8859-1")));
            return propsSorted(p);
        } catch (Exception e) {
            return "EXC=" + e.getClass().getSimpleName();
        }
    }
}
