import java.util.Locale;
import java.util.TimeZone;

public class CharacterProbe {
    interface Fn { void run() throws Exception; }
    static void emit(String k, Object v) { System.out.println(k + "=" + v); }
    static void ex(String k, Fn f) {
        try { f.run(); }
        catch (Throwable t) { System.out.println(k + "=exception=" + t.getClass().getName()); }
    }
    public static void main(String[] a) {
        Locale.setDefault(Locale.US);
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        try {
            emit("digit.A", Character.digit('A', 16));
            emit("digit.z", Character.digit('z', 36));
            emit("digit.arabic", Character.digit('١', 10));
            emit("digit.romNum", Character.digit('Ⅻ', 10));
            emit("forDigit.10", Character.forDigit(10, 16));
            emit("forDigit.35", Character.forDigit(35, 36));
            ex("forDigit.radix37", () -> emit("forDigit.radix37", Character.forDigit(5, 37)));
            ex("forDigit.neg", () -> emit("forDigit.neg", Character.forDigit(-1, 16)));
            emit("isWhitespace.nbsp", Character.isWhitespace(' '));
            emit("isSpaceChar.nbsp", Character.isSpaceChar(' '));
            emit("isWhitespace.tab", Character.isWhitespace('\t'));
            emit("getType.emoji", Character.getType(0x1F600));
            emit("getType.ascii", Character.getType('A'));
            emit("isLetter.cherokee", Character.isLetter('Ꮳ'));
            emit("toUpperCase.ss", Character.toUpperCase('ß'));
            emit("toLowerCase.tr", Character.toLowerCase('I'));
            emit("charCount.emoji", Character.charCount(0x1F600));
            emit("isSupplementary", Character.isSupplementaryCodePoint(0x1F600));
            emit("toChars.emoji", new String(Character.toChars(0x1F600)));
            emit("isAlphabetic.greek", Character.isAlphabetic(0x03B1));
            emit("isIdeographic.han", Character.isIdeographic(0x4E00));
        } catch (Throwable t) { System.out.println("fatal=exception=" + t.getClass().getName()); }
    }
}
