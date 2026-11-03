/*
 * @test
 * @summary java.awt.event.TextEvent conformance: white-box structure + black-box behavior.
 * @run main TextEventTest
 */

import java.awt.AWTEvent;
import java.awt.event.TextEvent;
import java.lang.reflect.Modifier;

public class TextEventTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.event.TextEvent");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "TextEvent is public");
        check(!Modifier.isAbstract(m), "TextEvent is concrete");
        check(c.getSuperclass() == AWTEvent.class, "TextEvent extends AWTEvent");
        check(c.getField("TEXT_VALUE_CHANGED").getType() == int.class, "TEXT_VALUE_CHANGED int");
        check(c.getField("TEXT_FIRST").getType() == int.class, "TEXT_FIRST int");
        check(c.getField("TEXT_LAST").getType() == int.class, "TEXT_LAST int");
        check(c.getConstructor(Object.class, int.class) != null, "has (Object,int) ctor");
    }

    static void testBlackBox() throws Exception {
        Object src = new Object();
        TextEvent e = new TextEvent(src, TextEvent.TEXT_VALUE_CHANGED);

        check(e.getID() == TextEvent.TEXT_VALUE_CHANGED, "getID round-trips");
        check(e.getSource() == src, "getSource round-trips");
        check(e.paramString().equals("TEXT_VALUE_CHANGED"), "paramString TEXT_VALUE_CHANGED");

        TextEvent unk = new TextEvent(src, 9999);
        check(unk.paramString().equals("unknown type"), "paramString unknown type");

        // documented constants
        check(TextEvent.TEXT_FIRST == 900, "TEXT_FIRST == 900");
        check(TextEvent.TEXT_LAST == 900, "TEXT_LAST == 900");
        check(TextEvent.TEXT_VALUE_CHANGED == 900, "TEXT_VALUE_CHANGED == 900");

        // id range relationship
        check(TextEvent.TEXT_FIRST <= TextEvent.TEXT_VALUE_CHANGED && TextEvent.TEXT_VALUE_CHANGED <= TextEvent.TEXT_LAST, "id within range");

        check(e instanceof AWTEvent, "TextEvent instanceof AWTEvent");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
