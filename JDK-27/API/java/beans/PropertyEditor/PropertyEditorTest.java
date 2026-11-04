/*
 * @test
 * @summary java.beans.PropertyEditor conformance: exact 12-method interface inventory with
 *          signatures, behavior contract executed against PropertyEditorSupport and the
 *          default Integer/Boolean/String/enum editors found via PropertyEditorManager:
 *          setValue/getValue identity, getAsText/setAsText round-trips, decode-based
 *          parsing (NumberFormatException), getJavaInitializationString formats
 *          (Number "42"/"null", String quoting and escapes), Boolean True/False tags,
 *          enum tags and value validation, editor instance independence, paint/custom
 *          editor defaults, PropertyChangeListener fire/remove semantics.
 * @run main PropertyEditorTest
 */

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.beans.PropertyEditorSupport;

public class PropertyEditorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface ThrowingRunnable { void run() throws Throwable; }
    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String m) {
        try { r.run(); }
        catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(m + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(m + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    static java.lang.reflect.Method method(Class<?> c, String name) throws Exception {
        return c.getDeclaredMethod(name, parameterTypesFor(name));
    }
    static Class<?>[] parameterTypesFor(String name) throws Exception {
        if (name.equals("setValue")) return new Class<?>[]{Object.class};
        if (name.equals("getValue")) return new Class<?>[0];
        if (name.equals("isPaintable")) return new Class<?>[0];
        if (name.equals("paintValue")) return new Class<?>[]{java.awt.Graphics.class, java.awt.Rectangle.class};
        if (name.equals("getJavaInitializationString")) return new Class<?>[0];
        if (name.equals("getAsText")) return new Class<?>[0];
        if (name.equals("setAsText")) return new Class<?>[]{String.class};
        if (name.equals("getTags")) return new Class<?>[0];
        if (name.equals("getCustomEditor")) return new Class<?>[0];
        if (name.equals("supportsCustomEditor")) return new Class<?>[0];
        if (name.equals("addPropertyChangeListener")) return new Class<?>[]{PropertyChangeListener.class};
        if (name.equals("removePropertyChangeListener")) return new Class<?>[]{PropertyChangeListener.class};
        throw new IllegalArgumentException(name);
    }

    static void testInterfaceInventory() throws Exception {
        Class<?> c = Class.forName("java.beans.PropertyEditor");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(c.getDeclaredMethods().length == 12, "exactly 12 methods, got " + c.getDeclaredMethods().length);
        check(hasMethod(c, "addPropertyChangeListener"), "declares addPropertyChangeListener");
        check(hasMethod(c, "removePropertyChangeListener"), "declares removePropertyChangeListener");
        check(hasMethod(c, "getAsText"), "declares getAsText");
        check(hasMethod(c, "setAsText"), "declares setAsText");
        check(hasMethod(c, "getCustomEditor"), "declares getCustomEditor");
        check(hasMethod(c, "getJavaInitializationString"), "declares getJavaInitializationString");
        check(hasMethod(c, "getTags"), "declares getTags");
        check(hasMethod(c, "getValue"), "declares getValue");
        check(hasMethod(c, "setValue"), "declares setValue");
        check(hasMethod(c, "isPaintable"), "declares isPaintable");
        check(hasMethod(c, "paintValue"), "declares paintValue");
        check(hasMethod(c, "supportsCustomEditor"), "declares supportsCustomEditor");
        // return types
        check(method(c, "getValue").getReturnType() == Object.class, "getValue returns Object");
        check(method(c, "setValue").getReturnType() == void.class, "setValue returns void");
        check(method(c, "getAsText").getReturnType() == String.class, "getAsText returns String");
        check(method(c, "setAsText").getReturnType() == void.class, "setAsText returns void");
        check(method(c, "getJavaInitializationString").getReturnType() == String.class,
                "getJavaInitializationString returns String");
        check(method(c, "getTags").getReturnType() == String[].class, "getTags returns String[]");
        check(method(c, "isPaintable").getReturnType() == boolean.class, "isPaintable returns boolean");
        check(method(c, "supportsCustomEditor").getReturnType() == boolean.class,
                "supportsCustomEditor returns boolean");
        check(method(c, "getCustomEditor").getReturnType() == java.awt.Component.class,
                "getCustomEditor returns Component");
        check(method(c, "paintValue").getReturnType() == void.class, "paintValue returns void");
        check(method(c, "addPropertyChangeListener").getReturnType() == void.class,
                "addPropertyChangeListener returns void");
        check(method(c, "removePropertyChangeListener").getReturnType() == void.class,
                "removePropertyChangeListener returns void");
        Class<?>[] iae = method(c, "setAsText").getExceptionTypes();
        boolean declaresIAE = false;
        for (Class<?> e : iae) if (e == IllegalArgumentException.class) declaresIAE = true;
        check(declaresIAE, "setAsText declares IllegalArgumentException");
        check(!PropertyEditor.class.isAssignableFrom(PropertyEditorManager.class),
                "PropertyEditorManager is not itself a PropertyEditor");
    }

    static void testSupportImplementor() throws Exception {
        PropertyEditor pe = new PropertyEditorSupport();
        pe.setValue("x");
        check("x".equals(pe.getValue()), "getValue after setValue");
        check("x".equals(pe.getAsText()), "getAsText");
        check(!pe.isPaintable() && !pe.supportsCustomEditor(), "defaults");
        pe.setValue("y");
        check("y".equals(pe.getAsText()), "value replaced");
        check(pe.getValue() == pe.getValue(), "getValue stable");
        Object marker = new Object();
        pe.setValue(marker);
        check(pe.getValue() == marker, "identity round-trip for arbitrary Object");
        check(pe.getTags() == null, "no tags by default");
        check(pe.getCustomEditor() == null, "no custom editor by default");
        check("???".equals(pe.getJavaInitializationString()), "no init string by default");
        pe.paintValue(null, null);
        check(true, "paintValue with null Graphics is a no-op");
    }

    static void testIntegerEditorChain() throws Exception {
        PropertyEditor e = PropertyEditorManager.findEditor(Integer.class);
        check(e != null, "default Integer editor exists");
        Integer v = Integer.valueOf(42);
        e.setValue(v);
        check(e.getValue() == v, "setValue/getValue identity");
        check("42".equals(e.getAsText()), "getAsText of 42");
        e.setAsText("7");
        check(Integer.valueOf(7).equals(e.getValue()), "setAsText parses to Integer");
        check("7".equals(e.getJavaInitializationString()), "init string is the literal");
        e.setAsText("0x1F");
        check(Integer.valueOf(31).equals(e.getValue()), "decode accepts hex form");
        check("31".equals(e.getJavaInitializationString()), "init string tracks value");
        e.setAsText(null);
        check(e.getValue() == null, "setAsText(null) clears value");
        check(e.getAsText() == null, "getAsText null when value null");
        check("null".equals(e.getJavaInitializationString()), "init string \"null\" when value null");
        e.setValue(Integer.valueOf(1));
        checkThrows(NumberFormatException.class, new ThrowingRunnable() {
            public void run() throws Throwable {
                PropertyEditorManager.findEditor(Integer.class).setAsText("abc");
            }
        }, "garbage text rejected by NumberFormatException (an IllegalArgumentException)");
        check(e.getTags() == null, "no tags for Integer");
        check(!e.supportsCustomEditor(), "no custom editor");
        check(!e.isPaintable(), "not paintable");
        check(e.getCustomEditor() == null, "custom editor null");
        e.paintValue(null, null);
        check(true, "paintValue null-safe");
    }

    static void testEditorIndependenceAndEvents() throws Exception {
        PropertyEditor a = PropertyEditorManager.findEditor(Integer.class);
        PropertyEditor b = PropertyEditorManager.findEditor(Integer.class);
        check(a != b, "findEditor returns a fresh instance each call");
        a.setValue(Integer.valueOf(1));
        b.setValue(Integer.valueOf(2));
        check(Integer.valueOf(1).equals(a.getValue()) && Integer.valueOf(2).equals(b.getValue()),
                "editors hold independent state");

        final PropertyEditor w = PropertyEditorManager.findEditor(Integer.class);
        final int[] cnt = new int[1];
        final String[] fields = new String[4];
        PropertyChangeListener pcl = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
                cnt[0]++;
                fields[0] = String.valueOf(ev.getPropertyName());
                fields[1] = String.valueOf(ev.getOldValue());
                fields[2] = String.valueOf(ev.getNewValue());
                fields[3] = String.valueOf(ev.getSource() == w);
            }
        };
        w.addPropertyChangeListener(pcl);
        w.setValue(Integer.valueOf(5));
        w.setValue(Integer.valueOf(5));
        check(cnt[0] == 2, "setValue fires on every call, even for equal values");
        check("null".equals(fields[0]) && "null".equals(fields[1]) && "null".equals(fields[2]),
                "event carries null name/old/new");
        check("true".equals(fields[3]), "event source is the editor itself");
        w.removePropertyChangeListener(pcl);
        w.setValue(Integer.valueOf(6));
        check(cnt[0] == 2, "removed listener gets no further events");
        w.removePropertyChangeListener(pcl);
        check(true, "removing an unregistered listener is a no-op");
    }

    static void testBooleanEditorChain() throws Exception {
        PropertyEditor e = PropertyEditorManager.findEditor(Boolean.class);
        check(e != null, "default Boolean editor exists");
        e.setValue(Boolean.TRUE);
        check("True".equals(e.getAsText()), "boolean rendered capitalized True");
        String[] tags = e.getTags();
        check(tags != null && tags.length == 2, "Boolean editor exposes two tags");
        check("True".equals(tags[0]) && "False".equals(tags[1]), "tag values");
        e.setAsText("false");
        check(Boolean.FALSE.equals(e.getValue()), "tag text parsed case-insensitively");
        check("False".equals(e.getAsText()), "getAsText after setAsText");
        e.setAsText("TRUE");
        check(Boolean.TRUE.equals(e.getValue()), "uppercase accepted");
        e.setAsText(null);
        check(e.getValue() == null, "setAsText(null) clears value");
        check("null".equals(e.getJavaInitializationString()), "Boolean init string null when value null");
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() throws Throwable {
                PropertyEditorManager.findEditor(Boolean.class).setAsText("maybe");
            }
        }, "non-tag text rejected with IllegalArgumentException");
        e.setValue(Boolean.FALSE);
        check("false".equals(e.getJavaInitializationString()), "Boolean init string is literal");
    }

    static void testStringEditorChain() throws Exception {
        PropertyEditor e = PropertyEditorManager.findEditor(String.class);
        check(e != null, "default String editor exists");
        e.setValue("hi");
        check("hi".equals(e.getAsText()), "string value returned as-is");
        check("\"hi\"".equals(e.getJavaInitializationString()), "init string quotes the value");
        e.setValue(null);
        check(e.getAsText() == null, "null string value gives null text");
        e.setAsText("ok");
        check("ok".equals(e.getValue()), "String editor setAsText works even from null value");
        e.setValue("a\"b\\c\nd");
        String init = e.getJavaInitializationString();
        check(init.contains("\\\"") && init.contains("\\\\"), "init string escapes quote and backslash");
        check(init.contains("\\n"), "init string escapes newline");
        check(e.getTags() == null, "no tags for String");
    }

    public enum Sample { RED, GREEN }

    static void testEnumEditorChain() throws Exception {
        PropertyEditor e = PropertyEditorManager.findEditor(Sample.class);
        check(e != null, "enum types get an editor without registration");
        String[] tags = e.getTags();
        check(tags != null && tags.length == 2, "enum editor lists constant tags");
        check("RED".equals(tags[0]) && "GREEN".equals(tags[1]), "enum tag values are constant names");
        e.setAsText("GREEN");
        check(Sample.GREEN == e.getValue(), "setAsText resolves constant name");
        check("GREEN".equals(e.getAsText()), "getAsText returns constant name");
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() throws Throwable {
                PropertyEditorManager.findEditor(Sample.class).setValue("not-an-enum");
            }
        }, "setValue with wrong type rejected");
        e.setValue(null);
        check(e.getValue() == null, "null accepted by enum editor");
    }

    public static void main(String[] args) throws Exception {
        testInterfaceInventory();
        testSupportImplementor();
        testIntegerEditorChain();
        testEditorIndependenceAndEvents();
        testBooleanEditorChain();
        testStringEditorChain();
        testEnumEditorChain();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
