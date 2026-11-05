/*
 * @test
 * @summary jdk.incubator.vector.VectorOperators conformance: operator constants.
 * @modules jdk.incubator.vector
 * @run main VectorOperatorsTest
 */

import jdk.incubator.vector.*;

public class VectorOperatorsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testDeepen() {
        check(VectorOperators.ADD.name().equals("ADD") && VectorOperators.ADD.operatorName().equals("+"), "ADD name/token");
        check(VectorOperators.NEG.operatorName().equals("-a") && VectorOperators.NOT.operatorName().equals("~"), "unary tokens");
        check(VectorOperators.MUL.operatorName().equals("*") && VectorOperators.DIV.operatorName().equals("/"), "mul/div tokens");
        check(VectorOperators.LT.operatorName().equals("<") && VectorOperators.GE.operatorName().equals(">="), "comparison tokens");
        check(VectorOperators.I2D instanceof VectorOperators.Conversion, "I2D is Conversion");
        check(VectorOperators.FMA instanceof VectorOperators.Ternary, "FMA is Ternary");
        check(VectorOperators.LT instanceof VectorOperators.Comparison, "LT is Comparison");
        check(VectorOperators.IS_NAN instanceof VectorOperators.Test, "IS_NAN is Test");
        check(VectorOperators.NOT instanceof VectorOperators.Unary, "NOT is Unary");
        check(VectorOperators.XOR instanceof VectorOperators.Binary, "XOR is Binary");
        check(VectorOperators.ADD instanceof VectorOperators.Associative, "ADD is Associative");
        check(VectorOperators.UMIN instanceof VectorOperators.Associative && VectorOperators.UMAX != null, "UMIN/UMAX");
        check(VectorOperators.SADD != null && VectorOperators.SUADD != null
                && VectorOperators.SSUB != null && VectorOperators.SUSUB != null, "saturating op constants");
        check(VectorOperators.FIRST_NONZERO != null && VectorOperators.ZOMO != null, "reassociation op constants");
        check(VectorOperators.BIT_COUNT != null && VectorOperators.TRAILING_ZEROS_COUNT != null
                && VectorOperators.LEADING_ZEROS_COUNT != null, "bit count op constants");
        check(VectorOperators.ROL != null && VectorOperators.ROR != null, "rotate op constants");
        check(VectorOperators.COMPRESS_BITS != null && VectorOperators.EXPAND_BITS != null, "bit compress/expand constants");
        check(VectorOperators.REINTERPRET_F2I != null && VectorOperators.REINTERPRET_D2L != null, "reinterpret conversions");
        check(VectorOperators.ZERO_EXTEND_B2I != null && VectorOperators.ZERO_EXTEND_S2L != null, "zero-extend conversions");
        Class<?> op;
        try { op = Class.forName("jdk.incubator.vector.VectorOperators$Operator"); } catch (Exception e) { throw new RuntimeException(e); }
        java.util.Set<String> ms = new java.util.TreeSet<>();
        for (java.lang.reflect.Method m : op.getMethods()) ms.add(m.getName());
        check(ms.equals(new java.util.TreeSet<>(java.util.Arrays.asList(
                "arity", "compatibleWith", "isAssociative", "isBoolean", "name", "operatorName", "rangeType"))),
                "Operator interface surface exact");
        check(op.isSealed(), "Operator sealed");
        check(java.util.Arrays.toString(op.getPermittedSubclasses()).contains("Unary")
                && java.util.Arrays.toString(op.getPermittedSubclasses()).contains("Conversion"), "sealed permits subinterfaces");
    }

    public static void main(String[] args) throws Exception {
        testDeepen();

        check(VectorOperators.ADD != null, "ADD");
        check(VectorOperators.NEG != null, "NEG");
        check(VectorOperators.LT != null, "LT");
        check(VectorOperators.NOT != null, "NOT");
        check(VectorOperators.SQRT != null, "SQRT");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
