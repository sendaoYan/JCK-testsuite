/*
 * @test
 * @summary org.w3c.dom.html.HTMLCollection conformance: length/item/namedItem.
 * @run main HTMLCollectionTest
 */

public class HTMLCollectionTest {
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
        Class<?> k = L("org.w3c.dom.html.HTMLCollection");
        check(k.isInterface(), "org.w3c.dom.html.HTMLCollection is an interface");
        check(k.getInterfaces().length == 0, "HTMLCollection declares no superinterface");
        check(k.getMethod("getLength") != null, "getLength()");
        check(k.getMethod("item", int.class) != null, "item(int)");
        check(k.getMethod("namedItem", String.class) != null, "namedItem(String)");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
