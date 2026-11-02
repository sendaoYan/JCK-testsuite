/*
 * @test
 * @summary org.w3c.dom.html.HTMLInputElement conformance: HTMLInputElement.
 * @run main HTMLInputElementTest
 */

public class HTMLInputElementTest {
    static int passed;

    static void check(boolean c, String m) {
        if (!c) throw new AssertionError(m);
        passed++;
    }

    static Class<?> L(String n) throws Exception { return Class.forName(n); }

    static short sconst(String c, String f) throws Exception {
        return Class.forName(c).getField(f).getShort(null);
    }

    public static void main(String[] args) throws Exception {
        Class<?> k = L("org.w3c.dom.html.HTMLInputElement");
        check(k.isInterface(), "org.w3c.dom.html.HTMLInputElement is an interface");
        check(L("org.w3c.dom.html.HTMLElement").isAssignableFrom(k), "HTMLInputElement extends HTMLElement");
        check(k.getInterfaces().length >= 1, "HTMLInputElement has superinterface");
        check(L("org.w3c.dom.Element").isAssignableFrom(k), "HTMLInputElement is-a Element");
        check(k.getMethod("getDefaultValue") != null, "getDefaultValue()");
        check(k.getMethod("getForm") != null, "getForm()");
        check(k.getMethod("setChecked", boolean.class) != null, "setChecked(boolean)");
        check(k.getMethod("getType") != null, "getType()");
        check(k.getMethod("blur") != null, "blur()");
        check(k.getMethod("focus") != null, "focus()");
        check(k.getMethod("select") != null, "select()");
        check(k.getMethod("click") != null, "click()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
