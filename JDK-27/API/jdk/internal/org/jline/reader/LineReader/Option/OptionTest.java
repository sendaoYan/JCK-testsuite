/*
 * @test
 * @summary jdk.internal.org.jline.reader.LineReader.Option conformance: internal package reflection.
 * @modules jdk.internal.le/jdk.internal.org.jline.reader:+open
 * @run main OptionTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OptionTest {
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
        Class<?> c = Class.forName("jdk.internal.org.jline.reader.LineReader$Option", false, OptionTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.org.jline.reader.LineReader$Option"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == true, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(Arrays.equals(interfaces(c), new String[]{}), "interfaces");
        check(Arrays.equals(methods(c), new String[]{"isDef","isSet","valueOf","values"}), "methods");
        check(Arrays.equals(fields(c), new String[]{"AUTO_FRESH_LINE","AUTO_GROUP","AUTO_LIST","AUTO_MENU","AUTO_MENU_LIST","AUTO_PARAM_SLASH","AUTO_REMOVE_SLASH","BRACKETED_PASTE","CASE_INSENSITIVE","CASE_INSENSITIVE_SEARCH","COMPLETE_IN_WORD","COMPLETE_MATCHER_CAMELCASE","COMPLETE_MATCHER_TYPO","DELAY_LINE_WRAP","DISABLE_EVENT_EXPANSION","DISABLE_HIGHLIGHTER","DISABLE_UNDO","EMPTY_WORD_OPTIONS","ERASE_LINE_ON_FINISH","GLOB_COMPLETE","GROUP","GROUP_PERSIST","HISTORY_BEEP","HISTORY_IGNORE_DUPS","HISTORY_IGNORE_SPACE","HISTORY_INCREMENTAL","HISTORY_REDUCE_BLANKS","HISTORY_TIMESTAMPED","HISTORY_VERIFY","INSERT_BRACKET","INSERT_TAB","LIST_AMBIGUOUS","LIST_PACKED","LIST_ROWS_FIRST","MENU_COMPLETE","MOUSE","RECOGNIZE_EXACT","USE_FORWARD_SLASH"}), "fields");
        check(Arrays.equals(enumConstants(c), new String[]{"AUTO_FRESH_LINE","AUTO_GROUP","AUTO_LIST","AUTO_MENU","AUTO_MENU_LIST","AUTO_PARAM_SLASH","AUTO_REMOVE_SLASH","BRACKETED_PASTE","CASE_INSENSITIVE","CASE_INSENSITIVE_SEARCH","COMPLETE_IN_WORD","COMPLETE_MATCHER_CAMELCASE","COMPLETE_MATCHER_TYPO","DELAY_LINE_WRAP","DISABLE_EVENT_EXPANSION","DISABLE_HIGHLIGHTER","DISABLE_UNDO","EMPTY_WORD_OPTIONS","ERASE_LINE_ON_FINISH","GLOB_COMPLETE","GROUP","GROUP_PERSIST","HISTORY_BEEP","HISTORY_IGNORE_DUPS","HISTORY_IGNORE_SPACE","HISTORY_INCREMENTAL","HISTORY_REDUCE_BLANKS","HISTORY_TIMESTAMPED","HISTORY_VERIFY","INSERT_BRACKET","INSERT_TAB","LIST_AMBIGUOUS","LIST_PACKED","LIST_ROWS_FIRST","MENU_COMPLETE","MOUSE","RECOGNIZE_EXACT","USE_FORWARD_SLASH"}), "enumConstants");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
