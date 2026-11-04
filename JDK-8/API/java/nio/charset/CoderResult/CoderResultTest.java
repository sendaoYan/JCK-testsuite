/*
 * @test
 * @summary java.nio.charset.CoderResult conformance: white-box structure + black-box factory/toString/throw.
 * @run main CoderResultTest
 */

import java.lang.reflect.Modifier;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.charset.CoderResult;
import java.nio.charset.MalformedInputException;
import java.nio.charset.UnmappableCharacterException;

public class CoderResultTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expect(Class<? extends Throwable> ex, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (ex.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + ": wrong exception " + t, t);
        }
        throw new AssertionError(msg + ": no exception thrown");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.charset.CoderResult");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass() == Object.class, "superclass is Object");
        check(c.getField("UNDERFLOW") != null && c.getField("OVERFLOW") != null, "UNDERFLOW/OVERFLOW constants");
    }

    static void testBlackBox() throws Exception {
        check(CoderResult.UNDERFLOW.isUnderflow(), "UNDERFLOW isUnderflow");
        check(!CoderResult.UNDERFLOW.isOverflow(), "UNDERFLOW not overflow");
        check(!CoderResult.UNDERFLOW.isError(), "UNDERFLOW not error");
        check(CoderResult.OVERFLOW.isOverflow(), "OVERFLOW isOverflow");
        check(!CoderResult.OVERFLOW.isError(), "OVERFLOW not error");
        check(!CoderResult.OVERFLOW.isUnderflow(), "OVERFLOW not underflow");

        check("UNDERFLOW".equals(CoderResult.UNDERFLOW.toString()), "UNDERFLOW toString");
        check("OVERFLOW".equals(CoderResult.OVERFLOW.toString()), "OVERFLOW toString");

        expect(UnsupportedOperationException.class, () -> CoderResult.UNDERFLOW.length(), "UNDERFLOW.length throws");
        expect(UnsupportedOperationException.class, () -> CoderResult.OVERFLOW.length(), "OVERFLOW.length throws");

        expect(BufferUnderflowException.class, () -> {
            try {
                CoderResult.UNDERFLOW.throwException();
            } catch (java.nio.charset.CharacterCodingException cce) {
                throw new AssertionError(cce);
            }
        }, "UNDERFLOW.throwException throws BufferUnderflowException");
        expect(BufferOverflowException.class, () -> {
            try {
                CoderResult.OVERFLOW.throwException();
            } catch (java.nio.charset.CharacterCodingException cce) {
                throw new AssertionError(cce);
            }
        }, "OVERFLOW.throwException throws BufferOverflowException");

        CoderResult mal = CoderResult.malformedForLength(3);
        check(mal.isError(), "malformed isError");
        check(mal.isMalformed(), "malformed isMalformed");
        check(!mal.isUnmappable(), "malformed not unmappable");
        check(mal.length() == 3, "malformed length");
        check("MALFORMED[3]".equals(mal.toString()), "malformed toString");
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
        check("UNMAPPABLE[4]".equals(unmap.toString()), "unmappable toString");
        try {
            unmap.throwException();
            check(false, "unmappable throwException should throw");
        } catch (UnmappableCharacterException expected) {
            check(expected.getInputLength() == 4, "UnmappableCharacterException length");
        }

        expect(IllegalArgumentException.class, () -> CoderResult.malformedForLength(0), "malformedForLength(0) throws");
        expect(IllegalArgumentException.class, () -> CoderResult.unmappableForLength(0), "unmappableForLength(0) throws");

        check(CoderResult.malformedForLength(3) == mal, "malformedForLength(3) is cached");
        check(CoderResult.unmappableForLength(4) == unmap, "unmappableForLength(4) is cached");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
