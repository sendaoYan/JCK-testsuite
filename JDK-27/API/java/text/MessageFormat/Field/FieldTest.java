/*
 * @test
 * @summary java.text.MessageFormat.Field conformance: ARGUMENT constant and appearance in
 *          formatToCharacterIterator.
 * @run main FieldTest
 */

import java.text.AttributedCharacterIterator;
import java.text.MessageFormat;

public class FieldTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testField() {
        MessageFormat.Field arg = MessageFormat.Field.ARGUMENT;
        check(arg != null, "ARGUMENT constant");
        check(arg.equals(MessageFormat.Field.ARGUMENT), "equals");
        check(arg.hashCode() == MessageFormat.Field.ARGUMENT.hashCode(), "hashCode");
        check(arg.toString() != null, "toString");

        MessageFormat mf = new MessageFormat("{0} is {1}");
        AttributedCharacterIterator it =
                mf.formatToCharacterIterator(new Object[]{"this", "test"});
        check(it.getAllAttributeKeys().contains(MessageFormat.Field.ARGUMENT),
              "ARGUMENT attribute present in output");
    }


    // ---- deepening helper (contract-level additions) ----
    interface TestBlock { void go() throws Exception; }

    static void expectX(Class<? extends Throwable> expected, TestBlock r, String m) {
        try {
            r.go();
        } catch (Throwable x) {
            if (expected.isInstance(x)) { passed++; return; }
            throw new AssertionError(m + ": expected " + expected.getSimpleName()
                    + " but got " + x.getClass().getName(), x);
        }
        throw new AssertionError(m + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }


    // ================= contract deepening =================

    static void testContractDeepening() {
        check(java.text.MessageFormat.Field.ARGUMENT != null, "ARGUMENT constant exists");
        check("java.text.MessageFormat$Field(message argument field)"
              .equals(java.text.MessageFormat.Field.ARGUMENT.toString()), "ARGUMENT toString exact");
        java.text.MessageFormat.Field other = java.text.MessageFormat.Field.ARGUMENT;
        check(other.equals(java.text.MessageFormat.Field.ARGUMENT)
              && other.hashCode() == java.text.MessageFormat.Field.ARGUMENT.hashCode(),
              "ARGUMENT is a singleton");
        java.text.MessageFormat mf = new java.text.MessageFormat("{0}", java.util.Locale.ENGLISH);
        java.text.AttributedCharacterIterator it = mf.formatToCharacterIterator(new Object[]{"v"});
        check(it.getAttribute(java.text.MessageFormat.Field.ARGUMENT) != null,
              "ARGUMENT marks substituted spans");
        check(java.text.Format.Field.class.isAssignableFrom(java.text.MessageFormat.Field.class),
              "extends Format.Field");
    }
    public static void main(String[] args) {
        testContractDeepening();
        testField();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
