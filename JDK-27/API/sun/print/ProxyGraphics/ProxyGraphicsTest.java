/*
 * @test
 * @summary sun.print.ProxyGraphics conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.print:+open
 * @run main ProxyGraphicsTest
 */

import java.util.*;
import java.lang.reflect.*;

public class ProxyGraphicsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.print.ProxyGraphics", false, ProxyGraphicsTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.print.ProxyGraphics".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.awt.Graphics".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("#java.awt.Graphics#getGraphics();public#boolean#drawImage(java.awt.Image,int,int,int,int,int,int,int,int,java.awt.Color,java.awt.image.ImageObserver);public#boolean#drawImage(java.awt.Image,int,int,int,int,int,int,int,int,java.awt.image.ImageObserver);public#boolean#drawImage(java.awt.Image,int,int,int,int,java.awt.Color,java.awt.image.ImageObserver);public#boolean#drawImage(java.awt.Image,int,int,int,int,java.awt.image.ImageObserver);public#boolean#drawImage(java.awt.Image,int,int,java.awt.Color,java.awt.image.ImageObserver);public#boolean#drawImage(java.awt.Image,int,int,java.awt.image.ImageObserver);public#boolean#hitClip(int,int,int,int);public#java.awt.Color#getColor();public#java.awt.Font#getFont();public#java.awt.FontMetrics#getFontMetrics();public#java.awt.FontMetrics#getFontMetrics(java.awt.Font);public#java.awt.Graphics#create();public#java.awt.Graphics#create(int,int,int,int);public#java.awt.Rectangle#getClipBounds();public#java.awt.Rectangle#getClipBounds(java.awt.Rectangle);public#java.awt.Rectangle#getClipRect();public#java.awt.Shape#getClip();public#java.lang.String#toString();public#void#clearRect(int,int,int,int);public#void#clipRect(int,int,int,int);public#void#copyArea(int,int,int,int,int,int);public#void#dispose();public#void#draw3DRect(int,int,int,int,boolean);public#void#drawArc(int,int,int,int,int,int);public#void#drawBytes([B,int,int,int,int);public#void#drawChars([C,int,int,int,int);public#void#drawLine(int,int,int,int);public#void#drawOval(int,int,int,int);public#void#drawPolygon([I,[I,int);public#void#drawPolygon(java.awt.Polygon);public#void#drawPolyline([I,[I,int);public#void#drawRect(int,int,int,int);public#void#drawRoundRect(int,int,int,int,int,int);public#void#drawString(java.lang.String,int,int);public#void#drawString(java.text.AttributedCharacterIterator,int,int);public#void#fill3DRect(int,int,int,int,boolean);public#void#fillArc(int,int,int,int,int,int);public#void#fillOval(int,int,int,int);public#void#fillPolygon([I,[I,int);public#void#fillPolygon(java.awt.Polygon);public#void#fillRect(int,int,int,int);public#void#fillRoundRect(int,int,int,int,int,int);public#void#finalize();public#void#setClip(int,int,int,int);public#void#setClip(java.awt.Shape);public#void#setColor(java.awt.Color);public#void#setFont(java.awt.Font);public#void#setPaintMode();public#void#setXORMode(java.awt.Color);public#void#translate(int,int)".equals(methods(c)), "methods");
        check("private#java.awt.Graphics#g".equals(fields(c)), "fields");
        check("".equals(enumConstants(c)), "enumConstants");
        check(c.getDeclaredConstructors().length == 1, "ctors");
        System.out.println("PASS (" + passed + " assertions)");
    }

    static String interfaces(Class<?> c) {
        List<String> l = new ArrayList<>();
        for (Class<?> i : c.getInterfaces()) l.add(i.getName());
        Collections.sort(l);
        return String.join(";", l);
    }

    static String methods(Class<?> c) {
        List<String> l = new ArrayList<>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (m.isSynthetic()) continue;
            StringBuilder s = new StringBuilder();
            s.append(java.lang.reflect.Modifier.toString(m.getModifiers())).append('#');
            s.append(m.getReturnType().getName()).append('#');
            s.append(m.getName()).append('(');
            Class<?>[] ps = m.getParameterTypes();
            for (int i = 0; i < ps.length; i++) { if (i > 0) s.append(','); s.append(ps[i].getName()); }
            s.append(')');
            l.add(s.toString());
        }
        Collections.sort(l);
        return String.join(";", l);
    }

    static String fields(Class<?> c) {
        List<String> l = new ArrayList<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) {
            if (f.isSynthetic()) continue;
            l.add(java.lang.reflect.Modifier.toString(f.getModifiers()) + '#' + f.getType().getName() + '#' + f.getName());
        }
        Collections.sort(l);
        return String.join(";", l);
    }

    static String enumConstants(Class<?> c) {
        if (!c.isEnum()) return "";
        List<String> l = new ArrayList<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) {
            if (f.isEnumConstant()) l.add(f.getName());
        }
        Collections.sort(l);
        return String.join(";", l);
    }
}
