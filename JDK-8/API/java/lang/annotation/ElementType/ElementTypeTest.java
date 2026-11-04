/*
 * @test
 * @summary java.lang.annotation.ElementType conformance: enum constants/valueOf/ordinal.
 * @run main ElementTypeTest
 */

import java.lang.annotation.ElementType;

public class ElementTypeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testElementType() {
        check(ElementType.values().length >= 10, "at least ten values");
        check(ElementType.valueOf("TYPE") == ElementType.TYPE, "valueOf TYPE");
        check(ElementType.valueOf("FIELD") == ElementType.FIELD, "valueOf FIELD");
        check(ElementType.valueOf("METHOD") == ElementType.METHOD, "valueOf METHOD");
        check(ElementType.valueOf("PARAMETER") == ElementType.PARAMETER, "valueOf PARAMETER");
        check(ElementType.valueOf("CONSTRUCTOR") == ElementType.CONSTRUCTOR, "valueOf CONSTRUCTOR");
        check(ElementType.valueOf("LOCAL_VARIABLE") == ElementType.LOCAL_VARIABLE,
              "valueOf LOCAL_VARIABLE");
        check(ElementType.valueOf("ANNOTATION_TYPE") == ElementType.ANNOTATION_TYPE,
              "valueOf ANNOTATION_TYPE");
        check(ElementType.valueOf("PACKAGE") == ElementType.PACKAGE, "valueOf PACKAGE");
        check(ElementType.valueOf("TYPE_PARAMETER") == ElementType.TYPE_PARAMETER,
              "valueOf TYPE_PARAMETER");
        check(ElementType.valueOf("TYPE_USE") == ElementType.TYPE_USE, "valueOf TYPE_USE");
        check(ElementType.TYPE.ordinal() == 0, "TYPE ordinal 0");
    }

    public static void main(String[] args) {
        testElementType();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
