/*
 * @test
 * @summary sun.awt.X11.XCursorFontConstants conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.awt.X11:+open
 * @run main XCursorFontConstantsTest
 */

import java.util.*;
import java.lang.reflect.*;

public class XCursorFontConstantsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.X11.XCursorFontConstants", false, XCursorFontConstantsTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.awt.X11.XCursorFontConstants".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("".equals(methods(c)), "methods");
        check("static final#int#XC_X_cursor;static final#int#XC_arrow;static final#int#XC_based_arrow_down;static final#int#XC_based_arrow_up;static final#int#XC_boat;static final#int#XC_bogosity;static final#int#XC_bottom_left_corner;static final#int#XC_bottom_right_corner;static final#int#XC_bottom_side;static final#int#XC_bottom_tee;static final#int#XC_box_spiral;static final#int#XC_center_ptr;static final#int#XC_circle;static final#int#XC_clock;static final#int#XC_coffee_mug;static final#int#XC_cross;static final#int#XC_cross_reverse;static final#int#XC_crosshair;static final#int#XC_diamond_cross;static final#int#XC_dot;static final#int#XC_dotbox;static final#int#XC_double_arrow;static final#int#XC_draft_large;static final#int#XC_draft_small;static final#int#XC_draped_box;static final#int#XC_exchange;static final#int#XC_fleur;static final#int#XC_gobbler;static final#int#XC_gumby;static final#int#XC_hand1;static final#int#XC_hand2;static final#int#XC_heart;static final#int#XC_icon;static final#int#XC_iron_cross;static final#int#XC_left_ptr;static final#int#XC_left_side;static final#int#XC_left_tee;static final#int#XC_leftbutton;static final#int#XC_ll_angle;static final#int#XC_lr_angle;static final#int#XC_man;static final#int#XC_middlebutton;static final#int#XC_mouse;static final#int#XC_num_glyphs;static final#int#XC_pencil;static final#int#XC_pirate;static final#int#XC_plus;static final#int#XC_question_arrow;static final#int#XC_right_ptr;static final#int#XC_right_side;static final#int#XC_right_tee;static final#int#XC_rightbutton;static final#int#XC_rtl_logo;static final#int#XC_sailboat;static final#int#XC_sb_down_arrow;static final#int#XC_sb_h_double_arrow;static final#int#XC_sb_left_arrow;static final#int#XC_sb_right_arrow;static final#int#XC_sb_up_arrow;static final#int#XC_sb_v_double_arrow;static final#int#XC_shuttle;static final#int#XC_sizing;static final#int#XC_spider;static final#int#XC_spraycan;static final#int#XC_star;static final#int#XC_target;static final#int#XC_tcross;static final#int#XC_top_left_arrow;static final#int#XC_top_left_corner;static final#int#XC_top_right_corner;static final#int#XC_top_side;static final#int#XC_top_tee;static final#int#XC_trek;static final#int#XC_ul_angle;static final#int#XC_umbrella;static final#int#XC_ur_angle;static final#int#XC_watch;static final#int#XC_xterm".equals(fields(c)), "fields");
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
