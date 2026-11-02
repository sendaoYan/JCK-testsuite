/*
 * @test
 * @summary jdk.internal.org.jline.utils.AnsiWriter conformance: internal package reflection.
 * @modules jdk.internal.le/jdk.internal.org.jline.utils:+open
 * @run main AnsiWriterTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AnsiWriterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static String[] methods(Class<?> c) {
        List<String> l = new ArrayList<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) l.add(m.getName());
        }
        String[] a = l.toArray(new String[0]); Arrays.sort(a); return a;
    }

    static String[] fields(Class<?> c) {
        List<String> l = new ArrayList<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) l.add(f.getName());
        }
        String[] a = l.toArray(new String[0]); Arrays.sort(a); return a;
    }

    static String[] enumConstants(Class<?> c) {
        List<String> l = new ArrayList<>();
        for (Field f : c.getDeclaredFields()) {
            if (f.isEnumConstant()) l.add(f.getName());
        }
        String[] a = l.toArray(new String[0]); Arrays.sort(a); return a;
    }

    static String[] interfaces(Class<?> c) {
        List<String> l = new ArrayList<>();
        for (Class<?> i : c.getInterfaces()) l.add(i.getName());
        String[] a = l.toArray(new String[0]); Arrays.sort(a); return a;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.org.jline.utils.AnsiWriter", false, AnsiWriterTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.org.jline.utils.AnsiWriter"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass().getName().equals("java.io.FilterWriter"), "superclass");
        check(Arrays.equals(interfaces(c), new String[]{}), "interfaces");
        check(Arrays.equals(methods(c), new String[]{"close","processAttributeRest","processChangeIconName","processChangeIconNameAndWindowTitle","processChangeWindowTitle","processCharsetSelect","processCursorDown","processCursorDownLine","processCursorLeft","processCursorRight","processCursorTo","processCursorToColumn","processCursorUp","processCursorUpLine","processDefaultBackgroundColor","processDefaultTextColor","processDeleteLine","processEraseLine","processEraseScreen","processInsertLine","processRestoreCursorPosition","processSaveCursorPosition","processScrollDown","processScrollUp","processSetAttribute","processSetBackgroundColor","processSetBackgroundColor","processSetBackgroundColorExt","processSetBackgroundColorExt","processSetForegroundColor","processSetForegroundColor","processSetForegroundColorExt","processSetForegroundColorExt","processUnknownExtension","processUnknownOperatingSystemCommand","write","write","write"}), "methods");
        check(Arrays.equals(fields(c), new String[]{"ATTRIBUTE_BLINK_FAST","ATTRIBUTE_BLINK_OFF","ATTRIBUTE_BLINK_SLOW","ATTRIBUTE_CONCEAL_OFF","ATTRIBUTE_CONCEAL_ON","ATTRIBUTE_INTENSITY_BOLD","ATTRIBUTE_INTENSITY_FAINT","ATTRIBUTE_INTENSITY_NORMAL","ATTRIBUTE_ITALIC","ATTRIBUTE_NEGATIVE_OFF","ATTRIBUTE_NEGATIVE_ON","ATTRIBUTE_NEGATIVE_Off","ATTRIBUTE_UNDERLINE","ATTRIBUTE_UNDERLINE_DOUBLE","ATTRIBUTE_UNDERLINE_OFF","BLACK","BLUE","CYAN","ERASE_LINE","ERASE_LINE_TO_BEGINING","ERASE_LINE_TO_END","ERASE_SCREEN","ERASE_SCREEN_TO_BEGINING","ERASE_SCREEN_TO_END","GREEN","MAGENTA","RED","WHITE","YELLOW"}), "fields");
        check(Arrays.equals(enumConstants(c), new String[]{}), "enumConstants");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
