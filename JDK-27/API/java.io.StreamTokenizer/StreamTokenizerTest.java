/*
 * @test
 * @summary java.io.StreamTokenizer conformance: white-box structure + black-box
 *          behavior (default grammar incl. commentChar('/') and both quote chars,
 *          TT_* constants, quoted-string escapes incl. octal, number forms without
 *          exponent support, eolIsSignificant/lineno over \n \r \r\n, pushBack,
 *          lowerCaseMode applies to words only, comment chars, ordinary/whitespace/
 *          word range configuration, resetSyntax, toString formats).
 * @run main StreamTokenizerTest
 */

import java.io.StreamTokenizer;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class StreamTokenizerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.StreamTokenizer");
        check(c.getConstructor(java.io.Reader.class) != null, "ctor (Reader)");
        check(c.getConstructor(java.io.InputStream.class) != null, "ctor (InputStream)");
        for (String m : new String[]{"resetSyntax", "wordChars", "whitespaceChars",
                                     "ordinaryChars", "ordinaryChar", "commentChar",
                                     "quoteChar", "parseNumbers", "eolIsSignificant",
                                     "slashStarComments", "slashSlashComments",
                                     "lowerCaseMode", "nextToken", "pushBack",
                                     "lineno", "toString"})
            check(has(c, m), "has method " + m);
        for (String f : new String[]{"ttype", "sval", "nval"})
            check(c.getField(f) != null && c.getField(f).getType() != null,
                  "public field " + f);
        for (String f : new String[]{"TT_EOF", "TT_EOL", "TT_NUMBER", "TT_WORD"}) {
            Field fld = c.getField(f);
            check(Modifier.isPublic(fld.getModifiers()) && Modifier.isStatic(fld.getModifiers())
                      && Modifier.isFinal(fld.getModifiers()) && fld.getType() == int.class,
                  "constant " + f);
        }
        check(c.getField("TT_EOF").getInt(null) == -1, "TT_EOF == -1");
        check(c.getField("TT_EOL").getInt(null) == '\n', "TT_EOL == '\\n'");
        check(c.getField("TT_NUMBER").getInt(null) == -2, "TT_NUMBER == -2");
        check(c.getField("TT_WORD").getInt(null) == -3, "TT_WORD == -3");
        check(!Modifier.isAbstract(c.getModifiers()), "concrete class");
    }

    static boolean has(Class<?> c, String m) {
        for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals(m)) return true;
        return false;
    }

    static StreamTokenizer st(String s) {
        return new StreamTokenizer(new StringReader(s));
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. default grammar: words, ints, negatives, decimals; quote chars and '/'
        //    are already wired in the default constructor
        StreamTokenizer t = st("hello world 42 -7 3.14");
        check(t.nextToken() == StreamTokenizer.TT_WORD && "hello".equals(t.sval), "word 1");
        check(t.nextToken() == StreamTokenizer.TT_WORD && "world".equals(t.sval), "word 2");
        check(t.nextToken() == StreamTokenizer.TT_NUMBER && t.nval == 42.0, "42");
        check(t.nextToken() == StreamTokenizer.TT_NUMBER && t.nval == -7.0, "negative");
        check(t.nextToken() == StreamTokenizer.TT_NUMBER && t.nval == 3.14, "decimal");
        check(t.nextToken() == StreamTokenizer.TT_EOF, "EOF");
        check(t.lineno() == 1, "single line");

        // 2. the default commentChar is '/': a slash starts a comment to end of line
        t = st("x/y\nz");
        check(t.nextToken() == StreamTokenizer.TT_WORD && "x".equals(t.sval), "x before slash");
        check(t.nextToken() == StreamTokenizer.TT_WORD && "z".equals(t.sval), "rest of line dropped");
        check(t.nextToken() == StreamTokenizer.TT_EOF, "EOF");

        // 3. quoted strings: ttype is the quote character, sval holds the body,
        //    escapes \\ \" \n \t and octal are processed
        t = st("\"hi there\" \"esc\\\"q\" \"tab\\there\" \"back\\\\slash\" \"oct\\101l\"");
        check(t.nextToken() == '"' && "hi there".equals(t.sval), "quoted phrase");
        check(t.nextToken() == '"' && "esc\"q".equals(t.sval), "escaped quote");
        check(t.nextToken() == '"' && "tab\there".equals(t.sval), "escaped tab");
        check(t.nextToken() == '"' && "back\\slash".equals(t.sval), "escaped backslash");
        check(t.nextToken() == '"' && "octAl".equals(t.sval), "octal \\101 -> A");
        check(t.nextToken() == StreamTokenizer.TT_EOF, "EOF after quotes");

        // 4. an unterminated quote yields its body without failing
        t = st("\"abc");
        check(t.nextToken() == '"' && "abc".equals(t.sval), "unterminated quote body");
        check(t.nextToken() == StreamTokenizer.TT_EOF, "EOF follows");

        // 5. single quotes are quoted by default too; ordinaryChar() disables that
        t = st("'x' \"y\"");
        t.ordinaryChar('\'');
        check(t.nextToken() == '\'', "ordinary single quote");
        check(t.nextToken() == StreamTokenizer.TT_WORD && "x".equals(t.sval), "word x");
        check(t.nextToken() == '\'', "closing ordinary quote");
        check(t.nextToken() == '"' && "y".equals(t.sval), "double quote still quoted");
        check(t.nextToken() == StreamTokenizer.TT_EOF, "EOF");

        // 6. number forms: leading dot, trailing dot, leading zeros; no exponent
        //    support ('e' remains a word character)
        t = st(".5 5. 007 1e3");
        check(t.nextToken() == StreamTokenizer.TT_NUMBER && t.nval == 0.5, ".5");
        check(t.nextToken() == StreamTokenizer.TT_NUMBER && t.nval == 5.0, "5.");
        check(t.nextToken() == StreamTokenizer.TT_NUMBER && t.nval == 7.0, "007");
        check(t.nextToken() == StreamTokenizer.TT_NUMBER && t.nval == 1.0, "no exponent: 1");
        check(t.nextToken() == StreamTokenizer.TT_WORD && "e3".equals(t.sval), "e3 stays a word");

        // 7. eolIsSignificant: \n, \r, \r\n each produce one TT_EOL; lineno tracks
        t = st("a\nb\r\nc\rd");
        t.eolIsSignificant(true);
        check(t.nextToken() == StreamTokenizer.TT_WORD, "a");
        check(t.nextToken() == StreamTokenizer.TT_EOL && t.lineno() == 2,
              "EOL after a increments the line number");
        check(t.nextToken() == StreamTokenizer.TT_WORD && t.lineno() == 2, "b on line 2");
        check(t.nextToken() == StreamTokenizer.TT_EOL && t.lineno() == 3, "\\r\\n = one EOL");
        check(t.nextToken() == StreamTokenizer.TT_WORD && t.lineno() == 3, "c on line 3");
        check(t.nextToken() == StreamTokenizer.TT_EOL && t.lineno() == 4, "\\r is an EOL");
        check(t.nextToken() == StreamTokenizer.TT_WORD && t.lineno() == 4, "d on line 4");
        check(t.nextToken() == StreamTokenizer.TT_EOF, "EOF");

        // 8. with eolIsSignificant(false) (default) newline is plain whitespace
        t = st("a\nb");
        check(t.nextToken() == StreamTokenizer.TT_WORD && "a".equals(t.sval), "a");
        check(t.nextToken() == StreamTokenizer.TT_WORD && "b".equals(t.sval) && t.lineno() == 2,
              "newline skipped silently, line still counted");

        // 9. slashSlashComments / slashStarComments
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

        // 10. commentChar: custom character comments to end of line only
        t = st("keep#gone\nnext");
        t.commentChar('#');
        check(t.nextToken() == StreamTokenizer.TT_WORD && "keep".equals(t.sval), "before #");
        check(t.nextToken() == StreamTokenizer.TT_WORD && "next".equals(t.sval)
                  && t.lineno() == 2, "comment ended at the newline");

        // 11. lowerCaseMode lowercases words but not quoted strings
        t = st("MiXeD \"UnTouChEd\"");
        t.lowerCaseMode(true);
        check(t.nextToken() == StreamTokenizer.TT_WORD && "mixed".equals(t.sval), "word lowercased");
        check(t.nextToken() == '"' && "UnTouChEd".equals(t.sval), "quote untouched");
        t.lowerCaseMode(false);
        check(t.nextToken() == StreamTokenizer.TT_EOF, "EOF");

        // 12. pushBack: the next nextToken repeats the previous token
        t = st("one two");
        check(t.nextToken() == StreamTokenizer.TT_WORD && "one".equals(t.sval), "first token");
        t.pushBack();
        check(t.nextToken() == StreamTokenizer.TT_WORD && "one".equals(t.sval), "token repeated");
        check(t.nextToken() == StreamTokenizer.TT_WORD && "two".equals(t.sval), "then the next");

        // 13. wordChars ranges build custom tokens; whitespaceChars splits on them
        t = st("a_1b");
        t.wordChars('0', '9');
        t.wordChars('_', '_');
        check(t.nextToken() == StreamTokenizer.TT_WORD && "a_1b".equals(t.sval), "custom word chars");
        t = st("xbcy");
        t.whitespaceChars('b', 'c');
        check(t.nextToken() == StreamTokenizer.TT_WORD && "x".equals(t.sval), "split at b");
        check(t.nextToken() == StreamTokenizer.TT_WORD && "y".equals(t.sval), "split at c");

        // 14. ordinaryChars cancels word characters; parseNumbers restores digits
        t = st("42x");
        t.ordinaryChars('0', '9');
        check(t.nextToken() == '4' && t.nextToken() == '2', "digits became ordinary chars");
        check(t.nextToken() == StreamTokenizer.TT_WORD && "x".equals(t.sval), "x stays a word");
        t = st("42x");
        t.ordinaryChars('0', '9');
        t.parseNumbers();
        check(t.nextToken() == StreamTokenizer.TT_NUMBER && t.nval == 42.0,
              "parseNumbers reclaims digits");

        // 15. resetSyntax: every character becomes ordinary, even spaces
        t = st("a 1");
        t.resetSyntax();
        check(t.nextToken() == 'a' && t.nextToken() == ' ' && t.nextToken() == '1',
              "all characters ordinary");

        // 16. toString formats for word, number, quoted string, ordinary char, EOL, EOF
        t = st("x 3 \"q\" + \n");
        t.eolIsSignificant(true);
        t.nextToken();
        check("Token[x], line 1".equals(t.toString()), "word: " + t.toString());
        t.nextToken();
        check("Token[n=3.0], line 1".equals(t.toString()), "number: " + t.toString());
        t.nextToken();
        check("Token[q], line 1".equals(t.toString()), "quote: " + t.toString());
        t.nextToken();
        check("Token['+'], line 1".equals(t.toString()), "ordinary: " + t.toString());
        t.nextToken();
        check(t.ttype == StreamTokenizer.TT_EOL, "EOL token");
        t.nextToken();
        check("Token[EOF], line 2".equals(t.toString()), "eof: " + t.toString());

        // 17. empty input is immediate EOF
        t = st("");
        check(t.nextToken() == StreamTokenizer.TT_EOF && t.lineno() == 1, "empty input");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
