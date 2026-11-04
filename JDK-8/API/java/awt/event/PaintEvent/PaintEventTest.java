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
        // setUpdateRect(null) accepted and round-trips through paramString
        e.setUpdateRect(null);
        check(e.getUpdateRect() == null, "setUpdateRect(null) accepted");
        check(e.paramString().equals("PAINT,updateRect=null"), "paramString null rect");
        // zero-size rect is legal
        PaintEvent zero = new PaintEvent(src, PaintEvent.PAINT, new java.awt.Rectangle(0, 0, 0, 0));
        check(zero.getUpdateRect().width == 0 && zero.getUpdateRect().height == 0, "zero-size rect legal");
        // toString
        check(zero.toString().startsWith("java.awt.event.PaintEvent["), "toString starts with class name");
    }

    @SuppressWarnings("unchecked")
    static <T> T roundTrip(T o) throws java.io.IOException, ClassNotFoundException {
        java.io.ByteArrayOutputStream b = new java.io.ByteArrayOutputStream();
        new java.io.ObjectOutputStream(b).writeObject(o);
        return (T) new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(b.toByteArray())).readObject();
    }
    static void testSerialization() throws Exception {
        java.awt.Component src = new java.awt.Component() { };
        java.awt.event.PaintEvent e = new java.awt.event.PaintEvent(src, java.awt.event.PaintEvent.PAINT, new java.awt.Rectangle(1, 2, 3, 4));
        java.awt.event.PaintEvent back = roundTrip(e);
        check(back.getID() == java.awt.event.PaintEvent.PAINT, "serialization preserves id");
        check(back.getUpdateRect().equals(new java.awt.Rectangle(1, 2, 3, 4)), "serialization preserves update rect");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
