/*
 * @test
 * @summary java.lang.Character API conformance: constants (radix/surrogate/code
 *          point), digit/forDigit, category predicates, case conversion, space
 *          and ISO-control classification, Java-identifier predicates, numeric
 *          value/type, surrogate-pair and code-point helpers, and reverseBytes.
 * @run main CharacterTest
 */

public class CharacterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testConstants() {
        check(Character.MIN_VALUE == 0, "MIN_VALUE == 0");
        check(Character.MAX_VALUE == 0xffff, "MAX_VALUE == 0xffff");
        check(Character.SIZE == 16, "SIZE == 16");
        check(Character.BYTES == 2, "BYTES == 2");
        check(Character.MIN_RADIX == 2, "MIN_RADIX == 2");
        check(Character.MAX_RADIX == 36, "MAX_RADIX == 36");
        check(Character.MIN_SURROGATE == 0xd800, "MIN_SURROGATE == 0xd800");
        check(Character.MAX_SURROGATE == 0xdfff, "MAX_SURROGATE == 0xdfff");
        check(Character.MIN_HIGH_SURROGATE == 0xd800, "MIN_HIGH_SURROGATE == 0xd800");
        check(Character.MAX_HIGH_SURROGATE == 0xdbff, "MAX_HIGH_SURROGATE == 0xdbff");
        check(Character.MIN_LOW_SURROGATE == 0xdc00, "MIN_LOW_SURROGATE == 0xdc00");
        check(Character.MAX_LOW_SURROGATE == 0xdfff, "MAX_LOW_SURROGATE == 0xdfff");
        check(Character.MIN_CODE_POINT == 0, "MIN_CODE_POINT == 0");
        check(Character.MAX_CODE_POINT == 0x10ffff, "MAX_CODE_POINT == 0x10ffff");
        check(Character.MIN_SUPPLEMENTARY_CODE_POINT == 0x10000,
              "MIN_SUPPLEMENTARY_CODE_POINT == 0x10000");
    }

    static void testDigit() {
        check(Character.digit('0', 10) == 0, "digit('0',10)");
        check(Character.digit('9', 10) == 9, "digit('9',10)");
        check(Character.digit('A', 16) == 10, "digit('A',16)");
        check(Character.digit('a', 16) == 10, "digit('a',16)");
        check(Character.digit('Z', 36) == 35, "digit('Z',36)");
        check(Character.digit('z', 36) == 35, "digit('z',36)");
        check(Character.digit('A', 10) == -1, "digit('A',10) == -1");
        check(Character.digit('5', 2) == -1, "digit('5',2) == -1");
        check(Character.digit('0', 37) == -1, "digit('0',37) invalid radix");
        check(Character.digit('0', 1) == -1, "digit('0',1) invalid radix");
        check(Character.digit(0x10, 16) == -1, "digit(0x10,16) control char");
    }

    static void testForDigit() {
        check(Character.forDigit(0, 10) == '0', "forDigit(0,10)");
        check(Character.forDigit(9, 10) == '9', "forDigit(9,10)");
        check(Character.forDigit(10, 16) == 'a', "forDigit(10,16)");
        check(Character.forDigit(15, 16) == 'f', "forDigit(15,16)");
        check(Character.forDigit(35, 36) == 'z', "forDigit(35,36)");
        check(Character.forDigit(-1, 10) == '\0', "forDigit(-1,10) == NUL");
        check(Character.forDigit(10, 10) == '\0', "forDigit(10,10) out of range");
        check(Character.forDigit(0, 37) == '\0', "forDigit(0,37) invalid radix");
    }

    static void testCategory() {
        check(Character.isDigit('0') && Character.isDigit('9'), "isDigit('0'/'9')");
        check(!Character.isDigit('A'), "!isDigit('A')");
        check(Character.isLetter('a') && Character.isLetter('Z'), "isLetter('a'/'Z')");
        check(!Character.isLetter('0'), "!isLetter('0')");
        check(Character.isLetterOrDigit('a'), "isLetterOrDigit('a')");
        check(Character.isLetterOrDigit('5'), "isLetterOrDigit('5')");
        check(!Character.isLetterOrDigit('_'), "!isLetterOrDigit('_')");
        check(Character.isUpperCase('A'), "isUpperCase('A')");
        check(!Character.isUpperCase('a'), "!isUpperCase('a')");
        check(Character.isLowerCase('a'), "isLowerCase('a')");
        check(!Character.isLowerCase('A'), "!isLowerCase('A')");
        check(Character.isTitleCase('ǅ'), "isTitleCase('\\u01C5')");
        check(Character.isDefined('a'), "isDefined('a')");
    }

    static void testCaseConversion() {
        check(Character.toUpperCase('a') == 'A', "toUpperCase('a')");
        check(Character.toLowerCase('A') == 'a', "toLowerCase('A')");
        check(Character.toUpperCase('1') == '1', "toUpperCase('1') identity");
        check(Character.toLowerCase('1') == '1', "toLowerCase('1') identity");
        check(Character.toUpperCase(0x61) == 0x41, "toUpperCase(codepoint)");
        check(Character.toLowerCase(0x41) == 0x61, "toLowerCase(codepoint)");
    }

    static void testSpace() {
        check(Character.isWhitespace(' '), "isWhitespace(' ')");
        check(Character.isWhitespace('\t'), "isWhitespace('\\t')");
        check(Character.isWhitespace('\n'), "isWhitespace('\\n')");
        check(!Character.isWhitespace('a'), "!isWhitespace('a')");
        check(Character.isSpaceChar(' '), "isSpaceChar(' ')");
        check(!Character.isSpaceChar('\n'), "!isSpaceChar('\\n')");
        check(Character.isISOControl('\n'), "isISOControl('\\n')");
        check(Character.isISOControl('\0'), "isISOControl('\\0')");
        check(!Character.isISOControl('a'), "!isISOControl('a')");
    }

    static void testIdentifier() {
        check(Character.isJavaIdentifierStart('a'), "isJavaIdentifierStart('a')");
        check(Character.isJavaIdentifierStart('$'), "isJavaIdentifierStart('$')");
        check(Character.isJavaIdentifierStart('_'), "isJavaIdentifierStart('_')");
        check(!Character.isJavaIdentifierStart('0'), "!isJavaIdentifierStart('0')");
        check(Character.isJavaIdentifierPart('0'), "isJavaIdentifierPart('0')");
        check(Character.isJavaIdentifierPart('$'), "isJavaIdentifierPart('$')");
        check(Character.isIdentifierIgnorable('\0'), "isIdentifierIgnorable('\\0')");
        check(Character.isUnicodeIdentifierStart('a'), "isUnicodeIdentifierStart('a')");
        check(Character.isUnicodeIdentifierPart('a'), "isUnicodeIdentifierPart('a')");
    }

    static void testNumericAndType() {
        check(Character.getNumericValue('5') == 5, "getNumericValue('5')");
        check(Character.getNumericValue('A') == 10, "getNumericValue('A')");
        check(Character.getNumericValue('a') == 10, "getNumericValue('a')");
        check(Character.getNumericValue('½') == -2, "getNumericValue('\\u00BD') == -2");
        check(Character.getNumericValue('x') == 33, "getNumericValue('x') == 33 (radix-36)");
        check(Character.getNumericValue('z') == 35, "getNumericValue('z') == 35");
        check(Character.getNumericValue('@') == -1, "getNumericValue('@') == -1");
        check(Character.getType('A') == Character.UPPERCASE_LETTER, "getType('A')");
        check(Character.getType('a') == Character.LOWERCASE_LETTER, "getType('a')");
        check(Character.getType('0') == Character.DECIMAL_DIGIT_NUMBER, "getType('0')");
        check(Character.getType(' ') == Character.SPACE_SEPARATOR, "getType(' ')");
    }

    static void testSurrogates() {
        check(Character.isSurrogate((char) 0xd800), "isSurrogate(0xd800)");
        check(Character.isHighSurrogate((char) 0xd800), "isHighSurrogate(0xd800)");
        check(!Character.isHighSurrogate((char) 0xdc00), "!isHighSurrogate(0xdc00)");
        check(Character.isLowSurrogate((char) 0xdc00), "isLowSurrogate(0xdc00)");
        check(Character.isSurrogatePair((char) 0xd800, (char) 0xdc00),
              "isSurrogatePair(0xd800,0xdc00)");
        check(!Character.isSurrogatePair((char) 0xdc00, (char) 0xd800),
              "!isSurrogatePair(reversed)");
        check(Character.toCodePoint((char) 0xd800, (char) 0xdc00) == 0x10000,
              "toCodePoint(0xd800,0xdc00)");
    }

    static void testCodePoint() {
        check(Character.charCount(0x10000) == 2, "charCount(0x10000) == 2");
        check(Character.charCount(0xffff) == 1, "charCount(0xffff) == 1");
        check(Character.isSupplementaryCodePoint(0x10000), "isSupplementaryCodePoint(0x10000)");
        check(!Character.isSupplementaryCodePoint(0xffff), "!isSupplementaryCodePoint(0xffff)");
        check(Character.isBmpCodePoint(0xffff), "isBmpCodePoint(0xffff)");
        check(!Character.isBmpCodePoint(0x10000), "!isBmpCodePoint(0x10000)");
        check(Character.isValidCodePoint(0x10ffff), "isValidCodePoint(0x10ffff)");
        check(!Character.isValidCodePoint(0x110000), "!isValidCodePoint(0x110000)");
        check(!Character.isValidCodePoint(-1), "!isValidCodePoint(-1)");
        check(Character.highSurrogate(0x10000) == 0xd800, "highSurrogate(0x10000)");
        check(Character.lowSurrogate(0x10000) == 0xdc00, "lowSurrogate(0x10000)");
    }

    static void testMisc() {
        check(Character.reverseBytes((char) 0x1234) == 0x3412, "reverseBytes(0x1234)");
        check(Character.valueOf('a') == Character.valueOf('a'), "valueOf cache");
        check(Character.valueOf('a').charValue() == 'a', "charValue");
        check(Character.compare('a', 'b') < 0, "compare('a','b') < 0");
        check(Character.compare('b', 'a') > 0, "compare('b','a') > 0");
        check(Character.compare('a', 'a') == 0, "compare('a','a') == 0");
        check(Character.valueOf('a').hashCode() == 97, "hashCode('a') == 97");
        check(Character.toString('a').equals("a"), "toString('a')");
        check(Character.valueOf('a').equals(Character.valueOf('a')), "equals same");
        check(!Character.valueOf('a').equals(Character.valueOf('b')), "!equals different");
    }

    // ---- Contract deepening (2026-11): full digit/forDigit matrix, numeric
    // values, category/type matrix, isWhitespace vs isSpaceChar fork, ISO
    // control matrix, case conversion matrix, surrogate/code-point suite,
    // Unicode version gates, UnicodeBlock mapping, identifier predicates.
    static final boolean MODERN = !System.getProperty("java.version").startsWith("1.");

    static void expectSIOOBE(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected StringIndexOutOfBoundsException");
        } catch (StringIndexOutOfBoundsException expected) {
            passed++;
        }
    }

    static void expectIOOBE(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException expected) {
            passed++;
        }
    }

    static void expectIAE(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            passed++;
        }
    }

    static void testDigitMatrix() {
        for (char c = '0'; c <= '9'; c++) {
            check(Character.digit(c, 10) == c - '0', "digit('0'..'9', 10) at " + c);
        }
        for (char c = 'a'; c <= 'z'; c++) {
            check(Character.digit(c, 36) == c - 'a' + 10, "digit('a'..'z', 36) at " + c);
        }
        for (char c = 'A'; c <= 'Z'; c++) {
            check(Character.digit(c, 36) == c - 'A' + 10, "digit('A'..'Z', 36) at " + c);
        }
        for (char c = 'a'; c <= 'f'; c++) {
            check(Character.digit(c, 16) == c - 'a' + 10, "digit('a'..'f', 16) at " + c);
        }
        for (char c = 'A'; c <= 'F'; c++) {
            check(Character.digit(c, 16) == c - 'A' + 10, "digit('A'..'F', 16) at " + c);
        }
        check(Character.digit('9', 9) == -1, "digit('9', 9) out of digit range");
        check(Character.digit('8', 9) == 8, "digit('8', 9)");
        check(Character.digit('a', 37) == -1, "digit('a', 37) radix out of range -> -1");
        check(Character.digit('0', 1) == -1, "digit('0', 1) radix below MIN_RADIX");
        check(Character.digit('0', 0) == -1, "digit('0', 0)");
        check(Character.digit('0', -5) == -1, "digit('0', negative radix)");
        // fullwidth forms decode as digits in the same radixes.
        check(Character.digit('０', 10) == 0, "digit(fullwidth 0, 10)");
        check(Character.digit('９', 10) == 9, "digit(fullwidth 9, 10)");
        check(Character.digit('６', 10) == 6, "digit(fullwidth 6, 10)");
        check(Character.digit('Ａ', 16) == 10, "digit(fullwidth A, 16)");
        for (int i = 0; i <= 9; i++) {
            check(Character.forDigit(i, 10) == '0' + i, "forDigit(0..9, 10) at " + i);
        }
        for (int i = 0; i <= 15; i++) {
            char expected = (char) (i < 10 ? '0' + i : 'a' + i - 10);
            check(Character.forDigit(i, 16) == expected, "forDigit(0..15, 16) at " + i);
        }
        check(Character.forDigit(10, 36) == 'a', "forDigit(10, 36) == 'a'");
        check(Character.forDigit(16, 16) == '\0', "forDigit(16, 16) out of range -> NUL");
        check(Character.forDigit(35, 35) == '\0', "forDigit(35, 35) out of range -> NUL");
        check(Character.forDigit(0, 1) == '\0', "forDigit(0, 1) invalid radix -> NUL");
        // digit/forDigit round-trip at both ends of every radix range.
        for (int r = Character.MIN_RADIX; r <= Character.MAX_RADIX; r++) {
            char lo = Character.forDigit(0, r);
            char hi = Character.forDigit(r - 1, r);
            check(Character.forDigit(Character.digit(lo, r), r) == lo,
                  "digit/forDigit round-trip radix " + r + " low end");
            check(Character.forDigit(Character.digit(hi, r), r) == hi,
                  "digit/forDigit round-trip radix " + r + " high end");
        }
    }

    static void testNumericValueMatrix() {
        for (char c = '0'; c <= '9'; c++) {
            check(Character.getNumericValue(c) == c - '0', "getNumericValue('0'..'9') at " + c);
        }
        for (char c = 'a'; c <= 'z'; c++) {
            check(Character.getNumericValue(c) == c - 'a' + 10, "getNumericValue(a..z) at " + c);
        }
        for (char c = 'A'; c <= 'Z'; c++) {
            check(Character.getNumericValue(c) == c - 'A' + 10, "getNumericValue(A..Z) at " + c);
        }
        check(Character.getNumericValue('１') == 1, "getNumericValue(fullwidth 1)");
        check(Character.getNumericValue('９') == 9, "getNumericValue(fullwidth 9)");
        check(Character.getNumericValue('Ａ') == 10, "getNumericValue(fullwidth A)");
        check(Character.getNumericValue('Ⅰ') == 1, "getNumericValue(Roman I) == 1");
        check(Character.getNumericValue('Ⅷ') == 8, "getNumericValue(Roman VIII) == 8");
        check(Character.getNumericValue('Ⅻ') == 12, "getNumericValue(Roman XII) == 12");
        check(Character.getNumericValue('Ⅿ') == 1000, "getNumericValue(Roman M) == 1000");
        check(Character.getNumericValue('〇') == 0, "getNumericValue(ideographic zero)");
        check(Character.getNumericValue('一') == -1,
              "getNumericValue(CJK one) == -1 (ideographs are not numeric)");
        check(Character.getNumericValue('$') == -1, "getNumericValue('$') == -1");
        check(Character.getNumericValue('+') == -1, "getNumericValue('+') == -1");
        check(Character.getNumericValue(0xFF19) == 9, "getNumericValue(int fullwidth 9)");
        check(Character.getNumericValue(0x41) == 10, "getNumericValue(int 'A') == 10");
    }

    static void testCategoryMatrix() {
        for (char c = 'a'; c <= 'z'; c++) {
            check(Character.isLowerCase(c) && Character.isLetter(c)
                  && !Character.isUpperCase(c) && !Character.isDigit(c),
                  "lowercase letter matrix at " + c);
        }
        for (char c = 'A'; c <= 'Z'; c++) {
            check(Character.isUpperCase(c) && Character.isLetter(c)
                  && !Character.isLowerCase(c) && !Character.isDigit(c),
                  "uppercase letter matrix at " + c);
        }
        for (char c = '0'; c <= '9'; c++) {
            check(Character.isDigit(c) && !Character.isLetter(c)
                  && Character.isLetterOrDigit(c),
                  "digit matrix at " + c);
        }
        check(!Character.isLetterOrDigit('_'), "'_' is not a letter or digit");
        check(!Character.isLetterOrDigit('$'), "'$' is not a letter or digit");
        check(Character.getType('Z') == Character.UPPERCASE_LETTER, "getType('Z')");
        check(Character.getType('5') == Character.DECIMAL_DIGIT_NUMBER, "getType('5')");
        check(Character.getType('_') == Character.CONNECTOR_PUNCTUATION, "getType('_')");
        check(Character.getType('$') == Character.CURRENCY_SYMBOL, "getType('$')");
        check(Character.getType('中') == Character.OTHER_LETTER, "getType(CJK ideograph)");
        check(Character.getType('½') == Character.OTHER_NUMBER, "getType(1/2 fraction)");
        check(Character.getType('Ⅷ') == Character.LETTER_NUMBER, "getType(Roman VIII)");
        check(Character.getType('‘') == Character.INITIAL_QUOTE_PUNCTUATION,
              "getType(left single quote)");
        check(Character.getType('\u0000') == Character.CONTROL, "getType(NUL)");
        check(Character.getType('\uD800') == Character.SURROGATE, "getType(high surrogate)");
        check(Character.getType(' ') == Character.SPACE_SEPARATOR, "getType(space)");
        // isAlphabetic is Unicode-rule based: Roman numerals are alphabetic
        // (category Nl) but isLetter is JDK-defined and excludes them.
        check(Character.isAlphabetic('A') && !Character.isAlphabetic('1'), "isAlphabetic basics");
        check(Character.isAlphabetic('Ⅰ') && !Character.isLetter('Ⅰ'),
              "isAlphabetic vs isLetter fork on Roman I");
        check(Character.isDefined('９') && !Character.isDefined('￾'),
              "fullwidth defined, noncharacter U+FFFE not defined");
    }

    static void testSpaceMatrix() {
        // isWhitespace vs isSpaceChar fork, identical on both JDK versions.
        check(Character.isWhitespace(' ') && Character.isSpaceChar(' '), "U+0020 both true");
        check(!Character.isWhitespace(' ') && Character.isSpaceChar(' '),
              "U+00A0 NBSP: space char but not whitespace");
        check(Character.isWhitespace(' ') && Character.isSpaceChar(' '), "U+2000 both");
        check(Character.isWhitespace(' ') && Character.isSpaceChar(' '), "U+2003 both");
        check(!Character.isWhitespace(' ') && Character.isSpaceChar(' '),
              "U+2007 figure space: no-break");
        check(!Character.isWhitespace(' ') && Character.isSpaceChar(' '),
              "U+202F narrow no-break space");
        check(Character.isWhitespace('　') && Character.isSpaceChar('　'),
              "U+3000 ideographic space both");
        check(Character.isWhitespace(' ') && Character.isSpaceChar(' '), "U+1680 both");
        check(Character.isWhitespace(' ') && Character.isSpaceChar(' '), "U+200A both");
        check(Character.isWhitespace(' ') && Character.isSpaceChar(' '), "U+205F both");
        check(Character.isWhitespace(' ') && Character.isSpaceChar(' '),
              "U+2028 line separator both");
        check(Character.isWhitespace(' ') && Character.isSpaceChar(' '),
              "U+2029 paragraph separator both");
        for (char c = '\u0009'; c <= (char) 0x000D; c++) {
            check(Character.isWhitespace(c) && !Character.isSpaceChar(c),
                  "C0 controls whitespace-only at 0x" + Integer.toHexString(c));
        }
        for (char c = '\u001C'; c <= '\u001F'; c++) {
            check(Character.isWhitespace(c) && !Character.isSpaceChar(c),
                  "information separators at 0x" + Integer.toHexString(c));
        }
        check(!Character.isWhitespace('­') && !Character.isSpaceChar('­'),
              "soft hyphen is neither");
        check(Character.getType(' ') == Character.SPACE_SEPARATOR,
              "getType(NBSP) == SPACE_SEPARATOR");
    }

    static void testISOControlMatrix() {
        for (int c = 0x0000; c <= 0x001F; c++) {
            check(Character.isISOControl(c), "isISOControl C0 at 0x" + Integer.toHexString(c));
        }
        check(Character.isISOControl(0x007F), "isISOControl(DEL)");
        for (int c = 0x0080; c <= 0x009F; c++) {
            check(Character.isISOControl(c), "isISOControl C1 at 0x" + Integer.toHexString(c));
        }
        check(!Character.isISOControl(' '), "space is not an ISO control");
        check(!Character.isISOControl(' '), "NBSP is not an ISO control");
        check(!Character.isISOControl('a'), "'a' is not an ISO control");
    }

    static void testCaseMatrix() {
        for (char c = 'a'; c <= 'z'; c++) {
            char up = (char) (c - 32);
            check(Character.toUpperCase(c) == up && Character.toLowerCase(c) == c
                  && Character.toTitleCase(c) == up, "ASCII case matrix at " + c);
        }
        for (char c = 'A'; c <= 'Z'; c++) {
            char lo = (char) (c + 32);
            check(Character.toLowerCase(c) == lo && Character.toUpperCase(c) == c,
                  "ASCII upper-case matrix at " + c);
        }
        for (char c = 0x20; c <= 0x40; c++) {
            check(Character.toUpperCase(c) == c && Character.toLowerCase(c) == c,
                  "punctuation/digits case-invariant at 0x" + Integer.toHexString(c));
        }
        // sharp s: the char methods cannot widen to "SS", so they return it unchanged.
        check(Character.toUpperCase('ß') == 'ß' && Character.toLowerCase('ß') == 'ß',
              "sharp s unchanged by char case conversion");
        check(Character.toUpperCase(0x00DF) == 0x00DF, "sharp s unchanged by int overload");
        check(Character.toUpperCase('ı') == 'I', "dotless i upper-cases to plain I");
        check(Character.toUpperCase('α') == 'Α', "Greek alpha -> Alpha");
        check(Character.toLowerCase('Α') == 'α', "Greek Alpha -> alpha");
        check(Character.toTitleCase('_') == '_', "'_' unchanged by toTitleCase");
        check(Character.toUpperCase('퟿') == '퟿', "last pre-surrogate char unchanged");
    }

    static void testSurrogateCodePointMatrix() {
        String music = "\ud834\udd1e"; // U+1D11E MUSICAL SYMBOL G CLEF
        check(music.length() == 2, "supplementary char spans two UTF-16 units");
        check(Character.codePointAt(music, 0) == 0x1D11E, "codePointAt(String,0) == 0x1D11E");
        check(Character.codePointAt(music, 1) == 0xDD1E, "codePointAt(String,1) low surrogate");
        check(Character.codePointBefore(music, 1) == 0xD834, "codePointBefore high surrogate");
        check(Character.codePointBefore(music, 2) == 0x1D11E, "codePointBefore(2) == 0x1D11E");
        check(Character.codePointCount(music, 0, 2) == 1, "codePointCount of pair == 1");
        check(Character.codePointCount(music, 0, 1) == 1, "lone high surrogate counts as 1");
        check(Character.offsetByCodePoints(music, 0, 1) == 2, "offsetByCodePoints skips pair");
        check(music.codePointCount(0, 2) == 1, "String.codePointCount of pair");
        check(music.codePointAt(0) == 0x1D11E, "String.codePointAt of pair");
        char[] pair = {'\ud834', '\udd1e'};
        check(Character.codePointAt(pair, 0) == 0x1D11E, "codePointAt(char[],0)");
        check(Character.codePointAt(pair, 0, 2) == 0x1D11E, "codePointAt(char[],0,2) with limit");
        check(Character.codePointBefore(pair, 2) == 0x1D11E, "codePointBefore(char[],2)");
        check(Character.codePointCount(pair, 0, 2) == 1, "codePointCount(char[],0,2)");
        char[] tc = Character.toChars(0x1D11E);
        check(tc.length == 2 && tc[0] == '\ud834' && tc[1] == '\udd1e', "toChars(0x1D11E) pair");
        check(new String(Character.toChars(0x1D11E)).equals(music), "toChars -> String round-trip");
        check(Character.toChars(0x41).length == 1 && Character.toChars(0x41)[0] == 'A',
              "toChars(BMP) single unit");
        check(Character.toChars(0x10FFFF).length == 2, "toChars(max code point)");
        check(Character.charCount(0x1D11E) == 2, "charCount(0x1D11E) == 2");
        check(Character.charCount('A') == 1, "charCount('A') == 1");
        check(Character.MIN_HIGH_SURROGATE == '\uD800', "MIN_HIGH_SURROGATE == D800");
        check(Character.MAX_HIGH_SURROGATE == '\uDBFF', "MAX_HIGH_SURROGATE == DBFF");
        check(Character.MIN_LOW_SURROGATE == '\uDC00', "MIN_LOW_SURROGATE == DC00");
        check(Character.MAX_LOW_SURROGATE == '\uDFFF', "MAX_LOW_SURROGATE == DFFF");
        check(Character.isHighSurrogate('\uD800') && Character.isHighSurrogate('\uDBFF'),
              "high surrogate range D800-DBFF");
        check(!Character.isHighSurrogate('\uDC00') && !Character.isHighSurrogate('퟿'),
              "outside high surrogate range");
        check(Character.isLowSurrogate('\uDC00') && Character.isLowSurrogate('\uDFFF'),
              "low surrogate range DC00-DFFF");
        check(!Character.isLowSurrogate('\uD800'), "D800 is not a low surrogate");
        check(Character.isSurrogate('\uD800') && Character.isSurrogate('\uDFFF')
              && !Character.isSurrogate('퟿'), "isSurrogate range");
        check(Character.isSurrogatePair('\uD83D', '\uDE00'), "isSurrogatePair emoji pair");
        check(Character.toCodePoint('\uD83D', '\uDE00') == 0x1F600, "toCodePoint emoji == 1F600");
        check(Character.toCodePoint(Character.highSurrogate(0x10FFFF),
                                    Character.lowSurrogate(0x10FFFF)) == 0x10FFFF,
              "high/low/toCodePoint round-trip at max");
        check(Character.highSurrogate(0x10FFFF) == '\uDBFF'
              && Character.lowSurrogate(0x10FFFF) == '\uDFFF', "max supplementary surrogates");
        String mixed = "a\ud834\udd1eb";
        check(Character.codePointCount(mixed, 0, 4) == 3, "codePointCount mixed string");
        check(Character.codePointCount(mixed, 0, 1) == 1, "codePointCount BMP prefix");
        check(mixed.offsetByCodePoints(0, 2) == 3, "offsetByCodePoints mixed string");
        expectSIOOBE(() -> Character.codePointAt(music, -1), "codePointAt negative index");
        expectSIOOBE(() -> Character.codePointAt(music, 5), "codePointAt past end");
        expectIOOBE(() -> Character.codePointCount(music, 1, 3), "codePointCount range past end");
        expectIAE(() -> Character.toChars(-1), "toChars negative");
        expectIAE(() -> Character.toChars(0x110000), "toChars above MAX_CODE_POINT");
    }

    static void testUnicodeVersionGates() {
        // Code points added after the JDK-8 Unicode tables (6.2): unassigned on
        // JDK-8, assigned on JDK-27. Lock each side's actual classification.
        int shakingFace = 0x1FAE0; // Emoji 15.0
        check(Character.isDefined(shakingFace) == MODERN,
              "U+1FAE0 defined only on modern Unicode tables");
        check(Character.getType(shakingFace)
              == (MODERN ? Character.OTHER_SYMBOL : Character.UNASSIGNED),
              "U+1FAE0 category depends on Unicode version");
        check(Character.isLetter(0x1E030) == MODERN,
              "U+1E030 modifier letter (Unicode 15.0)");
        check(Character.isLetter(0x1AFF0) == MODERN,
              "U+1AFF0 Kana Extended-B (Unicode 15.1)");
        check(Character.isDefined(0x2FFC) == MODERN,
              "U+2FFC IDS tertiary ideograph (Unicode 15.1)");
        check(String.valueOf(Character.UnicodeBlock.of(shakingFace))
              .equals(MODERN ? "SYMBOLS_AND_PICTOGRAPHS_EXTENDED_A" : "null"),
              "UnicodeBlock.of(U+1FAE0) is version-dependent");
    }

    static void testUnicodeBlockMatrix() {
        check(Character.UnicodeBlock.of('A') == Character.UnicodeBlock.BASIC_LATIN, "block(A)");
        check(Character.UnicodeBlock.of('Z') == Character.UnicodeBlock.BASIC_LATIN, "block(Z)");
        check(Character.UnicodeBlock.of('中') == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS,
              "block(CJK ideograph)");
        check(Character.UnicodeBlock.of('é') == Character.UnicodeBlock.LATIN_1_SUPPLEMENT,
              "block(e-acute)");
        check(Character.UnicodeBlock.of(0x1F600) == Character.UnicodeBlock.EMOTICONS,
              "block(emoji)");
        check(Character.UnicodeBlock.of('\uD800') == Character.UnicodeBlock.HIGH_SURROGATES,
              "block(high surrogate)");
        check(Character.UnicodeBlock.of('Ａ')
              == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS, "block(fullwidth A)");
        check(Character.UnicodeBlock.of('α') == Character.UnicodeBlock.GREEK, "block(greek)");
    }

    static void testIdentifierMatrix() {
        for (char c = 'a'; c <= 'z'; c++) {
            check(Character.isJavaIdentifierStart(c) && Character.isJavaIdentifierPart(c)
                  && Character.isUnicodeIdentifierStart(c) && Character.isUnicodeIdentifierPart(c),
                  "lowercase identifier matrix at " + c);
        }
        for (char c = 'A'; c <= 'Z'; c++) {
            check(Character.isJavaIdentifierStart(c) && Character.isJavaIdentifierPart(c)
                  && Character.isUnicodeIdentifierStart(c) && Character.isUnicodeIdentifierPart(c),
                  "uppercase identifier matrix at " + c);
        }
        for (char c = '0'; c <= '9'; c++) {
            check(!Character.isJavaIdentifierStart(c) && Character.isJavaIdentifierPart(c),
                  "digits are parts but not starts at " + c);
        }
        check(Character.isJavaIdentifierStart('_') && Character.isJavaIdentifierPart('_'),
              "'_' starts and continues Java identifiers");
        check(Character.isJavaIdentifierStart('$') && Character.isJavaIdentifierPart('$'),
              "'$' starts and continues Java identifiers (Java-only)");
        check(!Character.isJavaIdentifierStart('#') && !Character.isJavaIdentifierPart('#'),
              "'#' is not an identifier char");
        check(!Character.isJavaIdentifierStart(' ') && !Character.isJavaIdentifierPart(' '),
              "space is not an identifier char");
        check(!Character.isJavaIdentifierStart('-') && !Character.isJavaIdentifierPart('-'),
              "'-' is not an identifier char");
        check(!Character.isUnicodeIdentifierStart('$') && !Character.isUnicodeIdentifierPart('$'),
              "'$' is excluded from Unicode identifiers");
        for (char c = '\u0000'; c <= '\u0008'; c++) {
            check(Character.isIdentifierIgnorable(c), "ignorable C0 at 0x" + Integer.toHexString(c));
        }
        check(Character.isIdentifierIgnorable('\u000E') && Character.isIdentifierIgnorable('\u001B'),
              "ignorable 0x0E..0x1B range");
        for (int c = 0x7F; c <= 0x9F; c += 4) {
            check(Character.isIdentifierIgnorable(c), "ignorable C1 at 0x" + Integer.toHexString(c));
        }
        for (char c = '\u0009'; c <= (char) 0x000D; c++) {
            check(!Character.isIdentifierIgnorable(c),
                  "tab/LF/CR/FF/VT not ignorable at 0x" + Integer.toHexString(c));
        }
    }

    static void testMiscMatrix() {
        char[] cached = {'\u0000', '\u0001', '0', '9', 'A', 'Z', 'a', 'z', '\u007F'};
        for (char c : cached) {
            check(Character.valueOf(c) == Character.valueOf(c),
                  "valueOf caches 0..127 at 0x" + Integer.toHexString(c));
        }
        char[] uncached = {'\u0080', 'ÿ', '中', '￿'};
        for (char c : uncached) {
            check(Character.valueOf(c).equals(Character.valueOf(c))
                  && Character.valueOf(c).charValue() == c
                  && Character.valueOf(c).compareTo(Character.valueOf(c)) == 0,
                  "valueOf beyond cache equal by value at 0x" + Integer.toHexString(c));
        }
        check(Character.compare('a', 'b') == -1, "compare('a','b') == -1 exactly");
        check(Character.compare('b', 'a') == 1, "compare('b','a') == +1 exactly");
        check(Character.compare('A', 'a') == -32, "compare('A','a') == code-point diff -32");
        check(Character.compare('a', 'A') == 32, "compare('a','A') == +32");
        char[] hashed = {'\u0000', 'a', 'z', '中', '￿'};
        for (char c : hashed) {
            check(Character.hashCode(c) == c, "hashCode == value at 0x" + Integer.toHexString(c));
            check(Character.toString(c).equals(String.valueOf(c)),
                  "toString(char) at 0x" + Integer.toHexString(c));
        }
        check(Character.reverseBytes((char) 0x0001) == 0x0100, "reverseBytes(0x0001) == 0x0100");
        check(Character.reverseBytes((char) 0xABCD) == 0xCDAB, "reverseBytes(0xABCD) == 0xCDAB");
        check(Character.reverseBytes((char) 0) == 0, "reverseBytes(0) == 0");
        check(Character.reverseBytes((char) 0xFF00) == 0x00FF, "reverseBytes(0xFF00)");
        check(Character.reverseBytes((char) 0x00FF) == 0xFF00, "reverseBytes(0x00FF)");
        char[] reversed = {(char) 0x1234, (char) 0xABCD, (char) 0x0001, (char) 0xFF00};
        for (char c : reversed) {
            check(Character.reverseBytes(Character.reverseBytes(c)) == c,
                  "reverseBytes is an involution at 0x" + Integer.toHexString(c));
        }
        check(Character.MIN_VALUE == '\u0000', "MIN_VALUE == NUL char");
        check(Character.MAX_VALUE == '￿', "MAX_VALUE == FFFF char");
        check(Character.digit('1', Character.MIN_RADIX) == 1, "digit at MIN_RADIX");
        check(Character.isHighSurrogate(Character.MIN_HIGH_SURROGATE),
              "MIN_HIGH_SURROGATE is a high surrogate");
    }

    public static void main(String[] args) {
        testConstants();
        testDigit();
        testForDigit();
        testCategory();
        testCaseConversion();
        testSpace();
        testIdentifier();
        testNumericAndType();
        testSurrogates();
        testCodePoint();
        testMisc();
        testDigitMatrix();
        testNumericValueMatrix();
        testCategoryMatrix();
        testSpaceMatrix();
        testISOControlMatrix();
        testCaseMatrix();
        testSurrogateCodePointMatrix();
        testUnicodeVersionGates();
        testUnicodeBlockMatrix();
        testIdentifierMatrix();
        testMiscMatrix();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
