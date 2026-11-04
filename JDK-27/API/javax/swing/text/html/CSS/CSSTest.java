/*
 * @test
 * @summary javax.swing.text.html.CSS conformance: attribute factory, defaults,
 *          inheritance flags and serialization (headless-safe).
 * @run main CSSTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.swing.text.html.CSS;

public class CSSTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    // ---- Part 1: shape via reflection (kept from the structural pass) ----
    static void testShape() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.CSS", false, CSSTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(hasInterface(c, "java.io.Serializable"), "implements java.io.Serializable");
        check(Modifier.isPublic(c.getModifiers()) == true, "public modifier");
        check(Modifier.isProtected(c.getModifiers()) == false, "protected modifier");
        check(c.getInterfaces().length == 1, "one interface");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getAllAttributeKeys"), "method getAllAttributeKeys");
        check(names.contains("getAttribute"), "method getAttribute");
    }

    // ---- Part 2: attribute key factory ----
    static void testAttributeFactory() {
        CSS.Attribute[] all = CSS.getAllAttributeKeys();
        check(all != null && all.length == 67, "getAllAttributeKeys length 67");

        check(CSS.getAttribute("color") == CSS.Attribute.COLOR, "getAttribute(color) cached constant");
        check(CSS.getAttribute("font-size") == CSS.Attribute.FONT_SIZE, "getAttribute(font-size)");
        check(CSS.getAttribute("background-color") == CSS.Attribute.BACKGROUND_COLOR, "getAttribute(background-color)");
        check(CSS.getAttribute("list-style-type") == CSS.Attribute.LIST_STYLE_TYPE, "getAttribute(list-style-type)");
        check(CSS.getAttribute("zzz-no-such") == null, "getAttribute(unknown) null");
        check(CSS.getAttribute("") == null, "getAttribute(empty) null");
        boolean npe = false;
        try { CSS.getAttribute(null); } catch (NullPointerException e) { npe = true; }
        check(npe, "getAttribute(null) NPE");

        // repeated lookups are stable instances
        CSS.Attribute c1 = CSS.getAttribute("color");
        CSS.Attribute c2 = CSS.getAttribute("color");
        check(c1 == c2, "lookup stable instance");

        check("color".equals(CSS.Attribute.COLOR.toString()), "COLOR toString");
        check("font-size".equals(CSS.Attribute.FONT_SIZE.toString()), "FONT_SIZE toString");
        check("background-color".equals(CSS.Attribute.BACKGROUND_COLOR.toString()), "BACKGROUND_COLOR toString");
    }

    // ---- Part 3: default values and inheritance (observed on both JDKs) ----
    static void testDefaultsAndInheritance() {
        // defaults identical on both releases
        String[][] sameDefaults = {
            {"color", "black"},
            {"font-size", "medium"},
            {"font-style", "normal"},
            {"font-weight", "normal"},
            {"font-variant", "normal"},
            {"background-color", "transparent"},
            {"background-repeat", "repeat"},
            {"background-attachment", "scroll"},
            {"background-image", "none"},
            {"border-style", "none"},
            {"border-width", "medium"},
            {"border-bottom-width", "medium"},
            {"clear", "none"},
            {"display", "block"},
            {"float", "none"},
            {"height", "auto"},
            {"width", "auto"},
            {"list-style-type", "disc"},
            {"list-style-position", "outside"},
            {"list-style-image", "none"},
            {"margin-bottom", "0"},
            {"margin-top", "0"},
            {"padding-left", "0"},
            {"padding-top", "0"},
            {"text-decoration", "none"},
            {"text-transform", "none"},
            {"vertical-align", "baseline"},
            {"white-space", "normal"},
            {"letter-spacing", "normal"},
            {"line-height", "normal"},
            {"word-spacing", "normal"},
            {"margin-left-ltr", "-2147483648"},
            {"margin-right-rtl", "-2147483648"},
        };
        for (String[] d : sameDefaults) {
            CSS.Attribute a = CSS.getAttribute(d[0]);
            check(a != null, "default probe key " + d[0]);
            if (a != null) check(d[1].equals(a.getDefaultValue()), "default " + d[0] + "=" + d[1]);
        }
        // null defaults (unset shorthand properties)
        String[] nullDefaults = {"background", "border", "font", "margin", "padding", "text-align"};
        for (String n : nullDefaults) {
            CSS.Attribute a = CSS.getAttribute(n);
            if (a != null) check(a.getDefaultValue() == null, "default " + n + " null");
        }
        // version difference: background-position default
        CSS.Attribute bp = CSS.getAttribute("background-position");
        if (bp != null) {
            if (System.getProperty("java.version").startsWith("1.8")) {
                check(bp.getDefaultValue() == null, "JDK-8 background-position default null");
            } else {
                check("0% 0%".equals(bp.getDefaultValue()), "JDK-9+ background-position default 0% 0%");
            }
        }

        String[][] inhRows = {
            {"true", "color"}, {"true", "font-family"}, {"true", "font-size"},
            {"true", "font-style"}, {"true", "font-weight"}, {"true", "line-height"},
            {"true", "list-style-type"}, {"true", "list-style-position"},
            {"true", "text-align"}, {"true", "text-indent"}, {"true", "text-decoration"},
            {"true", "white-space"}, {"true", "word-spacing"}, {"true", "letter-spacing"},
            {"false", "width"}, {"false", "height"}, {"false", "margin-top"},
            {"false", "margin-bottom"}, {"false", "padding-top"}, {"false", "padding-left"},
            {"false", "background-color"}, {"false", "background-image"},
            {"false", "border-style"}, {"false", "border-width"},
            {"false", "display"}, {"false", "float"}, {"false", "vertical-align"},
            {"false", "margin-left-ltr"}, {"false", "margin-right-rtl"},
        };
        for (String[] row : inhRows) {
            CSS.Attribute a = CSS.getAttribute(row[1]);
            check(a != null, "inheritance probe key " + row[1]);
            if (a != null) check(a.isInherited() == row[0].equals("true"), "isInherited " + row[1]);
        }
    }

    // ---- Part 4: serialization ----
    static void testSerialization() throws Exception {
        long svu = ObjectStreamClass.lookup(CSS.class).getSerialVersionUID();
        if (System.getProperty("java.version").startsWith("1.8")) {
            check(svu == 3482914137475155648L, "JDK-8 CSS serialVersionUID");
        } else {
            check(svu == -9037853862581993409L, "JDK-9+ CSS serialVersionUID");
        }

        CSS css = new CSS();
        check(css != null, "new CSS()");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(css);
        oos.close();
        Object back = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())).readObject();
        check(back instanceof CSS, "CSS round-trip class");
        check(back != css, "round-trip distinct instance");
    }

    public static void main(String[] args) throws Exception {
        testShape();
        testAttributeFactory();
        testDefaultsAndInheritance();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
