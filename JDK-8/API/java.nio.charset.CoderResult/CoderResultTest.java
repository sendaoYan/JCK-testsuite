/*
 * @test
 * @summary java.nio.charset.CoderResult conformance: underflow/overflow/error and throwException.
 * @run main CoderResultTest
 */

import java.nio.charset.CoderResult;
import java.nio.charset.MalformedInputException;
import java.nio.charset.UnmappableCharacterException;

public class CoderResultTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testCoderResult() throws Exception {
        check(CoderResult.UNDERFLOW.isUnderflow(), "UNDERFLOW isUnderflow");
        check(!CoderResult.UNDERFLOW.isOverflow(), "UNDERFLOW not overflow");
        check(!CoderResult.UNDERFLOW.isError(), "UNDERFLOW not error");
        check(CoderResult.OVERFLOW.isOverflow(), "OVERFLOW isOverflow");
        check(!CoderResult.OVERFLOW.isError(), "OVERFLOW not error");

        CoderResult mal = CoderResult.malformedForLength(3);
        check(mal.isError(), "malformed isError");
        check(mal.isMalformed(), "malformed isMalformed");
        check(!mal.isUnmappable(), "malformed not unmappable");
        check(mal.length() == 3, "malformed length");
        try {
            mal.throwException();
            check(false, "malformed throwException should throw");
        } catch (MalformedInputException expected) {
            check(expected.getInputLength() == 3, "MalformedInputException length");
        }

        CoderResult unmap = CoderResult.unmappableForLength(4);
        check(unmap.isError(), "unmappable isError");
        check(unmap.isUnmappable(), "unmappable isUnmappable");
        check(!unmap.isMalformed(), "unmappable not malformed");
        check(unmap.length() == 4, "unmappable length");
        try {
            unmap.throwException();
            check(false, "unmappable throwException should throw");
        } catch (UnmappableCharacterException expected) {
            check(expected.getInputLength() == 4, "UnmappableCharacterException length");
        }
    }

    public static void main(String[] args) throws Exception {
        testCoderResult();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
