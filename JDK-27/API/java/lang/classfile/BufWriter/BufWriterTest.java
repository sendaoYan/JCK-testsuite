/*
 * @test
 * @summary java.lang.classfile.BufWriter conformance: white-box structure + black-box behavior.
 * @run main BufWriterTest
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

public class BufWriterTest {
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
        Class<?> c = Class.forName("java.lang.classfile.BufWriter");
        check(c != null, "java.lang.classfile.BufWriter loads");
        check(c.getName().equals("java.lang.classfile.BufWriter"), "binary name java.lang.classfile.BufWriter");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.BufWriter is public");
        check(c.isInterface(), "java.lang.classfile.BufWriter is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.BufWriter has no superclass");
        check(declares(c, "constantPool"), "declares constantPool");
        check(declares(c, "canWriteDirect"), "declares canWriteDirect");
        check(declares(c, "reserveSpace"), "declares reserveSpace");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        AttributeMapper<CA3> mapper = new AttributeMapper<CA3>() {
            public String name() { return "MyAttr"; }
            public CA3 readAttribute(AttributedElement e, ClassReader cr, int p) { return new CA3(this); }
            public void writeAttribute(BufWriter bw, CA3 a) {
                cap2[0] = bw;
                check(bw.constantPool() != null, "constantPool");
                check(bw.canWriteDirect(bw.constantPool()), "canWriteDirect");
                int before = bw.size();
                bw.writeIndex(a.attributeName());
                bw.writeInt(2);
                bw.writeBytes(new byte[]{1,2});
                check(bw.size() == before + 8, "attribute name+length+content");
                bw.writeU1(7);
                check(bw.size() == before + 9, "writeU1 +1");
                bw.writeU2(300);
                check(bw.size() == before + 11, "writeU2 +2");
                bw.writeInt(70000);
                check(bw.size() == before + 15, "writeInt +4");
                bw.writeBytes(new byte[]{9,9});
                check(bw.size() == before + 17, "writeBytes +2");
            }
            public AttributeStability stability() { return AttributeStability.CP_REFS; }
        };
        ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.with(new CA3(mapper)));
        check(cap2[0] != null, "BufWriter captured");

    }

    // ---- Part 3: 全原语写方法 size() 精确增量表（实测：u1=1/u2=2/int=float=4/long=double=8/bytes=N） ----
    static void testPrimitiveSizeTable() throws Exception {
        final List<String> log = new ArrayList<>();
        final BufWriter[] cap3 = new BufWriter[1];
        AttributeMapper<CA> mp = new AttributeMapper<>() {
            public String name() { return "BwAttr"; }
            public CA readAttribute(AttributedElement e, ClassReader cr, int p) { return new CA(this); }
            public void writeAttribute(BufWriter bw, CA a) {
                cap3[0] = bw;
                bw.writeIndex(a.attributeName());                    // 头：u2 名索引
                bw.writeInt(0);                                     // 头：u4 长度占位（mapper 自责，见 Part 4）
                int before = bw.size();
                bw.writeU1(0xAB);                 log.add("u1:" + (bw.size() - before));
                before = bw.size();
                bw.writeU2(0xCAFE);               log.add("u2:" + (bw.size() - before));
                before = bw.size();
                bw.writeInt(0x11223344);          log.add("int:" + (bw.size() - before));
                before = bw.size();
                bw.writeFloat(1.5f);              log.add("float:" + (bw.size() - before));
                before = bw.size();
                bw.writeLong(0x1122334455667788L); log.add("long:" + (bw.size() - before));
                before = bw.size();
                bw.writeDouble(0.5);              log.add("double:" + (bw.size() - before));
                before = bw.size();
                bw.writeBytes(new byte[]{1, 2, 3}); log.add("bytes3:" + (bw.size() - before));
                before = bw.size();
                bw.writeBytes(new byte[]{9, 9, 9, 9}, 1, 2); log.add("bytesOffLen:" + (bw.size() - before));
                before = bw.size();
                bw.writeIndex(bw.constantPool().utf8Entry("bwz")); log.add("writeIndex:" + (bw.size() - before));
                before = bw.size();
                bw.writeIndexOrZero(null);        log.add("idxOrZeroNull:" + (bw.size() - before));
                before = bw.size();
                bw.writeIndexOrZero(bw.constantPool().utf8Entry("bwnn")); log.add("idxOrZeroEntry:" + (bw.size() - before));
                before = bw.size();
                bw.writeIntBytes(2, 0xAABBCCDDEEFF0011L); log.add("intBytes2:" + (bw.size() - before));
                before = bw.size();
                bw.writeIntBytes(4, 0xAABBCCDDEEFF0011L); log.add("intBytes4:" + (bw.size() - before));
                // reserveSpace：仅预留容量，size 不前进（字节码级实测：只 grow 不动 offset）；后续 writeInt 照常 +4
                before = bw.size();
                bw.reserveSpace(4);
                log.add("reserve4:" + (bw.size() - before));
                before = bw.size();
                bw.writeInt(99);
                log.add("afterReserveInt:" + (bw.size() - before));
            }
            public AttributeStability stability() { return AttributeStability.STATELESS; }
        };
        byte[] b = ClassFile.of(ClassFile.AttributeMapperOption.of(e -> e.equalsString("BwAttr") ? mp : null))
                .build(ClassDesc.of("BwT"), cb -> cb.with(new CA(mp)));
        check(cap3[0] != null, "BufWriter captured (size table)");
        check(log.equals(List.of("u1:1", "u2:2", "int:4", "float:4", "long:8", "double:8", "bytes3:3", "bytesOffLen:2",
                "writeIndex:2", "idxOrZeroNull:2", "idxOrZeroEntry:2", "intBytes2:2", "intBytes4:4", "reserve4:0", "afterReserveInt:4")),
                "primitive size increment table: " + log);
        check(b.length > 8, "built bytes non-empty");
        // canWriteDirect：自身池 true；别的池 false（实测）
        final boolean[] direct = new boolean[2];
        AttributeMapper<CA> mp2 = new AttributeMapper<>() {
            public String name() { return "BwAttr2"; }
            public CA readAttribute(AttributedElement e, ClassReader cr, int p) { return new CA(this); }
            public void writeAttribute(BufWriter bw, CA a) {
                direct[0] = bw.canWriteDirect(bw.constantPool());
                direct[1] = bw.canWriteDirect(ConstantPoolBuilder.of());
            }
            public AttributeStability stability() { return AttributeStability.STATELESS; }
        };
        ClassFile.of(ClassFile.AttributeMapperOption.of(e -> e.equalsString("BwAttr2") ? mp2 : null))
                .build(ClassDesc.of("BwU"), cb -> cb.with(new CA(mp2)));
        check(direct[0], "canWriteDirect(own pool) true");
        check(!direct[1], "canWriteDirect(foreign pool) false");
    }

    // ---- Part 4: patchInt 原地改写 → re-parse 回读闭环（实测锁定 writeAttribute 布局语义） ----
    // 布局契约（实测）：writeAttribute 负责写完整属性（u2 名索引 + u4 长度 + payload）；
    // BufWriter 位置是类文件缓冲的绝对偏移（进入回调时 bw.size() 已 > 0），patchInt 偏移须以 size() 为基。
    static final int[] rd = new int[4];
    static final AttributeMapper<CA> PATCH_MAPPER = new AttributeMapper<>() {
        public String name() { return "BwPatch"; }
        public CA readAttribute(AttributedElement e, ClassReader cr, int p) {
            rd[0] = cr.readU2(p - 6);      // 名索引
            rd[1] = cr.readInt(p - 4);     // 长度字段
            rd[2] = cr.readInt(p);         // payload A（patch u4 目标）
            rd[3] = cr.readU2(p + 4);      // payload B（patch u2 目标）
            return new CA(this);
        }
        public void writeAttribute(BufWriter bw, CA a) {
            int base = bw.size();
            bw.writeIndex(a.attributeName());
            bw.writeInt(8);                // 长度字段 = payload 8 字节（mapper 责任，框架不代填——实测）
            bw.writeInt(0);                // payload A 占位
            bw.writeU2(0);                 // payload B 占位
            bw.patchInt(base + 6, 4, 0x11223344);  // u4 原地改写
            bw.patchInt(base + 10, 2, 0xABCD);     // u2 原地改写
            bw.writeU2(0);                 // 填充：payload 必须与长度字段严格一致（实测不一致 → parse "too big"）
        }
        public AttributeStability stability() { return AttributeStability.STATELESS; }
    };
    static void testPatchIntRoundtrip() throws Exception {
        ClassFile cf = ClassFile.of(ClassFile.AttributeMapperOption.of(e -> e.equalsString("BwPatch") ? PATCH_MAPPER : null));
        byte[] b = cf.build(ClassDesc.of("BwP"), cb -> cb.with(new CA(PATCH_MAPPER)));
        ClassModel cm = cf.parse(b);  // 触发 readAttribute
        check(cm.attributes().size() == 1, "patched attribute reparsed");
        check(rd[0] > 0, "name index valid u2");
        check(rd[1] == 8, "length field 8 (mapper-written)");
        check(rd[2] == 0x11223344, "patchInt u4 in-place rewrite visible after reparse: 0x" + Integer.toHexString(rd[2]));
        check(rd[3] == 0xABCD, "patchInt u2 in-place rewrite: 0x" + Integer.toHexString(rd[3]));
        // patchInt 不改变 size（原地）：在写属性时前后 size 差 == 头 6 + payload 8
        final int[] delta = new int[1];
        AttributeMapper<CA> mp3 = new AttributeMapper<>() {
            public String name() { return "BwPatch2"; }
            public CA readAttribute(AttributedElement e, ClassReader cr, int p) { return new CA(this); }
            public void writeAttribute(BufWriter bw, CA a) {
                int start = bw.size();
                bw.writeIndex(a.attributeName());
                bw.writeInt(4);
                bw.writeInt(0x55667788);
                int at = bw.size() - 4;
                bw.patchInt(at, 4, 0x99AABBCC);   // 原地：size 不变
                delta[0] = bw.size() - start;
            }
            public AttributeStability stability() { return AttributeStability.STATELESS; }
        };
        ClassFile.of(ClassFile.AttributeMapperOption.of(e -> e.equalsString("BwPatch2") ? mp3 : null))
                .build(ClassDesc.of("BwQ"), cb -> cb.with(new CA(mp3)));
        check(delta[0] == 10, "patchInt keeps size (6 header + 4 payload): " + delta[0]);
    }

    // ---- Part 5: 属性长度错写 → parse 拒绝（"attribute too big to handle" 负例，实测锁定） ----
    static void testBadLengthNegative() {
        AttributeMapper<CA> bad = new AttributeMapper<>() {
            public String name() { return "BwBad"; }
            public CA readAttribute(AttributedElement e, ClassReader cr, int p) { return new CA(this); }
            public void writeAttribute(BufWriter bw, CA a) {
                bw.writeIndex(a.attributeName());
                bw.writeInt(0x7FFFFFFF);   // 离谱长度
            }
            public AttributeStability stability() { return AttributeStability.STATELESS; }
        };
        ClassFile cf = ClassFile.of(ClassFile.AttributeMapperOption.of(e -> e.equalsString("BwBad") ? bad : null));
        byte[] b = cf.build(ClassDesc.of("BwR"), cb -> cb.with(new CA(bad)));
        try {
            ClassModel m = cf.parse(b);
            m.attributes();   // 惰性读取，触发长度校验
            check(false, "absurd attribute length must be rejected");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("too big"), "too-big IAE msg: " + e.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testPrimitiveSizeTable();
        testPatchIntRoundtrip();
        testBadLengthNegative();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
