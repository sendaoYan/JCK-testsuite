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

    public static void main(String[] args) {
        testField();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
