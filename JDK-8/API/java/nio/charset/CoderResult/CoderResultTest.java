/*
 * @test
 * @summary java.nio.charset.CoderResult conformance: white-box structure + black-box factory/toString/throw.
 * @run main CoderResultTest
 */

import java.lang.reflect.Modifier;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
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
        // Not directly constructible: every declared constructor is non-public.
        for (java.lang.reflect.Constructor<?> k : c.getDeclaredConstructors())
            check(!Modifier.isPublic(k.getModifiers()), "A3 CoderResult ctor not public");
        // JDK-8: class is non-final (two ctors: one private, one package-private).
        // JDK-27 makes the class final with a single private ctor (asserted on that side).
        check(!Modifier.isFinal(c.getModifiers()), "A3 CoderResult not final on JDK-8");
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

    // ================ A3 batch appended: caching/identity, predicate matrix, singleton
    // ================ cross-check against coder machinery, exception messages ================

    static void testExtrasA3() {
        // Factory caching: equal lengths return the same cached instance (probed both sides,
        // including n = 100 -- the cache grows to the largest requested length).
        check(CoderResult.malformedForLength(1) == CoderResult.malformedForLength(1), "A3 mal(1) cached");
        check(CoderResult.malformedForLength(2) == CoderResult.malformedForLength(2), "A3 mal(2) cached");
        check(CoderResult.unmappableForLength(2) == CoderResult.unmappableForLength(2), "A3 unm(2) cached");
        check(CoderResult.malformedForLength(100) == CoderResult.malformedForLength(100), "A3 mal(100) cached");
        CoderResult m1 = CoderResult.malformedForLength(1);
        CoderResult m2 = CoderResult.malformedForLength(2);
        CoderResult u1 = CoderResult.unmappableForLength(1);
        check(m1 != m2 && m1 != u1, "A3 distinct lengths/roles are distinct objects");
        check(m1.equals(m1) && m1.hashCode() == m1.hashCode(), "A3 equals/hashCode consistent");
        check(!m1.equals(u1), "A3 malformed not equal to unmappable");
        // toString and predicate matrix for every result shape.
        check("MALFORMED[1]".equals(m1.toString()), "A3 MALFORMED[1] toString");
        check("UNMAPPABLE[1]".equals(u1.toString()), "A3 UNMAPPABLE[1] toString");
        check(!m2.isUnderflow() && !m2.isOverflow(), "A3 error result neither under/overflow");
        check(!u1.isUnderflow() && !u1.isOverflow(), "A3 unmappable neither under/overflow");
        check(m1.isError() && m2.isError() && u1.isError(), "A3 isError for all error results");
        check(m1.isMalformed() && !m1.isUnmappable(), "A3 malformed predicate exclusive");
        check(u1.isUnmappable() && !u1.isMalformed(), "A3 unmappable predicate exclusive");
        check(!CoderResult.UNDERFLOW.isMalformed() && !CoderResult.UNDERFLOW.isUnmappable(),
              "A3 UNDERFLOW not malformed/unmappable");
        check(!CoderResult.OVERFLOW.isMalformed() && !CoderResult.OVERFLOW.isUnmappable(),
              "A3 OVERFLOW not malformed/unmappable");
        // Negative lengths are rejected exactly like zero.
        expect(IllegalArgumentException.class, () -> CoderResult.malformedForLength(-1),
              "A3 malformedForLength(-1) IAE");
        expect(IllegalArgumentException.class, () -> CoderResult.unmappableForLength(-1),
              "A3 unmappableForLength(-1) IAE");
        // The two constants are true singletons: the coder machinery hands back the same objects.
        CharsetDecoder d = StandardCharsets.UTF_8.newDecoder();
        check(d.decode(ByteBuffer.wrap(new byte[0]), CharBuffer.allocate(1), true) == CoderResult.UNDERFLOW,
              "A3 decode(empty) returns UNDERFLOW singleton");
        CoderResult ov = StandardCharsets.UTF_8.newDecoder()
                .decode(ByteBuffer.wrap("ab".getBytes(StandardCharsets.US_ASCII)), CharBuffer.allocate(1), true);
        check(ov == CoderResult.OVERFLOW, "A3 decode(tiny out) returns OVERFLOW singleton");
        CoderResult en = StandardCharsets.UTF_8.newEncoder()
                .encode(CharBuffer.wrap("ab"), ByteBuffer.allocate(1), true);
        check(en == CoderResult.OVERFLOW, "A3 encode(tiny out) returns OVERFLOW singleton");
    }

    static void testExceptionMessagesA3() throws Exception {
        try {
            CoderResult.malformedForLength(5).throwException();
            check(false, "A3 malformed throwException must throw");
        } catch (MalformedInputException e) {
            check("Input length = 5".equals(e.getMessage()), "A3 MIE message format");
            check(e.getInputLength() == 5, "A3 MIE length 5");
        }
        try {
            CoderResult.unmappableForLength(6).throwException();
            check(false, "A3 unmappable throwException must throw");
        } catch (UnmappableCharacterException e) {
            check("Input length = 6".equals(e.getMessage()), "A3 UCE message format");
            check(e.getInputLength() == 6, "A3 UCE length 6");
        }
        // Repeated throws are allowed: the same result object can throw again.
        try {
            CoderResult.malformedForLength(5).throwException();
            check(false, "A3 repeated throw must throw again");
        } catch (MalformedInputException e) {
            check(e.getInputLength() == 5, "A3 repeated throw length");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testExtrasA3();
        testExceptionMessagesA3();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
