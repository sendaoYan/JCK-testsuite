/*
 * @test
 * @summary javax.swing.text.DefaultFormatter conformance: white-box structure + black-box behavior.
 * @run main DefaultFormatterTest
 */

import java.lang.reflect.Modifier;
import java.text.ParseException;
import javax.swing.text.DefaultFormatter;

public class DefaultFormatterTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // Value class whose String constructor throws, to exercise the ParseException path.
    public static class Boom {
        public Boom(String s) { throw new RuntimeException("boom"); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.DefaultFormatter");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JFormattedTextField$AbstractFormatter"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 2, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.lang.Cloneable"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("java.io.Serializable"), "interface 1");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        DefaultFormatter f = new DefaultFormatter();

        check(f.getOverwriteMode() == true, "overwriteMode default true");
        check(f.getAllowsInvalid() == true, "allowsInvalid default true");
        check(f.getCommitsOnValidEdit() == false, "commitsOnValidEdit default false");
        check(f.getValueClass() == null, "valueClass default null");

        f.setCommitsOnValidEdit(true);
        check(f.getCommitsOnValidEdit() == true, "setCommitsOnValidEdit round trip");
        f.setOverwriteMode(false);
        check(f.getOverwriteMode() == false, "setOverwriteMode round trip");
        f.setAllowsInvalid(false);
        check(f.getAllowsInvalid() == false, "setAllowsInvalid round trip");

        f.setValueClass(Integer.class);
        check(f.getValueClass() == Integer.class, "setValueClass/getValueClass");

        check("".equals(f.valueToString(null)), "valueToString(null) -> empty");
        check("hello".equals(f.valueToString("hello")), "valueToString -> toString");

        Object v = f.stringToValue("42");
        check(v instanceof Integer && ((Integer) v).intValue() == 42, "stringToValue with valueClass Integer");

        f.setValueClass(Object.class); // no String constructor
        Object s = f.stringToValue("plain");
        check("plain".equals(s), "stringToValue falls back to String when no String ctor");

        f.setValueClass(Boom.class);
        boolean threw = false;
        try {
            f.stringToValue("x");
        } catch (ParseException pe) {
            threw = true;
        }
        check(threw, "stringToValue -> ParseException when String ctor throws");

        f.setValueClass(Integer.class);
        DefaultFormatter clone = (DefaultFormatter) f.clone();
        check(clone != f, "clone is distinct instance");
        check(clone.getClass() == DefaultFormatter.class, "clone same class");
        check(clone.getValueClass() == Integer.class, "clone copies valueClass");
        clone.setValueClass(String.class);
        check(f.getValueClass() == Integer.class, "clone independent of original");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
