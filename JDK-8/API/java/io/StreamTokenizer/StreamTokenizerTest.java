/*
 * @test
 * @summary java.io.StreamTokenizer conformance: full contract coverage.
 *          Default grammar incl. commentChar('/') and both quote chars, quoted-string
 *          escapes incl. octal, number forms without exponent support, eolIsSignificant/
 *          lineno over \n \r \r\n, pushBack, lowerCaseMode, comment chars, ordinary/
 *          whitespace/word range configuration, resetSyntax, toString formats.
 * @run main StreamTokenizerTest
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;

public class StreamTokenizerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static StreamTokenizer st(String s) {
        return new StreamTokenizer(new StringReader(s));
    }

    static void testBehavior() throws Exception {
        StreamTokenizer t = st("hello world 42 -7 3.14");
        check(t.nextToken() == StreamTokenizer.TT_WORD && "hello".equals(t.sval), "word 1");
        check(t.nextToken() == StreamTokenizer.TT_WORD && "world".equals(t.sval), "word 2");
        check(t.nextToken() == StreamTokenizer.TT_NUMBER && t.nval == 42.0, "42");
        check(t.nextToken() == StreamTokenizer.TT_NUMBER && t.nval == -7.0, "negative");
        check(t.nextToken() == StreamTokenizer.TT_NUMBER && t.nval == 3.14, "decimal");
        check(t.nextToken() == StreamTokenizer.TT_EOF, "EOF");
        check(t.lineno() == 1, "single line");

        t = st("x/y\nz");
        check(t.nextToken() == StreamTokenizer.TT_WORD && "x".equals(t.sval), "x before slash");
        check(t.nextToken() == StreamTokenizer.TT_WORD && "z".equals(t.sval), "rest of line dropped");
        check(t.nextToken() == StreamTokenizer.TT_EOF, "EOF");

        t = st("\"hi there\" \"esc\\\"q\" \"tab\\there\" \"back\\\\slash\" \"oct\\101l\"");
        check(t.nextToken() == '"' && "hi there".equals(t.sval), "quoted phrase");
        check(t.nextToken() == '"' && "esc\"q".equals(t.sval), "escaped quote");
        check(t.nextToken() == '"' && "tab\there".equals(t.sval), "escaped tab");
        check(t.nextToken() == '"' && "back\\slash".equals(t.sval), "escaped backslash");
        check(t.nextToken() == '"' && "octAl".equals(t.sval), "octal \\101 -> A");
        check(t.nextToken() == StreamTokenizer.TT_EOF, "EOF after quotes");

        t = st("\"abc");
        check(t.nextToken() == '"' && "abc".equals(t.sval), "unterminated quote body");
        check(t.nextToken() == StreamTokenizer.TT_EOF, "EOF follows");

        t = st("'x' \"y\"");
        t.ordinaryChar('\'');
        check(t.nextToken() == '\'', "ordinary single quote");
        check(t.nextToken() == StreamTokenizer.TT_WORD && "x".equals(t.sval), "word x");
        check(t.nextToken() == '\'', "closing ordinary quote");
        check(t.nextToken() == '"' && "y".equals(t.sval), "double quote still quoted");
        check(t.nextToken() == StreamTokenizer.TT_EOF, "EOF");

        t = st(".5 5. 007 1e3");
        check(t.nextToken() == StreamTokenizer.TT_NUMBER && t.nval == 0.5, ".5");
        check(t.nextToken() == StreamTokenizer.TT_NUMBER && t.nval == 5.0, "5.");
        check(t.nextToken() == StreamTokenizer.TT_NUMBER && t.nval == 7.0, "007");
        check(t.nextToken() == StreamTokenizer.TT_NUMBER && t.nval == 1.0, "no exponent: 1");
        check(t.nextToken() == StreamTokenizer.TT_WORD && "e3".equals(t.sval), "e3 stays a word");

        t = st("a\nb\r\nc\rd");
        t.eolIsSignificant(true);
        check(t.nextToken() == StreamTokenizer.TT_WORD, "a");
        check(t.nextToken() == StreamTokenizer.TT_EOL && t.lineno() == 2, "EOL after a increments");
        check(t.nextToken() == StreamTokenizer.TT_WORD && t.lineno() == 2, "b on line 2");
        check(t.nextToken() == StreamTokenizer.TT_EOL && t.lineno() == 3, "\\r\\n = one EOL");
        check(t.nextToken() == StreamTokenizer.TT_WORD && t.lineno() == 3, "c on line 3");
        check(t.nextToken() == StreamTokenizer.TT_EOL && t.lineno() == 4, "\\r is an EOL");
        check(t.nextToken() == StreamTokenizer.TT_WORD && t.lineno() == 4, "d on line 4");
        check(t.nextToken() == StreamTokenizer.TT_EOF, "EOF");

        t = st("a\nb");
        check(t.nextToken() == StreamTokenizer.TT_WORD && "a".equals(t.sval), "a");
        check(t.nextToken() == StreamTokenizer.TT_WORD && "b".equals(t.sval) && t.lineno() == 2,
              "newline skipped silently, line still counted");

        t = st("a // dropped\nb /* block\nstill comment */ c");
        t.slashSlashComments(true);
        t.slashSlashComments(false);
        t.slashSlashComments(true);                        // last call wins
        t.slashStarComments(true);
        check(t.nextToken() == StreamTokenizer.TT_WORD && "a".equals(t.sval), "a before //");
        check(t.nextToken() == StreamTokenizer.TT_WORD && "b".equals(t.sval),
              "line comment ended at newline");
        check(t.nextToken() == StreamTokenizer.TT_WORD && "c".equals(t.sval),
              "multi-line block comment spanned lines");
        check(t.nextToken() == StreamTokenizer.TT_EOF, "EOF");
        check(t.lineno() >= 3, "block comment advanced the line number");

        t = st("keep#gone\nnext");
        t.commentChar('#');
        check(t.nextToken() == StreamTokenizer.TT_WORD && "keep".equals(t.sval), "before #");
        check(t.nextToken() == StreamTokenizer.TT_WORD && "next".equals(t.sval)
                  && t.lineno() == 2, "comment ended at the newline");

        t = st("MiXeD \"UnTouChEd\"");
        t.lowerCaseMode(true);
        check(t.nextToken() == StreamTokenizer.TT_WORD && "mixed".equals(t.sval), "word lowercased");
        check(t.nextToken() == '"' && "UnTouChEd".equals(t.sval), "quote untouched");
        t.lowerCaseMode(false);
        check(t.nextToken() == StreamTokenizer.TT_EOF, "EOF");

        t = st("one two");
        check(t.nextToken() == StreamTokenizer.TT_WORD && "one".equals(t.sval), "first token");
        t.pushBack();
        check(t.nextToken() == StreamTokenizer.TT_WORD && "one".equals(t.sval), "token repeated");
        check(t.nextToken() == StreamTokenizer.TT_WORD && "two".equals(t.sval), "then the next");

        t = st("a_1b");
        t.wordChars('0', '9');
        t.wordChars('_', '_');
        check(t.nextToken() == StreamTokenizer.TT_WORD && "a_1b".equals(t.sval), "custom word chars");
        t = st("xbcy");
        t.whitespaceChars('b', 'c');
        check(t.nextToken() == StreamTokenizer.TT_WORD && "x".equals(t.sval), "split at b");
        check(t.nextToken() == StreamTokenizer.TT_WORD && "y".equals(t.sval), "split at c");

        t = st("42x");
        t.ordinaryChars('0', '9');
        check(t.nextToken() == '4' && t.nextToken() == '2', "digits became ordinary chars");
        check(t.nextToken() == StreamTokenizer.TT_WORD && "x".equals(t.sval), "x stays a word");
        t = st("42x");
        t.ordinaryChars('0', '9');
        t.parseNumbers();
        check(t.nextToken() == StreamTokenizer.TT_NUMBER && t.nval == 42.0,
              "parseNumbers reclaims digits");

        t = st("a 1");
        t.resetSyntax();
        check(t.nextToken() == 'a' && t.nextToken() == ' ' && t.nextToken() == '1',
              "all characters ordinary");

        t = st("x 3 \"q\" + \n");
        t.eolIsSignificant(true);
        t.nextToken();
        check("Token[x], line 1".equals(t.toString()), "word toString");
        t.nextToken();
        check("Token[n=3.0], line 1".equals(t.toString()), "number toString");
        t.nextToken();
        check("Token[q], line 1".equals(t.toString()), "quote toString");
        t.nextToken();
        check("Token['+'], line 1".equals(t.toString()), "ordinary toString");
        t.nextToken();
        check(t.ttype == StreamTokenizer.TT_EOL, "EOL token");
        t.nextToken();
        check("Token[EOF], line 2".equals(t.toString()), "eof toString");

        t = st("");
        check(t.nextToken() == StreamTokenizer.TT_EOF && t.lineno() == 1, "empty input");
    }

    // ================= 深化追加（契约级） =================

    // ---- initial state: TT_NOTHING (-4) before the first nextToken (实测) ----
    static void testInitialState() throws Exception {
        StreamTokenizer t = st("z");
        check(t.ttype == -4, "fresh tokenizer holds TT_NOTHING (-4), not EOF");
        check(t.sval == null, "fresh sval is null");
        check(t.nval == 0.0, "fresh nval is 0.0");
        check(t.lineno() == 1, "fresh lineno is 1");
        check("Token[NOTHING], line 1".equals(t.toString()),
              "fresh toString names NOTHING: " + t.toString());
        check(StreamTokenizer.TT_EOF == -1 && StreamTokenizer.TT_EOL == '\n'
                  && StreamTokenizer.TT_NUMBER == -2 && StreamTokenizer.TT_WORD == -3,
              "the four TT_ constants are -1/'\\n'/-2/-3");
        // pushBack before the first call changes nothing: the first token still reads once
        t = st("q");
        t.pushBack();
        check(t.nextToken() == StreamTokenizer.TT_WORD && "q".equals(t.sval),
              "pushBack before first call is harmless");
        check(t.nextToken() == StreamTokenizer.TT_EOF, "EOF after the single token");
    }

    // ---- pushBack is a one-deep flag, not a stack (实测) ----
    static void testPushBackSemantics() throws Exception {
        // 2x and 3x pushBack still replay the token exactly once
        StreamTokenizer t = st("a b");
        check(t.nextToken() == StreamTokenizer.TT_WORD && "a".equals(t.sval), "read a");
        t.pushBack();
        t.pushBack();
        check(t.nextToken() == StreamTokenizer.TT_WORD && "a".equals(t.sval),
              "double pushBack replays once");
        check(t.nextToken() == StreamTokenizer.TT_WORD && "b".equals(t.sval),
              "then advances to b");
        t.pushBack();
        t.pushBack();
        t.pushBack();
        check(t.nextToken() == StreamTokenizer.TT_WORD && "b".equals(t.sval),
              "triple pushBack also replays once");
        check(t.nextToken() == StreamTokenizer.TT_EOF, "then EOF");
        // at EOF: pushBack makes EOF repeat indefinitely
        t.pushBack();
        check(t.nextToken() == StreamTokenizer.TT_EOF, "pushBack at EOF replays EOF");
        t.pushBack();
        check(t.nextToken() == StreamTokenizer.TT_EOF, "and again");
        // EOL tokens replay too
        t = st("x\ny");
        t.eolIsSignificant(true);
        check(t.nextToken() == StreamTokenizer.TT_WORD, "x");
        check(t.nextToken() == StreamTokenizer.TT_EOL, "EOL");
        t.pushBack();
        check(t.nextToken() == StreamTokenizer.TT_EOL, "EOL replays");
        // quoted tokens replay with sval intact
        t = st("\"w\"");
        check(t.nextToken() == '"' && "w".equals(t.sval), "quoted token");
        t.pushBack();
        check(t.nextToken() == '"' && "w".equals(t.sval), "quoted token replays with sval");
        // after pushBack, toString still describes the pushed token
        t = st("A");
        t.nextToken();
        t.pushBack();
        check("Token[A], line 1".equals(t.toString()), "toString after pushBack");
    }

    // ---- quote scanning: \n and \r close an unterminated quote (实测) ----
    static void testQuoteCloseRules() throws Exception {
        StreamTokenizer t = st("\"ab\ncd\"");
        check(t.nextToken() == '"' && "ab".equals(t.sval),
              "newline closes the quote; only the prefix is kept");
        check(t.nextToken() == StreamTokenizer.TT_WORD && "cd".equals(t.sval),
              "scanning resumes as words on the next line");
        t = st("\"ab\rcd\"");
        check(t.nextToken() == '"' && "ab".equals(t.sval),
              "carriage return also closes the quote");
        check(t.nextToken() == StreamTokenizer.TT_WORD && "cd".equals(t.sval) && t.lineno() == 2,
              "CR advanced the line");
        // empty quoted string yields the empty sval, distinct from null
        t = st("\"\" x");
        check(t.nextToken() == '"' && t.sval != null && t.sval.isEmpty(),
              "empty quotes produce an empty sval (not null)");
        check(t.nextToken() == StreamTokenizer.TT_WORD && "x".equals(t.sval), "next token");
        // a lone quote char at EOF yields ttype=quote with empty sval
        t = st("\"");
        check(t.nextToken() == '"' && t.sval != null && t.sval.isEmpty(),
              "lone quote at EOF gives an empty body");
        check(t.nextToken() == StreamTokenizer.TT_EOF, "EOF follows");
    }

    // ---- escape matrix: C escapes, octal digits, overflow truncation (实测) ----
    static void testEscapeMatrix() throws Exception {
        StreamTokenizer t = st("\"\\a\\v\\f\\b\\r\\n\\t\"");
        check(t.nextToken() == '"' && t.sval.length() == 7, "seven escapes decoded");
        int[] expect = {7, 11, 12, 8, 13, 10, 9};
        boolean all = true;
        for (int i = 0; i < 7; i++) all &= t.sval.charAt(i) == expect[i];
        check(all, "\\a=7 \\v=11 \\f=12 \\b=8 \\r=13 \\n=10 \\t=9");
        // octal: up to three digits, each digit must be 0-7
        t = st("\"\\1\\12\\123\"");
        t.nextToken();
        check(t.sval.charAt(0) == 1 && t.sval.charAt(1) == 10 && t.sval.charAt(2) == 83,
              "\\1=1, \\12=10, \\123=83");
        // a fourth digit or an 8/9 stops the escape early: \128 = \12 + '8'
        t = st("\"\\128\"");
        t.nextToken();
        check(t.sval.length() == 2 && t.sval.charAt(0) == 10 && t.sval.charAt(1) == '8',
              "\\128 splits into \\12 and literal 8");
        // \0 is the empty octal escape: a NUL
        t = st("\"\\0\"");
        t.nextToken();
        check(t.sval.length() == 1 && t.sval.charAt(0) == 0, "\\0 encodes NUL");
        // \777 exceeds 255: only the first two digits are consumed (63), 7 stays literal
        t = st("\"\\77\\777\"");
        t.nextToken();
        check(t.sval.length() == 3 && t.sval.charAt(0) == 63 && t.sval.charAt(1) == 63
                  && t.sval.charAt(2) == 55,
              "\\77=63; \\777 overflows into \\77 + '7'");
    }

    // ---- characters >= 0x100 are word chars by constitution (实测) ----
    static void testHighChars() throws Exception {
        StreamTokenizer t = st("\u0100\u1234zz");
        check(t.nextToken() == StreamTokenizer.TT_WORD && "\u0100\u1234zz".equals(t.sval),
              "supplementary-range chars join words with ASCII letters");
        // resetSyntax cannot make them ordinary: the syntax table only covers 0..255
        t = st("\u0100");
        t.resetSyntax();
        check(t.nextToken() == StreamTokenizer.TT_WORD && "\u0100".equals(t.sval),
              "chars >= 0x100 stay words even after resetSyntax");
        // out-of-range wordChars calls are clamped to no-ops
        t = st("\u0100b");
        t.wordChars(-5, -1);
        check(t.nextToken() == StreamTokenizer.TT_WORD && "\u0100b".equals(t.sval),
              "wordChars below 0 is a no-op");
        t = st("\u0100b");
        t.wordChars(300, 400);
        check(t.nextToken() == StreamTokenizer.TT_WORD && "\u0100b".equals(t.sval),
              "wordChars above 255 is a no-op");
        t = st("\u0500b");
        t.wordChars(0, 1000);
        check(t.nextToken() == StreamTokenizer.TT_WORD && "\u0500b".equals(t.sval),
              "wordChars(0,1000) clamps to 0..255, hi char still a word");
    }

    // ---- resetSyntax: every char 0..255 ordinary, newlines included, lineno frozen (实测) ----
    static void testResetSyntax() throws Exception {
        StreamTokenizer t = st("ab\ncd");
        t.resetSyntax();
        int n = 0;
        StringBuilder seq = new StringBuilder();
        while (t.nextToken() != StreamTokenizer.TT_EOF) {
            n++;
            seq.append((char) t.ttype);
        }
        check(n == 5, "five one-char tokens: " + n);
        check("ab\ncd".contentEquals(seq), "the newline itself is delivered as a token");
        check(t.lineno() == 1, "ordinary newlines do not advance lineno");
        // whitespace can be re-established on top of resetSyntax
        t = st("a b");
        t.resetSyntax();
        t.whitespaceChars(' ', ' ');
        check(t.nextToken() == 'a' && t.nextToken() == 'b'
                  && t.nextToken() == StreamTokenizer.TT_EOF,
              "whitespaceChars layers back onto resetSyntax");
        // numbers can be re-established too
        t = st("42");
        t.resetSyntax();
        t.parseNumbers();
        check(t.nextToken() == StreamTokenizer.TT_NUMBER && t.nval == 42.0,
              "parseNumbers layers back onto resetSyntax");
    }

    // ---- EOL significance: \r\n is ONE EOL, blank \r\n lines yield standalone EOLs (实测) ----
    static void testEolSignificant() throws Exception {
        StreamTokenizer t = st("a\r\nb\r\n\r\nc");
        t.eolIsSignificant(true);
        check(t.nextToken() == StreamTokenizer.TT_WORD, "a");
        check(t.nextToken() == StreamTokenizer.TT_EOL && t.lineno() == 2,
              "\\r\\n collapses into a single EOL");
        check(t.nextToken() == StreamTokenizer.TT_WORD, "b");
        check(t.nextToken() == StreamTokenizer.TT_EOL && t.lineno() == 3, "second EOL");
        check(t.nextToken() == StreamTokenizer.TT_EOL && t.lineno() == 4,
              "the blank line emits its own EOL");
        check(t.nextToken() == StreamTokenizer.TT_WORD && "c".equals(t.sval), "c");
        check(t.nextToken() == StreamTokenizer.TT_EOF, "EOF");
        // with EOL insignificant the same input is just words
        t = st("a\r\nb");
        check(t.nextToken() == StreamTokenizer.TT_WORD && t.nextToken() == StreamTokenizer.TT_WORD
                  && t.lineno() == 2, "insignificant EOLs vanish but still count lines");
    }

    // ---- lineno precision quirks in block comments (实测) ----
    static void testLinenoQuirks() throws Exception {
        // a block comment swallowing "\n\n" only advances the line by ONE
        StreamTokenizer t = st("a /* c\n\n*/ b");
        t.slashStarComments(true);
        check(t.nextToken() == StreamTokenizer.TT_WORD && "a".equals(t.sval), "a");
        check(t.nextToken() == StreamTokenizer.TT_WORD && "b".equals(t.sval) && t.lineno() == 2,
              "double newline inside a block comment counts as one line: " + t.lineno());
        // a single "\n" in the same position also lands on line 2
        t = st("a /* c\n*/ b");
        t.slashStarComments(true);
        t.nextToken();
        t.nextToken();
        check(t.lineno() == 2, "single newline inside a block comment: one line");
        // unterminated block comment: EOF, lineno untouched
        t = st("a /* never ends");
        t.slashStarComments(true);
        check(t.nextToken() == StreamTokenizer.TT_WORD && "a".equals(t.sval), "a");
        check(t.nextToken() == StreamTokenizer.TT_EOF && t.lineno() == 1,
              "unterminated block comment hits EOF without advancing lines");
        // lone \r between words advances the line
        t = st("a\rb");
        t.nextToken();
        t.nextToken();
        check(t.lineno() == 2, "lone CR advances lineno");
    }

    // ---- '/' configuration: ordinaryChar disables comments; the flags re-enable (实测) ----
    static void testSlashConfiguration() throws Exception {
        // making '/' ordinary stops both comment styles
        StreamTokenizer t = st("a /* x */ b");
        t.ordinaryChar('/');
        check(t.nextToken() == StreamTokenizer.TT_WORD && "a".equals(t.sval), "a read as a word");
        check(t.nextToken() == '/', "slash delivered as itself");
        check(t.nextToken() == '*', "star delivered as itself");
        // slashSlashComments(true) restores line-comment behavior even after ordinaryChar
        t = st("a // gone\nb");
        t.ordinaryChar('/');
        t.slashSlashComments(true);
        check(t.nextToken() == StreamTokenizer.TT_WORD && "a".equals(t.sval), "a again");
        check(t.nextToken() == StreamTokenizer.TT_WORD && "b".equals(t.sval),
              "line comment resumed to end of line");
        // line comments also end at a bare \r
        t = st("a // x\rb");
        t.slashSlashComments(true);
        check(t.nextToken() == StreamTokenizer.TT_WORD && "a".equals(t.sval)
                  && t.lineno() == 1, "a on line 1");
        check(t.nextToken() == StreamTokenizer.TT_WORD && "b".equals(t.sval)
                  && t.lineno() == 2, "CR terminates the line comment");
        // commentChar on the same char overrides a quoteChar
        t = st("a #bc# d");
        t.quoteChar('#');
        t.commentChar('#');
        check(t.nextToken() == StreamTokenizer.TT_WORD && "a".equals(t.sval), "a before #");
        check(t.nextToken() == StreamTokenizer.TT_EOF,
              "commentChar assignment wins over quoteChar: rest of line dropped");
    }

    // ---- number grammar corners: no exponent, no plus, octal-ish, precision (实测) ----
    static void testNumberMatrix() throws Exception {
        StreamTokenizer t = st(".");
        check(t.nextToken() == StreamTokenizer.TT_NUMBER && t.nval == 0.0,
              "lone '.' is the number 0.0");
        t = st(".x");
        check(t.nextToken() == StreamTokenizer.TT_NUMBER && t.nval == 0.0, "'.x' reads 0.0");
        check(t.nextToken() == StreamTokenizer.TT_WORD && "x".equals(t.sval),
              "'x' survives as a word");
        t = st("-0");
        check(t.nextToken() == StreamTokenizer.TT_NUMBER
                  && Double.doubleToRawLongBits(t.nval) == Double.doubleToRawLongBits(-0.0),
              "'-0' preserves the negative zero");
        t = st("123456789012345678901234567890");
        check(t.nextToken() == StreamTokenizer.TT_NUMBER && t.nval == 1.2345678901234566E29,
              "30-digit integer collapses through double arithmetic");
        t = st("1.7976931348623157e308");
        check(t.nextToken() == StreamTokenizer.TT_NUMBER && t.nval == 1.7976931348623155,
              "mantissa parses with accumulate-then-divide precision, not parseDouble");
        check(t.nextToken() == StreamTokenizer.TT_WORD && "e308".equals(t.sval),
              "'e308' has no exponent support: a plain word");
        t = st("3-4");
        check(t.nextToken() == StreamTokenizer.TT_NUMBER && t.nval == 3.0, "3 first");
        check(t.nextToken() == StreamTokenizer.TT_NUMBER && t.nval == -4.0,
              "'-' starts a fresh number");
        t = st("-");
        check(t.nextToken() == '-', "a lone '-' is an ordinary char (45)");
        t = st("NaN Infinity");
        check(t.nextToken() == StreamTokenizer.TT_WORD && "NaN".equals(t.sval),
              "NaN is a word, not a number");
        check(t.nextToken() == StreamTokenizer.TT_WORD && "Infinity".equals(t.sval),
              "Infinity is a word, not a number");
        t = st("0x1F");
        check(t.nextToken() == StreamTokenizer.TT_NUMBER && t.nval == 0.0,
              "hex prefix reads as 0.0");
        check(t.nextToken() == StreamTokenizer.TT_WORD && "x1F".equals(t.sval),
              "'x1F' remains a word");
        t = st("1\"q\"");
        check(t.nextToken() == StreamTokenizer.TT_NUMBER && t.nval == 1.0, "number first");
        check(t.nextToken() == '"', "a quote immediately after a number starts a string");
        t = st("007.5e2");
        check(t.nextToken() == StreamTokenizer.TT_NUMBER && t.nval == 7.5,
              "leading zeros drop: 007.5 = 7.5");
        t = st("..");
        check(t.nextToken() == StreamTokenizer.TT_NUMBER && t.nval == 0.0, "first '.'");
        check(t.nextToken() == StreamTokenizer.TT_NUMBER && t.nval == 0.0, "second '.'");
        t = st("5..6");
        check(t.nextToken() == StreamTokenizer.TT_NUMBER && t.nval == 5.0, "5.");
        check(t.nextToken() == StreamTokenizer.TT_NUMBER && t.nval == 0.6, ".6");
        t = st("+5");
        check(t.nextToken() == '+', "'+' is ordinary (only '-' belongs to numbers)");
        check(t.nextToken() == StreamTokenizer.TT_NUMBER && t.nval == 5.0, "then 5");
    }

    // ---- flag semantics: OR vs ASSIGN across the range setters (实测) ----
    static void testFlagSemantics() throws Exception {
        // whitespaceChars ASSIGNS: it wipes the word/digit flags of its range
        StreamTokenizer t = st("1a2");
        t.whitespaceChars('0', '9');
        check(t.nextToken() == StreamTokenizer.TT_WORD && "a".equals(t.sval),
              "whitespaceChars erases the digit number-flags");
        // wordChars ORs, but a later whitespaceChars still overwrites the overlap
        t = st("abcdefg");
        t.wordChars('a', 'g');
        t.whitespaceChars('a', 'c');
        check(t.nextToken() == StreamTokenizer.TT_WORD && "defg".equals(t.sval),
              "the a-c overlap became whitespace; d-g remain word chars");
        // a custom quoteChar coexists with the built-in double quote
        t = st("$hi$ \"dq\"");
        t.quoteChar('$');
        check(t.nextToken() == '$' && "hi".equals(t.sval), "custom quote char '$' (36)");
        check(t.nextToken() == '"' && "dq".equals(t.sval), "double quote still active");
        // quoteChar adds; ordinaryChar removes
        t = st("'a' \"b\"");
        t.quoteChar('\'');
        check(t.nextToken() == '\'' && "a".equals(t.sval), "single-quote reactivated");
        check(t.nextToken() == '"' && "b".equals(t.sval), "double quote untouched");
        t = st("\"x\"");
        t.ordinaryChar('"');
        check(t.nextToken() == '"' && t.sval == null, "quote char made ordinary (ttype only)");
        check(t.nextToken() == StreamTokenizer.TT_WORD && "x".equals(t.sval), "body scanned as word");
        check(t.nextToken() == '"' && t.sval == null, "closing quote ordinary again");
    }

    // ---- nval/sval state machine across token classes (实测) ----
    static void testNvalSvalState() throws Exception {
        StreamTokenizer t = st("5 word 7");
        t.nextToken();
        check(t.nval == 5.0 && t.sval == null, "number token: nval set, sval cleared");
        t.nextToken();
        check(t.sval.equals("word") && t.nval == 5.0,
              "word token: sval set, nval keeps its stale value");
        t.nextToken();
        check(t.ttype == StreamTokenizer.TT_NUMBER && t.nval == 7.0 && t.sval == null,
              "the trailing 7 is another number token: sval cleared, nval refreshed");
        t.nextToken();
        check(t.ttype == StreamTokenizer.TT_EOF && t.sval == null && t.nval == 7.0,
              "EOF: sval cleared, nval holds the last number");
        // ordinary char tokens clear both fields (fresh tokenizer)
        t = st("+");
        t.nextToken();
        check(t.sval == null && t.nval == 0.0, "ordinary token on fresh state: both null");
        // a word followed by an ordinary char clears the stale sval
        t = st("w +");
        t.nextToken();
        check("w".equals(t.sval), "word first");
        t.nextToken();
        check(t.sval == null && t.ttype == '+', "ordinary token clears the stale sval");
    }

    // ---- lowerCaseMode maps through char-wise lowercasing incl. non-ASCII (实测) ----
    static void testLowercaseMapping() throws Exception {
        StreamTokenizer t = st("\u0100B\u0100");
        t.lowerCaseMode(true);
        check(t.nextToken() == StreamTokenizer.TT_WORD && "\u0101b\u0101".equals(t.sval),
              "ASCII 'B'->'b' and 0x100->0x101 all lowercased");
        // a leading non-word char (e.g. '!') keeps the leading word lowercased only
        t = st("MiXeD");
        t.lowerCaseMode(true);
        check(t.nextToken() == StreamTokenizer.TT_WORD && "mixed".equals(t.sval),
              "ordinary ASCII lowercasing");
        t.lowerCaseMode(false);
        check(t.nextToken() == StreamTokenizer.TT_EOF, "EOF");
    }

    // ---- InputStream ctor: bytes >= 0x80 decode 1:1 into word chars (实测) ----
    static void testInputStreamCtor() throws Exception {
        @SuppressWarnings("deprecation")
        StreamTokenizer t = new StreamTokenizer(
            new java.io.ByteArrayInputStream(new byte[]{(byte) 0xC3, 'a', ' ', 'b'}));
        check(t.nextToken() == StreamTokenizer.TT_WORD && "\u00C3a".equals(t.sval),
              "byte 0xC3 becomes the single char U+00C3 in a word");
        check(t.nextToken() == StreamTokenizer.TT_WORD && "b".equals(t.sval), "token after space");
        check(t.nextToken() == StreamTokenizer.TT_EOF, "EOF");
    }

    // ---- Reader.read() failures propagate from nextToken (实测, 经 read() 单字符路径) ----
    static void testReaderPropagation() throws Exception {
        Reader r = new StringReader("xy") {
            public int read() throws IOException { throw new IOException("boom"); }
        };
        StreamTokenizer t = new StreamTokenizer(r);
        try {
            t.nextToken();
            check(false, "Reader failure must propagate");
        } catch (IOException expected) {
            check("boom".equals(expected.getMessage()), "exact IOException relayed");
        }
        // mid-stream failure after a good token
        Reader r2 = new StringReader("a b") {
            int n;
            public int read() throws IOException {
                if (n == 0) { n++; return 'a'; }
                if (n == 1) { n++; return ' '; }
                throw new IOException("late boom");
            }
        };
        StreamTokenizer t2 = new StreamTokenizer(r2);
        check(t2.nextToken() == StreamTokenizer.TT_WORD && "a".equals(t2.sval),
              "first token delivered normally");
        try {
            t2.nextToken();
            check(false, "mid-stream Reader failure must propagate");
        } catch (IOException expected) {
            check("late boom".equals(expected.getMessage()), "late failure relayed");
        }
    }

    public static void main(String[] args) throws Exception {
        testBehavior();
        testInitialState();
        testPushBackSemantics();
        testQuoteCloseRules();
        testEscapeMatrix();
        testHighChars();
        testResetSyntax();
        testEolSignificant();
        testLinenoQuirks();
        testSlashConfiguration();
        testNumberMatrix();
        testFlagSemantics();
        testNvalSvalState();
        testLowercaseMapping();
        testInputStreamCtor();
        testReaderPropagation();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
