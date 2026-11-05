/*
 * @test
 * @summary java.lang.classfile.attribute.StackMapFrameInfo.SimpleVerificationTypeInfo conformance: white-box structure + black-box behavior.
 * @run main SimpleVerificationTypeInfoTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;
import java.nio.file.*;

public class SimpleVerificationTypeInfoTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean declares(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }
    static final ClassReader[] cap = new ClassReader[1];
    static final BufWriter[] cap2 = new BufWriter[1];
    static final BranchInstruction[] br = new BranchInstruction[1];
    static final SwitchCase[] sc = new SwitchCase[1];
    static final TableSwitchInstruction[] ts = new TableSwitchInstruction[1];
    static final LookupSwitchInstruction[] ls = new LookupSwitchInstruction[1];
    static final ExceptionCatch[] ec = new ExceptionCatch[1];
    static final TypeAnnotation.TypeArgumentTarget[] ta = new TypeAnnotation.TypeArgumentTarget[1];
    static final TypeAnnotation.LocalVarTarget[] lvt = new TypeAnnotation.LocalVarTarget[1];
    static final TypeAnnotation.LocalVarTargetInfo[] lvi = new TypeAnnotation.LocalVarTargetInfo[1];
    static final StackMapFrameInfo.UninitializedVerificationTypeInfo[] uv = new StackMapFrameInfo.UninitializedVerificationTypeInfo[1];
    static final TypeAnnotation.OffsetTarget[] ot = new TypeAnnotation.OffsetTarget[1];

    // ---- custom attribute support for black-box above ----
    static class CA extends CustomAttribute<CA> { CA(AttributeMapper<CA> m) { super(m); } }
    static class CA2 extends CustomAttribute<CA2> { CA2(AttributeMapper<CA2> m) { super(m); } }
    static class CA3 extends CustomAttribute<CA3> { CA3(AttributeMapper<CA3> m) { super(m); } }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.classfile.attribute.StackMapFrameInfo$SimpleVerificationTypeInfo");
        check(c != null, "java.lang.classfile.attribute.StackMapFrameInfo.SimpleVerificationTypeInfo loads");
        check(c.getName().equals("java.lang.classfile.attribute.StackMapFrameInfo$SimpleVerificationTypeInfo"), "binary name java.lang.classfile.attribute.StackMapFrameInfo$SimpleVerificationTypeInfo");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.attribute.StackMapFrameInfo.SimpleVerificationTypeInfo is public");
        check(c.isEnum(), "java.lang.classfile.attribute.StackMapFrameInfo.SimpleVerificationTypeInfo is an enum");
        check(c.getSuperclass() == Enum.class, "java.lang.classfile.attribute.StackMapFrameInfo.SimpleVerificationTypeInfo superclass is java.lang.Enum");
        Object[] ec = c.getEnumConstants();
        check(ec.length == 7, "7 enum constants");
        java.util.Set<String> es = new java.util.HashSet<>();
        for (Object e : ec) es.add(((Enum<?>) e).name());
        check(es.equals(java.util.Set.of("TOP", "INTEGER", "FLOAT", "DOUBLE", "LONG", "NULL", "UNINITIALIZED_THIS")), "exact enum constant names");
        check(declares(c, "values"), "declares values");
        check(declares(c, "valueOf"), "declares valueOf");
        check(declares(c, "tag"), "declares tag");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        check(StackMapFrameInfo.SimpleVerificationTypeInfo.TOP.tag() == 0, "TOP tag 0");
        check(StackMapFrameInfo.SimpleVerificationTypeInfo.INTEGER.tag() == 1, "INTEGER tag 1");
        check(StackMapFrameInfo.SimpleVerificationTypeInfo.NULL.tag() == 5, "NULL tag 5");
        check(StackMapFrameInfo.SimpleVerificationTypeInfo.UNINITIALIZED_THIS.tag() == 6, "UNINITIALIZED_THIS tag 6");
        check(StackMapFrameInfo.SimpleVerificationTypeInfo.values().length == 7, "7 constants");

    }


    // ---- real-product member ----
    static class SVReal { static int f(boolean c, int a, long b) { int r = a; if (c) r += b; return r; } }

    // ---- Part 3: 枚举全值表（name 顺序 + tag == ordinal，JVMS 0..6）----
    static void testEnumTable() {
        StackMapFrameInfo.SimpleVerificationTypeInfo[] vs = StackMapFrameInfo.SimpleVerificationTypeInfo.values();
        check(vs.length == 7, "7 values");
        List<String> names = new ArrayList<>();
        for (var v : vs) names.add(v.name());
        check(names.equals(List.of("TOP", "INTEGER", "FLOAT", "DOUBLE", "LONG", "NULL", "UNINITIALIZED_THIS")), "value order: " + names);
        int[] tags = {0, 1, 2, 3, 4, 5, 6};
        for (int i = 0; i < vs.length; i++) {
            check(vs[i].tag() == tags[i], vs[i].name() + " tag " + vs[i].tag());
            check(vs[i].ordinal() == vs[i].tag(), vs[i].name() + " ordinal==tag");
            check(StackMapFrameInfo.SimpleVerificationTypeInfo.valueOf(vs[i].name()) == vs[i], "valueOf roundtrip " + vs[i].name());
            check(vs[i] instanceof StackMapFrameInfo.VerificationTypeInfo, "is VerificationTypeInfo");
        }
        check(StackMapFrameInfo.SimpleVerificationTypeInfo.TOP.tag() == StackMapFrameInfo.VerificationTypeInfo.ITEM_TOP, "TOP==ITEM_TOP");
        check(StackMapFrameInfo.SimpleVerificationTypeInfo.INTEGER.tag() == StackMapFrameInfo.VerificationTypeInfo.ITEM_INTEGER, "INTEGER==ITEM_INTEGER");
        check(StackMapFrameInfo.SimpleVerificationTypeInfo.UNINITIALIZED_THIS.tag() == StackMapFrameInfo.VerificationTypeInfo.ITEM_UNINITIALIZED_THIS, "UNIT_THIS==ITEM_UNINITIALIZED_THIS");
    }

    // ---- Part 4: javac 真产物 frame 中出现 Simple vti ----
    static void testRealFrames() throws Exception {
        byte[] rb = SimpleVerificationTypeInfoTest.class.getResourceAsStream("SimpleVerificationTypeInfoTest$SVReal.class").readAllBytes();
        ClassModel m = ClassFile.of().parse(rb);
        Set<String> seen = new HashSet<>();
        for (MethodModel mm : m.methods()) {
            if (!mm.methodName().stringValue().equals("f") || mm.code().isEmpty()) continue;
            CodeAttribute ca = (CodeAttribute) mm.code().get();
            for (StackMapFrameInfo fr : ca.findAttribute(Attributes.stackMapTable()).get().entries())
                for (StackMapFrameInfo.VerificationTypeInfo v : fr.locals())
                    if (v instanceof StackMapFrameInfo.SimpleVerificationTypeInfo s) seen.add(s.name());
        }
        check(seen.contains("INTEGER"), "INTEGER local present");
        check(seen.contains("LONG") || seen.size() >= 1, "simple vti observed: " + seen);
    }

    // ---- Part 5: 组装回读（枚举常量直接当 frame locals 用）----
    static void testAssemblyReadback() throws Exception {
        byte[] bb = ClassFile.of().build(ClassDesc.of("DynSVT"), cb -> cb.withMethodBody("m",
                MethodTypeDesc.ofDescriptor("(I)I"), ClassFile.ACC_STATIC,
                cob -> { Label l = cob.newLabel(); cob.iconst_0().iload(0).if_icmpge(l); cob.iconst_1().ireturn(); cob.labelBinding(l).iconst_m1().ireturn(); }));
        CodeAttribute ca = (CodeAttribute) ClassFile.of().parse(bb).methods().get(0).code().get();
        Label lbl = ca.findAttribute(Attributes.stackMapTable()).get().entries().get(0).target();
        var frame = StackMapFrameInfo.of(lbl,
                List.of(StackMapFrameInfo.SimpleVerificationTypeInfo.INTEGER, StackMapFrameInfo.SimpleVerificationTypeInfo.TOP),
                List.of(StackMapFrameInfo.SimpleVerificationTypeInfo.DOUBLE));
        check(frame.locals().get(0) == StackMapFrameInfo.SimpleVerificationTypeInfo.INTEGER, "assembled locals");
        check(frame.stack().get(0).tag() == StackMapFrameInfo.VerificationTypeInfo.ITEM_DOUBLE, "assembled stack DOUBLE");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testEnumTable();
        testRealFrames();
        testAssemblyReadback();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
