/*
 * @test
 * @summary javax.swing.JFormattedTextField conformance: white-box structure + black-box behavior.
 * @run main JFormattedTextFieldTest
 */

import java.lang.reflect.Modifier;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.JFormattedTextField;

public class JFormattedTextFieldTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.JFormattedTextField");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JTextField"), "superclass");
        check(!Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        JFormattedTextField f = new JFormattedTextField();
        check(f.getValue() == null, "default value null");
        check(f.getFocusLostBehavior() == JFormattedTextField.COMMIT_OR_REVERT, "default focus lost behavior");
        f.setValue(Integer.valueOf(42));
        check(Integer.valueOf(42).equals(f.getValue()), "setValue/getValue");
        f.setFocusLostBehavior(JFormattedTextField.REVERT);
        check(f.getFocusLostBehavior() == JFormattedTextField.REVERT, "setFocusLostBehavior");
        check(f.getFormatterFactory() != null, "formatter factory non-null");

        check(JFormattedTextField.COMMIT == 0 && JFormattedTextField.COMMIT_OR_REVERT == 1
                && JFormattedTextField.REVERT == 2 && JFormattedTextField.PERSIST == 3, "constants");

        JFormattedTextField obj = new JFormattedTextField(Integer.valueOf(7));
        check(Integer.valueOf(7).equals(obj.getValue()), "object ctor");

        JFormattedTextField num = new JFormattedTextField(NumberFormat.getIntegerInstance());
        num.setValue(Long.valueOf(123));
        check("123".equals(num.getText()), "formatted text");
        check(num.isEditValid(), "isEditValid true");

        num.setText("bad");
        boolean threw = false;
        try { num.commitEdit(); } catch (ParseException e) { threw = true; }
        check(threw, "commitEdit throws on invalid");
        check(!num.isEditValid(), "isEditValid false after bad text");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
