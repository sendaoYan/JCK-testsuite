/*
 * @test
 * @summary javax.swing.text.DefaultFormatterFactory conformance: white-box structure + black-box behavior.
 * @run main DefaultFormatterFactoryTest
 */

import java.lang.reflect.Modifier;
import javax.swing.JFormattedTextField;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.DefaultFormatterFactory;

public class DefaultFormatterFactoryTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.DefaultFormatterFactory");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JFormattedTextField$AbstractFormatterFactory"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.io.Serializable"), "interface 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        DefaultFormatter a = new DefaultFormatter();
        DefaultFormatter b = new DefaultFormatter();
        DefaultFormatter c = new DefaultFormatter();
        DefaultFormatter d = new DefaultFormatter();

        DefaultFormatterFactory f4 = new DefaultFormatterFactory(a, b, c, d);
        check(f4.getDefaultFormatter() == a, "4-arg default");
        check(f4.getDisplayFormatter() == b, "4-arg display");
        check(f4.getEditFormatter() == c, "4-arg edit");
        check(f4.getNullFormatter() == d, "4-arg null");

        DefaultFormatterFactory f1 = new DefaultFormatterFactory(a);
        check(f1.getDefaultFormatter() == a, "1-arg default");
        check(f1.getDisplayFormatter() == null, "1-arg display null");
        check(f1.getEditFormatter() == null, "1-arg edit null");
        check(f1.getNullFormatter() == null, "1-arg null null");

        DefaultFormatterFactory f2 = new DefaultFormatterFactory(a, b);
        check(f2.getDefaultFormatter() == a && f2.getDisplayFormatter() == b, "2-arg default+display");
        check(f2.getEditFormatter() == null && f2.getNullFormatter() == null, "2-arg later null");

        DefaultFormatterFactory f3 = new DefaultFormatterFactory(a, b, c);
        check(f3.getDefaultFormatter() == a && f3.getDisplayFormatter() == b && f3.getEditFormatter() == c, "3-arg default+display+edit");
        check(f3.getNullFormatter() == null, "3-arg null null");

        DefaultFormatterFactory f0 = new DefaultFormatterFactory();
        check(f0.getDefaultFormatter() == null && f0.getDisplayFormatter() == null
                && f0.getEditFormatter() == null && f0.getNullFormatter() == null, "no-arg all null");
        f0.setDefaultFormatter(a);
        f0.setDisplayFormatter(b);
        f0.setEditFormatter(c);
        f0.setNullFormatter(d);
        check(f0.getDefaultFormatter() == a, "setDefaultFormatter round trip");
        check(f0.getDisplayFormatter() == b, "setDisplayFormatter round trip");
        check(f0.getEditFormatter() == c, "setEditFormatter round trip");
        check(f0.getNullFormatter() == d, "setNullFormatter round trip");

        check(f4.getFormatter(null) == null, "getFormatter(null) null");

        JFormattedTextField ftf = new JFormattedTextField();
        check(f4.getFormatter(ftf) == d, "null value -> null formatter");

        ftf.setValue("x");
        check(f4.getFormatter(ftf) == b, "non-null value, no focus -> display formatter");

        check(f1.getFormatter(ftf) == a, "display null -> default fallback");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
