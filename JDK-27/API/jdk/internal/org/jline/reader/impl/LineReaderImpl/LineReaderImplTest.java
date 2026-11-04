/*
 * @test
 * @summary jdk.internal.org.jline.reader.impl.LineReaderImpl conformance: internal package reflection.
 * @modules jdk.internal.le/jdk.internal.org.jline.reader.impl:+open
 * @run main LineReaderImplTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LineReaderImplTest {
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
        Class<?> c = Class.forName("jdk.internal.org.jline.reader.impl.LineReaderImpl", false, LineReaderImplTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.org.jline.reader.impl.LineReaderImpl"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Arrays.equals(interfaces(c), new String[]{"java.io.Flushable","jdk.internal.org.jline.reader.LineReader"}), "interfaces");
        check(Arrays.equals(methods(c), new String[]{"acceptAndHold","acceptAndInferNextHistory","acceptLine","acceptLineAndDownHistory","addCommandsInBuffer","argumentBase","backwardChar","backwardDeleteChar","backwardDeleteWord","backwardKillLine","backwardKillWord","backwardWord","beep","beginPaste","beginningOfBufferOrHistory","beginningOfHistory","beginningOfLine","beginningOfLineHist","buildStyle","builtinWidgets","callWidget","capitalizeWord","cleanup","clear","clearChoices","clearScreen","completePrefix","completeWord","computePost","computePost","copyPrevWord","copyRegionAsKill","deactivateRegion","defaultKeyMaps","deleteChar","deleteCharOrList","deleteWord","digitArgument","doCleanup","doComplete","doComplete","doInsertComment","doList","doList","doLowercaseVersion","doMenu","doReadBinding","doReadStringUntil","doSearchHistory","doViDeleteOrChange","doViMatchBracket","downCaseWord","downHistory","downLine","downLineOrHistory","downLineOrSearch","dumb","editAndAddInBuffer","editAndExecute","emacs","emacsBackwardWord","emacsEditingMode","emacsForwardWord","endOfBufferOrHistory","endOfHistory","endOfLine","endOfLineHist","exchangePointAndMark","expandHistory","expandOrComplete","expandOrCompletePrefix","expandWord","finish","finishBuffer","flush","focusIn","focusOut","forwardChar","forwardWord","freshLine","getAppName","getAutosuggestion","getBracketType","getBuffer","getBuiltinWidgets","getCandidateComparator","getCompleter","getCompletionStyle","getCompletionStyleBackground","getCompletionStyleBackground","getCompletionStyleDescription","getCompletionStyleDescription","getCompletionStyleGroup","getCompletionStyleGroup","getCompletionStyleListBackground","getCompletionStyleListDescription","getCompletionStyleListGroup","getCompletionStyleListSelection","getCompletionStyleListStarting","getCompletionStyleSelection","getCompletionStyleSelection","getCompletionStyleStarting","getCompletionStyleStarting","getDisplayedBufferWithPrompts","getExpander","getGroupComparator","getHighlighter","getHistory","getKeyMap","getKeyMaps","getKeys","getLastBinding","getOriginalGroupName","getOthersGroupName","getParsedLine","getParser","getRegionActive","getRegionMark","getSearchTerm","getTabWidth","getTailTip","getTerminal","getVariable","getVariables","getWidget","getWidgets","handleSignal","historyIncrementalSearchBackward","historyIncrementalSearchForward","historySearchBackward","historySearchForward","insertClose","insertCloseCurly","insertCloseParen","insertCloseSquare","insertComment","insertTab","isAlpha","isDelimiter","isInViChangeOperation","isInViCmdMode","isInViMoveOperation","isKeyMap","isReading","isSet","isViAlphaNum","isWhitespace","isWord","killBuffer","killLine","killRegion","killWholeLine","killWord","listChoices","menu","menuComplete","menuExpandOrComplete","mouse","moveHistory","moveHistory","negArgument","nextBindingIsComplete","option","overwriteMode","peekCharacter","printAbove","printAbove","putString","quit","quotedInsert","readBinding","readBinding","readCharacter","readLine","readLine","readLine","readLine","readLine","readLine","readLine","readMouseEvent","redisplay","redisplay","redo","redrawLine","runMacro","safe","searchBackwards","searchBackwards","searchBackwards","searchForwards","searchForwards","searchForwards","selfInsert","selfInsertUnmeta","sendBreak","setAutosuggestion","setBuffer","setBuffer","setCompleter","setCompletionMatcher","setExpander","setHighlighter","setHistory","setKeyMap","setMarkCommand","setOpt","setParser","setPrompt","setRightPrompt","setTailTip","setVariable","switchCase","toColumns","toColumns","transposeChars","transposeWords","undefinedKey","undo","universalArgument","unsetOpt","upCaseWord","upHistory","upLine","upLineOrHistory","upLineOrSearch","variable","viAddEol","viAddNext","viBackwardBlankWord","viBackwardBlankWordEnd","viBackwardChar","viBackwardDeleteChar","viBackwardKillWord","viBackwardWord","viBackwardWordEnd","viBeginningOfLine","viChange","viChange","viChangeEol","viChangeWholeLine","viCmd","viCmdMode","viDelete","viDeleteChangeYankToRemap","viDeleteChar","viDeleteTo","viDigitOrBeginningOfLine","viDownLineOrHistory","viEndOfLine","viFindNextChar","viFindNextCharSkip","viFindPrevChar","viFindPrevCharSkip","viFirstNonBlank","viForwardBlankWord","viForwardBlankWordEnd","viForwardChar","viForwardWord","viForwardWordEnd","viHistorySearchBackward","viHistorySearchForward","viInsert","viInsertBol","viInsertComment","viInsertion","viJoin","viKillEol","viKillWholeLine","viMatchBracket","viOpenLineAbove","viOpenLineBelow","viOpp","viPutAfter","viPutBefore","viRepeatFind","viRepeatSearch","viReplaceChars","viRevRepeatFind","viRevRepeatSearch","viSwapCase","viUpLineOrHistory","viYankTo","viYankTo","viYankWholeLine","visual","visualLineMode","visualMode","whatCursorPosition","wrap","yank","yankPop","zeroOut"}), "methods");
        check(Arrays.equals(fields(c), new String[]{"BRACKETED_PASTE_BEGIN","BRACKETED_PASTE_END","BRACKETED_PASTE_OFF","BRACKETED_PASTE_ON","DEFAULT_AMBIGUOUS_BINDING","DEFAULT_BELL_STYLE","DEFAULT_BLINK_MATCHING_PAREN","DEFAULT_COMMENT_BEGIN","DEFAULT_COMPLETION_STYLE_BACKGROUND","DEFAULT_COMPLETION_STYLE_DESCRIPTION","DEFAULT_COMPLETION_STYLE_GROUP","DEFAULT_COMPLETION_STYLE_LIST_BACKGROUND","DEFAULT_COMPLETION_STYLE_LIST_DESCRIPTION","DEFAULT_COMPLETION_STYLE_LIST_GROUP","DEFAULT_COMPLETION_STYLE_LIST_SELECTION","DEFAULT_COMPLETION_STYLE_LIST_STARTING","DEFAULT_COMPLETION_STYLE_SELECTION","DEFAULT_COMPLETION_STYLE_STARTING","DEFAULT_ERRORS","DEFAULT_FEATURES_MAX_BUFFER_SIZE","DEFAULT_INDENTATION","DEFAULT_LIST_MAX","DEFAULT_MAX_REPEAT_COUNT","DEFAULT_MENU_LIST_MAX","DEFAULT_ORIGINAL_GROUP_NAME","DEFAULT_OTHERS_GROUP_NAME","DEFAULT_REMOVE_SUFFIX_CHARS","DEFAULT_SEARCH_TERMINATORS","DEFAULT_SECONDARY_PROMPT_PATTERN","DEFAULT_SUGGESTIONS_MIN_BUFFER_SIZE","DEFAULT_SYSTEM_PROPERTY_PREFIX","DEFAULT_TAB_WIDTH","DEFAULT_WORDCHARS","FOCUS_IN_SEQ","FOCUS_OUT_SEQ","NULL_MASK","TAB_WIDTH","alternateIn","alternateOut","appName","autosuggestion","bindingReader","buf","builtinWidgets","candidateStartPosition","commandsBuffer","completer","completionMatcher","count","display","doAutosuggestion","expander","findChar","findDir","findTailAdd","highlighter","history","historyBuffer","isArgDigit","isUndo","keyMap","keyMaps","killRing","lock","maskingCallback","modifiedHistory","mult","nextCommandFromHistory","nextHistoryId","options","overTyping","parsedLine","parser","post","prompt","reading","regionActive","regionMark","repeatCount","rightPrompt","searchBackward","searchBuffer","searchFailing","searchIndex","searchTerm","size","skipRedisplay","smallTerminalOffset","startedReading","state","tailTip","terminal","undo","universal","variables","viMoveMode","widgets","yankBuffer"}), "fields");
        check(Arrays.equals(enumConstants(c), new String[]{}), "enumConstants");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
