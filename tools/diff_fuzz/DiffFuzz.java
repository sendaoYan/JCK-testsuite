import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Random;
import java.util.TimeZone;
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
        "a++", "a*+", "a?+", "(?<1>a)",
        // deeper constructions: named backrefs, scripts, flags toggling,
        // class ops, code points, octal, inline flags, conditionals
        "(?<name>a)\\k<name>", "\\k<name>", "(?<name>a)\\k<nope>", "(?<a>a)(?<b>b)\\k<a>",
        "\\p{InGreek}", "\\p{sc=Latin}", "\\p{Script=Greek}", "\\p{script=Greek}",
        "\\p{gc=Lu}", "\\p{IsLu}", "\\p{InBasic_Latin}", "\\p{IsBasicLatin}",
        "(?i)a(?-i)b", "(?i:a)", "(?-i:a)", "[a-z&&[^m-p]]", "[[a-z]&&[^aeiou]]",
        "[[a][b]]", "[a[b]]", "[a&&b]", "\\N{LATIN SMALL LETTER A}", "a{0}", "a{0,0}",
        "a{2147483647}", "a{2147483648}", "(?(1)a|b)", "(?<n1>a)(?<n1>b)",
        "(?<1>a)\\1", "(?<1>a)\\k<1>", "[\\d]", "[.]", "[\\s\\S]", "[\\w\\W]", "[^\\D]",
        "[\\p{L}\\p{N}]", "\\0", "\\07", "\\012", "\\0x", "\\c@", "\\c_", "\\x{41}",
        "\\x{1F600}", "\\x{110000}", "\\x{}", "\\u{41}", "(?x)a b", "(?x)a#c\n",
        "\\Q[a-z]\\E", "\\Qabc\\E+", "(?U)\\w", "(?d)\\w", "\\P{Lu}", "\\p{javaUpperCase}"
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
        fuzzSimpleDateFormat();
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
            "(?i)ﬁ", "(?iu)ﬁ", "(?u)\\w", "(?U)\\w",
            // named-group backrefs and duplicate names
            "(?<name>a)\\k<name>", "(?<name>a)(?<name>b)", "\\k<name>",
            "(?<a>a)(?<b>b)\\k<a>", "(?<n>a)\\k<n>", "(?<n>a)\\1",
            // lookaround nesting / combinations
            "(?=(?=a))a", "(?<=a)b|c", "(?<!a)b", "(?<=a)(?=b)", "(?!a)(?<!a)a",
            // quantifier edge cases
            "a{0}", "a{0,0}", "a{1}", "a{1,}", "a{,2}", "a{1,2}", "a{2,2}", "a{2,3}?",
            "a{2,2147483647}", "a{2147483648}", "(?:a{2}){3}", "a{2}?", "(a*)+", "(a|a)*",
            // character classes
            "[[a-z]&&[^aeiou]]+", "[a-z&&[^m-p]]", "[[a][b]]", "[a[b]]", "[a&&b]", "[a&&a]",
            "[]a]", "[^]", "[-]", "[--a]", "[a--b]", "[\\d\\s]", "[\\w-]+", "[\\p{L}\\p{N}]+",
            "\\p{Lu}+", "\\p{Ll}", "\\p{Lt}", "\\p{Nd}", "\\p{P}", "\\p{S}", "\\p{IsGreek}",
            "\\p{InGreek}", "\\p{sc=Latin}+", "\\p{Script=Greek}", "\\p{gc=Lu}", "\\p{IsLu}",
            "\\p{IsBasicLatin}", "\\p{javaLowerCase}+", "\\p{javaUpperCase}+",
            // code points / octal / hex
            "\\x{1F600}", "\\x{41}", "\\x{110000}", "\\x{}", "\\x41", "\\u0041", "\\u{41}",
            "\\0", "\\07", "\\012", "\\0x", "\\cA", "\\c@", "\\c_", "\\c",
            // flags / inline flags / quoting
            "(?i)a(?-i)b", "(?i:a)", "(?-i:a)", "(?i)(?-i)a", "(?d)\\w", "(?id)ab",
            "(?u)a(?-u)b", "\\Q[a-z]+\\E", "\\Qabc\\E+", "(?x)a b c", "(?x)a # c\nb",
            // conditionals (not supported -> stable PSE) and unsupported \N
            "(?(1)a|b)", "\\N{LATIN SMALL LETTER A}", "\\N{a}",
            // possessive / atomic
            "a++a", "a*+a", "a?+a", "(?>a|ab)b", "(?>a+)a", "(a|ab)c", "\\b\\w++\\b"
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

    // ------------------------------------------------------ SimpleDateFormat

    static final TimeZone SDF_TZ_GMT = TimeZone.getTimeZone("GMT");
    static final TimeZone SDF_TZ_NY  = TimeZone.getTimeZone("America/New_York");
    static final TimeZone SDF_TZ_SH  = TimeZone.getTimeZone("Asia/Shanghai");

    // Calendar-built dates are computed in UTC (no DST, no wall clock) so the
    // millis are identical on both JDKs; only display (pattern/zone) can differ.
    static long sdfCalMillis(int y, int mo, int d, int h, int mi, int s, int ms) {
        GregorianCalendar c = new GregorianCalendar(SDF_TZ_GMT, Locale.US);
        c.setGregorianChange(new Date(Long.MIN_VALUE)); // pure proleptic Gregorian
        c.clear();
        c.set(Calendar.YEAR, y);
        c.set(Calendar.MONTH, mo);
        c.set(Calendar.DAY_OF_MONTH, d);
        c.set(Calendar.HOUR_OF_DAY, h);
        c.set(Calendar.MINUTE, mi);
        c.set(Calendar.SECOND, s);
        c.set(Calendar.MILLISECOND, ms);
        return c.getTimeInMillis();
    }

    static final String[] SDF_PATTERNS = {
        "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm:ss.SSS",
        "yyyy-MM-dd HH:mm:ss Z", "yyyy-MM-dd HH:mm:ss XXX", "yyyy-MM-dd HH:mm:ss z",
        "EEE, dd MMM yyyy HH:mm:ss zzz", "yyyyMMddHHmmss", "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        "dd/MM/yyyy", "MM/dd/yyyy", "yyyy G", "yyyy-MM-dd HH:mm:ss.SSS Z",
        "yyyyy-MM-dd", "yy-MM-dd", "Y-MM-dd", "YYYY-MM-dd", "u-MM-dd",
        "w W D F E", "a h:mm K", "k:mm", "S", "X", "XX", "XXX", "Z", "zzz", "z",
        "yyyy-MM-dd HH:mm:ss.SSSS", "yyyy-MM-dd HH:mm:ss.SSSSSS"
    };

    static final String[] SDF_PARSE_PATTERNS = {
        "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm:ss.SSS",
        "yyyyMMdd", "yyyyMMddHHmmss", "dd/MM/yyyy", "MM/dd/yyyy", "yyyy", "yyyy-MM",
        "MM-dd", "yyyy-MM-dd HH", "yyyy-MM-dd HH:mm", "yyyyMMdd HHmmss",
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", "yyyy-MM-dd HH:mm:ss Z",
        "yyyy G", "yyyy-MM-dd G", "yyMMdd", "yy-MM-dd", "Y-MM-dd", "YYYY", "u-MM-dd",
        "yyyy-MM-dd hh:mm:ss a", "yyyy-MM-dd HH:mm:ss z", "yyyyMMddHHmmssSSS",
        "'year' yyyy 'month' MM", "yyyy-MM-dd''", "yyyy-MM-dd [literal]",
        "yyyy-MM-dd'T'HH:mm:ss.SSSSSS", "yyyy-MM-dd HH:mm:ss.SSSSSS",
        "", " ", "'", "''", "'yyyy", "yyyy'", "yyyy-MM-dd HH:mm:ss.SSSSSSS",
        "yyyyy-MM-dd", "yyyyy", "yyyy-MM-dd X", "yyyy-MM-dd XX", "yyyy-MM-dd XXX",
        "yyyy/MM/dd", "dd.MM.yyyy"
    };

    static final String[] SDF_PARSE_INPUTS = {
        "2024-01-15", "2024-1-1", "2024-01-15 10:20:30", "2024-01-15 10:20:30.123",
        "2024-13-40", "2024-00-00", "0000-00-00", "2024-02-30", "2024-02-29",
        "2023-02-29", "2024-04-31", "9999-12-31", "10000-01-01", "0001-01-01",
        "2024", "2024-01", "01-15", "15/01/2024", "01/15/2024", "2024/01/15",
        "", " ", "abc", "2024-01-15 ", " 2024-01-15", "2024-01-15x",
        "2024-01-15 10:20", "2024-01-15 10", "2024-01-15 10:20:30.123456",
        "2024-01-15 10:20:30.123456789", "2024-01-15T10:20:30.123+05:30",
        "2024-01-15 10:20:30 +0530", "2024-01-15 10:20:30 GMT",
        "24-01-15", "240115", "20240115", "20240115102030", "2024-01-15 10:20:30 AM",
        "2024-01-15 10:20:30 PM", "12:00:00", "10:20", "-2024-01-15", "+2024-01-15",
        "2024-01-15 25:00:00", "2024-01-15 23:60:00", "2024-01-15 23:59:60",
        "2024-01-15 10:20:30.123 AM", "AD 2024-01-15", "BC 0001-01-01",
        "31/12/2024", "12/31/2024", "01.01.2024", "2024-01-15 10:20:30 +00:00",
        "29/02/2024", "29/02/2023", "2024-01-15 10:20:30 GMT+05:30"
    };

    // Locale-data (CLDR vs COMPAT) and textual-symbol patterns: these expose the
    // JDK-9 default-locale-provider switch and unrelated timezone-name data.
    static final Locale[] SDF_LOCALES = {
        Locale.US, Locale.GERMANY, Locale.FRANCE, Locale.ITALY, Locale.JAPAN,
        Locale.CHINA, Locale.KOREA, new Locale("ru"), Locale.UK
    };
    static final String[] SDF_LOCALE_NAMES = {
        "US", "GERMANY", "FRANCE", "ITALY", "JAPAN", "CHINA", "KOREA", "ru", "UK"
    };
    static final TimeZone[] SDF_TEXT_ZONES = {
        SDF_TZ_NY, TimeZone.getTimeZone("Europe/London"), SDF_TZ_GMT,
        TimeZone.getTimeZone("Europe/Berlin")
    };
    static final String[] SDF_TEXT_ZONE_NAMES = { "NY", "London", "GMT", "Berlin" };
    static final String[] SDF_TEXT_PATTERNS = {
        "MMMM", "MMMMM", "MMM", "EEE", "EEEE", "a", "G", "zzzz", "zzz",
        "d MMMM yyyy", "EEEE, MMMM d, yyyy"
    };
    static final String[] SDF_LOC_PARSE_PATTERNS = {
        "MMM d yyyy", "d MMM yyyy", "MMMM d yyyy", "d MMMM yyyy", "EEE, MMM d, yyyy"
    };
    static final String[] SDF_LOC_PARSE_INPUTS = {
        "Jan 15 2024", "15 Jan 2024", "January 15 2024", "15 January 2024",
        "Mon, Jan 15, 2024", "Jan. 15 2024", "15 Jan. 2024", "janv. 15 2024",
        "15 janv. 2024", "15 janvier 2024", "gen 15 2024", "15 gen 2024",
        "15 gennaio 2024", "15. Januar 2024", "15. März 2024", "15 mars 2024"
    };

    static void fuzzSimpleDateFormat() {
        // ---- formatting: boundary millis x pattern x timezone
        String[] dates = {
            "epoch", "0",
            "epoch-1ms", "-1",
            "epoch+1ms", "1",
            "epoch-1s", "-1000",
            "epoch+1s", "1000",
            "epoch-1h", "-3600000",
            "epoch+1h", "3600000",
            "epoch-1d", "-86400000",
            "epoch+1d", "86400000",
            "y2038", "2147483647000",
            "y1901", "-2147483648000",
            "y0001", "-62135596800000",
            "y9999", "253402300799999",
            "y10000", "253402300800000",
            "leap-2000-02-29", Long.toString(sdfCalMillis(2000, 1, 29, 12, 0, 0, 0)),
            "leap-2004-02-29", Long.toString(sdfCalMillis(2004, 1, 29, 12, 0, 0, 0)),
            "nonleap-1900-02-28", Long.toString(sdfCalMillis(1900, 1, 28, 12, 0, 0, 0)),
            "nonleap-2100-02-28", Long.toString(sdfCalMillis(2100, 1, 28, 12, 0, 0, 0)),
            "y1582-10-04", Long.toString(sdfCalMillis(1582, 9, 4, 12, 0, 0, 0)),
            "y1582-10-15", Long.toString(sdfCalMillis(1582, 9, 15, 12, 0, 0, 0)),
            "ny-dst-spring", Long.toString(sdfCalMillis(2024, 2, 10, 7, 0, 0, 0)),
            "ny-dst-fall",   Long.toString(sdfCalMillis(2024, 10, 3, 6, 0, 0, 0)),
            "ny-pre-dst",    Long.toString(sdfCalMillis(2024, 2, 10, 6, 30, 0, 0)),
            "ny-post-dst",   Long.toString(sdfCalMillis(2024, 2, 10, 7, 30, 0, 0))
        };
        int idx = 0;
        for (int p = 0; p < SDF_PATTERNS.length; p++) {
            for (int d = 0; d < dates.length; d += 2) {
                for (int z = 0; z < 3; z++) {
                    String zoneName = z == 0 ? "GMT" : (z == 1 ? "NY" : "SH");
                    String input = "pat=" + SDF_PATTERNS[p] + "|date=" + dates[d]
                            + "|millis=" + dates[d + 1] + "|tz=" + zoneName;
                    emit("SDF.format", idx, input,
                         sdfFormat(SDF_PATTERNS[p], Long.parseLong(dates[d + 1]),
                                   z == 0 ? SDF_TZ_GMT : (z == 1 ? SDF_TZ_NY : SDF_TZ_SH)));
                    idx++;
                }
            }
        }

        // ---- parsing: pattern x input x {lenient, strict} x {parse, parsePosition}
        for (int p = 0; p < SDF_PARSE_PATTERNS.length; p++) {
            for (int i = 0; i < SDF_PARSE_INPUTS.length; i++) {
                for (int strict = 0; strict < 2; strict++) {
                    String mode = strict == 0 ? "lenient" : "strict";
                    String input = "pat=" + SDF_PARSE_PATTERNS[p] + "|in="
                            + SDF_PARSE_INPUTS[i] + "|" + mode;
                    emit("SDF.parse", idx, input,
                         sdfParse(SDF_PARSE_PATTERNS[p], SDF_PARSE_INPUTS[i], strict == 1));
                    idx++;
                }
            }
        }

        // ---- textual symbols across locales (CLDR vs COMPAT data)
        for (int p = 0; p < SDF_TEXT_PATTERNS.length; p++) {
            for (int l = 0; l < SDF_LOCALES.length; l++) {
                for (int d = 0; d < 3; d++) {
                    long m = (d == 0) ? 0L : (d == 1) ? 253402300799999L : -62135596800000L;
                    for (int z = 0; z < SDF_TEXT_ZONES.length; z++) {
                        String input = "pat=" + SDF_TEXT_PATTERNS[p] + "|loc="
                                + SDF_LOCALE_NAMES[l] + "|millis=" + m + "|tz="
                                + SDF_TEXT_ZONE_NAMES[z];
                        emit("SDF.format-text", idx, input,
                             sdfFormatLoc(SDF_TEXT_PATTERNS[p], SDF_LOCALES[l], m,
                                          SDF_TEXT_ZONES[z]));
                        idx++;
                    }
                }
            }
        }

        // ---- locale-sensitive parsing (CLDR abbreviated month names differ)
        for (int p = 0; p < SDF_LOC_PARSE_PATTERNS.length; p++) {
            for (int l = 0; l < SDF_LOCALES.length; l++) {
                for (int i = 0; i < SDF_LOC_PARSE_INPUTS.length; i++) {
                    for (int strict = 0; strict < 2; strict++) {
                        String mode = strict == 0 ? "lenient" : "strict";
                        String input = "pat=" + SDF_LOC_PARSE_PATTERNS[p] + "|loc="
                                + SDF_LOCALE_NAMES[l] + "|in=" + SDF_LOC_PARSE_INPUTS[i]
                                + "|" + mode;
                        emit("SDF.parse-loc", idx, input,
                             sdfParseLoc(SDF_LOC_PARSE_PATTERNS[p], SDF_LOCALES[l],
                                         SDF_LOC_PARSE_INPUTS[i], strict == 1));
                        idx++;
                    }
                }
            }
        }
    }

    static String sdfFormatLoc(String pattern, Locale loc, long millis, TimeZone tz) {
        try {
            SimpleDateFormat df = new SimpleDateFormat(pattern, loc);
            df.setTimeZone(tz);
            return "ok=" + df.format(new Date(millis));
        } catch (Exception e) {
            return "EXC=" + e.getClass().getSimpleName();
        }
    }

    static String sdfParseLoc(String pattern, Locale loc, String s, boolean strict) {
        SimpleDateFormat df;
        try {
            df = new SimpleDateFormat(pattern, loc);
        } catch (Exception e) {
            return "CONSTRUCT=EXC=" + e.getClass().getSimpleName();
        }
        df.setTimeZone(SDF_TZ_GMT);
        df.setLenient(!strict);
        String plain;
        try {
            Date d = df.parse(s);
            plain = "ok=" + (d == null ? "null" : d.getTime());
        } catch (Exception e) {
            plain = "EXC=" + e.getClass().getSimpleName();
        }
        ParsePosition pp = new ParsePosition(0);
        Date d2 = df.parse(s, pp);
        String ppv = (d2 == null) ? "null" : Long.toString(d2.getTime());
        return plain + "|pp=" + ppv + "@" + pp.getIndex() + "|errIdx=" + pp.getErrorIndex();
    }

    static String sdfFormat(String pattern, long millis, TimeZone tz) {
        try {
            SimpleDateFormat df = new SimpleDateFormat(pattern, Locale.US);
            df.setTimeZone(tz);
            Date d = new Date(millis);
            return "ok=" + df.format(d);
        } catch (Exception e) {
            return "EXC=" + e.getClass().getSimpleName();
        }
    }

    static String sdfParse(String pattern, String s, boolean strict) {
        SimpleDateFormat df;
        try {
            df = new SimpleDateFormat(pattern, Locale.US);
        } catch (Exception e) {
            return "CONSTRUCT=EXC=" + e.getClass().getSimpleName();
        }
        df.setTimeZone(SDF_TZ_GMT);
        df.setLenient(!strict);
        String plain;
        try {
            Date d = df.parse(s);
            plain = "ok=" + (d == null ? "null" : d.getTime());
        } catch (Exception e) {
            plain = "EXC=" + e.getClass().getSimpleName();
        }
        ParsePosition pp = new ParsePosition(0);
        Date d2 = df.parse(s, pp);
        String ppv = (d2 == null) ? "null" : Long.toString(d2.getTime());
        return plain + "|pp=" + ppv + "@" + pp.getIndex() + "|errIdx=" + pp.getErrorIndex();
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
