/*
 * @test
 * @summary javax.swing.text.MaskFormatter conformance: white-box structure + black-box behavior.
 * @run main MaskFormatterTest
 */

import java.lang.reflect.Modifier;
import java.text.ParseException;
import javax.swing.text.MaskFormatter;

public class MaskFormatterTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.MaskFormatter");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.DefaultFormatter"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        MaskFormatter f = new MaskFormatter("###-####");
        check("###-####".equals(f.getMask()), "constructor/getMask");
        check(f.getAllowsInvalid() == false, "allowsInvalid default false");
        check(f.getValueContainsLiteralCharacters() == true, "valueContainsLiteralCharacters default true");
        check(f.getPlaceholderCharacter() == ' ', "placeholder char default space");
        check(f.getPlaceholder() == null, "placeholder string default null");

        check("123-4567".equals(f.valueToString("123-4567")), "valueToString literal value");
        check("123-4567".equals(f.stringToValue("123-4567")), "stringToValue literal value");
        check("123-4567".equals(f.stringToValue(f.valueToString("123-4567"))), "round trip");

        boolean shortLen = false;
        try { f.stringToValue("1234567"); } catch (ParseException pe) { shortLen = true; }
        check(shortLen, "short value -> ParseException");
        boolean badChar = false;
        try { f.stringToValue("12a-4567"); } catch (ParseException pe) { badChar = true; }
        check(badChar, "non-digit -> ParseException");

        MaskFormatter g = new MaskFormatter("###-####");
        g.setValueContainsLiteralCharacters(false);
        check(g.getValueContainsLiteralCharacters() == false, "setValueContainsLiteralCharacters round trip");
        check("123-4567".equals(g.valueToString("1234567")), "valueToString without literal input");
        check("1234567".equals(g.stringToValue("123-4567")), "stringToValue strips literals");

        MaskFormatter h = new MaskFormatter("###-####");
        h.setPlaceholderCharacter('_');
        check(h.getPlaceholderCharacter() == '_', "setPlaceholderCharacter round trip");
        check("123-____".equals(h.valueToString("123")), "valueToString pads with placeholder char");

        MaskFormatter p = new MaskFormatter("###-####");
        p.setPlaceholder("555-1212");
        check("555-1212".equals(p.getPlaceholder()), "setPlaceholder round trip");
        check("123-1212".equals(p.valueToString("123")), "valueToString pads with placeholder string");

        MaskFormatter v = new MaskFormatter("###-####");
        v.setValidCharacters("0123456789");
        check("0123456789".equals(v.getValidCharacters()), "setValidCharacters round trip");
        v.setInvalidCharacters("9");
        check("9".equals(v.getInvalidCharacters()), "setInvalidCharacters round trip");
        boolean invalid = false;
        try { v.valueToString("129-4567"); } catch (ParseException pe) { invalid = true; }
        check(invalid, "invalid character in value -> ParseException");

        v.setMask("(###) ###-####");
        check("(###) ###-####".equals(v.getMask()), "setMask round trip");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
