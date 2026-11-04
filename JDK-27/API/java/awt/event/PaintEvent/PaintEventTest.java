/*
 * @test
 * @summary java.awt.event.PaintEvent conformance: white-box structure + black-box behavior.
 * @run main PaintEventTest
 */

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.PaintEvent;
import java.lang.reflect.Modifier;

public class PaintEventTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.event.PaintEvent");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "PaintEvent is public");
        check(!Modifier.isAbstract(m), "PaintEvent is concrete");
        check(c.getSuperclass() == ComponentEvent.class, "PaintEvent extends ComponentEvent");
        check(c.getField("PAINT").getType() == int.class, "PAINT int");
        check(c.getField("UPDATE").getType() == int.class, "UPDATE int");
        check(c.getConstructor(Component.class, int.class, Rectangle.class) != null, "has (Component,int,Rectangle) ctor");
    }

    static void testBlackBox() throws Exception {
        Component src = new Component() { };
        Rectangle rect = new Rectangle(5, 6, 7, 8);
        PaintEvent e = new PaintEvent(src, PaintEvent.PAINT, rect);

        check(e.getID() == PaintEvent.PAINT, "getID round-trips");
        check(e.getSource() == src, "getSource round-trips");
        check(e.getComponent() == src, "getComponent returns source");
        check(e.getUpdateRect().equals(rect), "getUpdateRect round-trips");

        // setUpdateRect
        Rectangle rect2 = new Rectangle(0, 0, 1, 1);
        e.setUpdateRect(rect2);
        check(e.getUpdateRect().equals(rect2), "setUpdateRect changes rect");

        // documented constants
        check(PaintEvent.PAINT_FIRST == 800, "PAINT_FIRST == 800");
        check(PaintEvent.PAINT_LAST == 801, "PAINT_LAST == 801");
        check(PaintEvent.PAINT == 800, "PAINT == 800");
        check(PaintEvent.UPDATE == 801, "UPDATE == 801");

        // null update rect
        PaintEvent nullRect = new PaintEvent(src, PaintEvent.UPDATE, null);
        check(nullRect.getUpdateRect() == null, "null updateRect round-trips");

        // paramString
        check(e.paramString().equals("PAINT,updateRect=" + rect2), "paramString PAINT");
        check(nullRect.paramString().equals("UPDATE,updateRect=null"), "paramString UPDATE null");
        PaintEvent unk = new PaintEvent(src, 9999, rect);
        check(unk.paramString().equals("unknown type,updateRect=" + rect), "paramString unknown type");

        check(e instanceof ComponentEvent, "PaintEvent instanceof ComponentEvent");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
