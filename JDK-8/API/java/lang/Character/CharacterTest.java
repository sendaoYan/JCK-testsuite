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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
