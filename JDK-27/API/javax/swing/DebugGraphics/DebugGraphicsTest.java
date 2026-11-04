/*
 * @test
 * @summary javax.swing.DebugGraphics conformance: type shape + constants + logging pass-through (headless-safe).
 * @run main DebugGraphicsTest
 */

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.swing.DebugGraphics;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;

public class DebugGraphicsTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    static void testShape() throws Exception {
        Class<?> c = Class.forName("javax.swing.DebugGraphics", false, DebugGraphicsTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.awt.Graphics"),
                "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("create"), "method create");
        check(names.contains("setFlashColor"), "method setFlashColor");
        check(names.contains("flashColor"), "method flashColor");
        check(names.contains("setFlashTime"), "method setFlashTime");
    }

    static void testConstants() {
        check(DebugGraphics.NONE_OPTION == -1, "NONE_OPTION == -1");
        check(DebugGraphics.LOG_OPTION == 1, "LOG_OPTION == 1");
        check(DebugGraphics.FLASH_OPTION == 2, "FLASH_OPTION == 2");
        check(DebugGraphics.BUFFERED_OPTION == 4, "BUFFERED_OPTION == 4");
        DebugGraphics.setFlashTime(123);
        check(DebugGraphics.flashTime() == 123, "setFlashTime roundtrip");
        DebugGraphics.setFlashCount(7);
        check(DebugGraphics.flashCount() == 7, "setFlashCount roundtrip");
        Color c = new Color(1, 2, 3);
        DebugGraphics.setFlashColor(c);
        check(DebugGraphics.flashColor() == c, "setFlashColor roundtrip");
        DebugGraphics.setFlashTime(100);
        DebugGraphics.setFlashCount(2);
        DebugGraphics.setFlashColor(Color.red);
    }

    static void testLoggingAndPassthrough() throws Exception {
        PrintStream saved = DebugGraphics.logStream();
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        PrintStream capture = new PrintStream(captured, true);
        DebugGraphics.setLogStream(capture);
        try {
            BufferedImage img = new BufferedImage(20, 20, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = img.createGraphics();
            DebugGraphics dg = new DebugGraphics(g, new JLabel("dbg"));
            dg.setDebugOptions(DebugGraphics.LOG_OPTION);
            dg.drawLine(1, 2, 3, 4);
            String logged = captured.toString();
            check(logged.contains("Drawing line"), "drawLine logged to log stream under LOG_OPTION");
            DebugGraphics child = (DebugGraphics) dg.create();
            check(child != null && child != dg, "create() yields new DebugGraphics");
            child.dispose();
            dg.setColor(Color.BLUE);
            check(dg.getColor() == Color.BLUE, "setColor/getColor passthrough");
            dg.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 10));
            check(dg.getFont() != null, "setFont/getFont passthrough");
            dg.translate(2, 3); // passthrough no-throw
            dg.dispose();
            g.dispose();
        } finally {
            DebugGraphics.setLogStream(saved);
        }
    }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(new MetalLookAndFeel());
        testShape();
        testConstants();
        testLoggingAndPassthrough();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
