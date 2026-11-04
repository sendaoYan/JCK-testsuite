/*
 * @test
 * @summary jdk.internal.org.jline.reader.LineReader conformance: internal package reflection.
 * @modules jdk.internal.le/jdk.internal.org.jline.reader:+open
 * @run main LineReaderTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LineReaderTest {
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
        Class<?> c = Class.forName("jdk.internal.org.jline.reader.LineReader", false, LineReaderTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.org.jline.reader.LineReader"), "name");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "superclass");
        check(Arrays.equals(interfaces(c), new String[]{}), "interfaces");
        check(Arrays.equals(methods(c), new String[]{"addCommandsInBuffer","callWidget","defaultKeyMaps","editAndAddInBuffer","editAndAddInBuffer","getAppName","getAutosuggestion","getBuffer","getBuiltinWidgets","getExpander","getHighlighter","getHistory","getKeyMap","getKeyMaps","getKeys","getLastBinding","getParsedLine","getParser","getRegionActive","getRegionMark","getSearchTerm","getTailTip","getTerminal","getVariable","getVariables","getWidgets","isReading","isSet","option","printAbove","printAbove","readLine","readLine","readLine","readLine","readLine","readLine","readLine","readMouseEvent","runMacro","setAutosuggestion","setKeyMap","setOpt","setTailTip","setVariable","unsetOpt","variable","zeroOut"}), "methods");
        check(Arrays.equals(fields(c), new String[]{"ACCEPT_AND_HOLD","ACCEPT_AND_INFER_NEXT_HISTORY","ACCEPT_LINE","ACCEPT_LINE_AND_DOWN_HISTORY","AMBIGUOUS_BINDING","ARGUMENT_BASE","BACKWARD_CHAR","BACKWARD_DELETE_CHAR","BACKWARD_DELETE_WORD","BACKWARD_KILL_LINE","BACKWARD_KILL_WORD","BACKWARD_WORD","BEEP","BEGINNING_OF_BUFFER_OR_HISTORY","BEGINNING_OF_HISTORY","BEGINNING_OF_LINE","BEGINNING_OF_LINE_HIST","BEGIN_PASTE","BELL_STYLE","BIND_TTY_SPECIAL_CHARS","BLINK_MATCHING_PAREN","CALLBACK_FINISH","CALLBACK_INIT","CALLBACK_KEYMAP","CAPITALIZE_WORD","CHARACTER_SEARCH","CHARACTER_SEARCH_BACKWARD","CLEAR","CLEAR_SCREEN","COMMENT_BEGIN","COMPLETE_PREFIX","COMPLETE_WORD","COMPLETION_STYLE_BACKGROUND","COMPLETION_STYLE_DESCRIPTION","COMPLETION_STYLE_GROUP","COMPLETION_STYLE_LIST_BACKGROUND","COMPLETION_STYLE_LIST_DESCRIPTION","COMPLETION_STYLE_LIST_GROUP","COMPLETION_STYLE_LIST_SELECTION","COMPLETION_STYLE_LIST_STARTING","COMPLETION_STYLE_SELECTION","COMPLETION_STYLE_STARTING","COPY_PREV_WORD","COPY_REGION_AS_KILL","DELETE_CHAR","DELETE_CHAR_OR_LIST","DELETE_WORD","DIGIT_ARGUMENT","DISABLE_COMPLETION","DISABLE_HISTORY","DOWN_CASE_WORD","DOWN_HISTORY","DOWN_LINE","DOWN_LINE_OR_HISTORY","DOWN_LINE_OR_SEARCH","DO_LOWERCASE_VERSION","DUMB","EDITING_MODE","EDIT_AND_EXECUTE_COMMAND","EMACS","EMACS_BACKWARD_WORD","EMACS_EDITING_MODE","EMACS_FORWARD_WORD","END_OF_BUFFER_OR_HISTORY","END_OF_HISTORY","END_OF_LINE","END_OF_LINE_HIST","ERRORS","EXCHANGE_POINT_AND_MARK","EXECUTE_NAMED_CMD","EXPAND_HISTORY","EXPAND_OR_COMPLETE","EXPAND_OR_COMPLETE_PREFIX","EXPAND_WORD","FEATURES_MAX_BUFFER_SIZE","FOCUS_IN","FOCUS_OUT","FORWARD_CHAR","FORWARD_WORD","FRESH_LINE","HISTORY_BEGINNING_SEARCH_BACKWARD","HISTORY_BEGINNING_SEARCH_FORWARD","HISTORY_FILE","HISTORY_FILE_SIZE","HISTORY_IGNORE","HISTORY_INCREMENTAL_PATTERN_SEARCH_BACKWARD","HISTORY_INCREMENTAL_PATTERN_SEARCH_FORWARD","HISTORY_INCREMENTAL_SEARCH_BACKWARD","HISTORY_INCREMENTAL_SEARCH_FORWARD","HISTORY_SEARCH_BACKWARD","HISTORY_SEARCH_FORWARD","HISTORY_SIZE","INDENTATION","INFER_NEXT_HISTORY","INPUT_RC_FILE_NAME","INSERT_CLOSE_CURLY","INSERT_CLOSE_PAREN","INSERT_CLOSE_SQUARE","INSERT_COMMENT","INSERT_LAST_WORD","KEYMAP","KILL_BUFFER","KILL_LINE","KILL_REGION","KILL_WHOLE_LINE","KILL_WORD","LINE_OFFSET","LIST_CHOICES","LIST_EXPAND","LIST_MAX","MAGIC_SPACE","MAIN","MAX_REPEAT_COUNT","MENU","MENU_COMPLETE","MENU_EXPAND_OR_COMPLETE","MENU_LIST_MAX","MENU_SELECT","MOUSE","NEG_ARGUMENT","ORIGINAL_GROUP_NAME","OTHERS_GROUP_NAME","OVERWRITE_MODE","PREFER_VISIBLE_BELL","PROP_SUPPORT_PARSEDLINE","PUT_REPLACE_SELECTION","QUOTED_INSERT","READ_COMMAND","RECURSIVE_EDIT","REDISPLAY","REDO","REDRAW_LINE","REMOVE_SUFFIX_CHARS","REVERSE_MENU_COMPLETE","SAFE","SEARCH_TERMINATORS","SECONDARY_PROMPT_PATTERN","SELF_INSERT","SELF_INSERT_UNMETA","SEND_BREAK","SET_LOCAL_HISTORY","SET_MARK_COMMAND","SPELL_WORD","SPLIT_UNDO","SUGGESTIONS_MIN_BUFFER_SIZE","SYSTEM_PROPERTY_PREFIX","TAB_WIDTH","TRANSPOSE_CHARS","TRANSPOSE_WORDS","UNDEFINED_KEY","UNDO","UNIVERSAL_ARGUMENT","UP_CASE_WORD","UP_HISTORY","UP_LINE","UP_LINE_OR_HISTORY","UP_LINE_OR_SEARCH","VICMD","VIINS","VIOPP","VISUAL","VISUAL_LINE_MODE","VISUAL_MODE","VI_ADD_EOL","VI_ADD_NEXT","VI_BACKWARD_BLANK_WORD","VI_BACKWARD_BLANK_WORD_END","VI_BACKWARD_CHAR","VI_BACKWARD_DELETE_CHAR","VI_BACKWARD_KILL_WORD","VI_BACKWARD_WORD","VI_BACKWARD_WORD_END","VI_BEGINNING_OF_LINE","VI_CHANGE","VI_CHANGE_EOL","VI_CHANGE_WHOLE_LINE","VI_CMD_MODE","VI_DELETE","VI_DELETE_CHAR","VI_DIGIT_OR_BEGINNING_OF_LINE","VI_DOWN_LINE_OR_HISTORY","VI_END_OF_LINE","VI_FETCH_HISTORY","VI_FIND_NEXT_CHAR","VI_FIND_NEXT_CHAR_SKIP","VI_FIND_PREV_CHAR","VI_FIND_PREV_CHAR_SKIP","VI_FIRST_NON_BLANK","VI_FORWARD_BLANK_WORD","VI_FORWARD_BLANK_WORD_END","VI_FORWARD_CHAR","VI_FORWARD_WORD","VI_FORWARD_WORD_END","VI_GOTO_COLUMN","VI_HISTORY_SEARCH_BACKWARD","VI_HISTORY_SEARCH_FORWARD","VI_INSERT","VI_INSERT_BOL","VI_INSERT_COMMENT","VI_JOIN","VI_KILL_EOL","VI_KILL_LINE","VI_MATCH_BRACKET","VI_OPEN_LINE_ABOVE","VI_OPEN_LINE_BELOW","VI_OPER_SWAP_CASE","VI_PUT_AFTER","VI_PUT_BEFORE","VI_QUOTED_INSERT","VI_REPEAT_CHANGE","VI_REPEAT_FIND","VI_REPEAT_SEARCH","VI_REPLACE","VI_REPLACE_CHARS","VI_REV_REPEAT_FIND","VI_REV_REPEAT_SEARCH","VI_SET_BUFFER","VI_SUBSTITUTE","VI_SWAP_CASE","VI_UNDO_CHANGE","VI_UP_LINE_OR_HISTORY","VI_YANK","VI_YANK_EOL","VI_YANK_WHOLE_LINE","WHAT_CURSOR_POSITION","WORDCHARS","YANK","YANK_POP"}), "fields");
        check(Arrays.equals(enumConstants(c), new String[]{}), "enumConstants");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
