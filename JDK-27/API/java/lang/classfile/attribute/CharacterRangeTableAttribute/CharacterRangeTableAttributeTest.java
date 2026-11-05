/*
 * @test
 * @summary java.lang.classfile.attribute.CharacterRangeTableAttribute conformance: white-box structure + black-box behavior.
 * @run main CharacterRangeTableAttributeTest
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

public class CharacterRangeTableAttributeTest {
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
        Class<?> c = Class.forName("java.lang.classfile.attribute.CharacterRangeTableAttribute");
        check(c != null, "java.lang.classfile.attribute.CharacterRangeTableAttribute loads");
        check(c.getName().equals("java.lang.classfile.attribute.CharacterRangeTableAttribute"), "binary name java.lang.classfile.attribute.CharacterRangeTableAttribute");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.attribute.CharacterRangeTableAttribute is public");
        check(c.isInterface(), "java.lang.classfile.attribute.CharacterRangeTableAttribute is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.attribute.CharacterRangeTableAttribute has no superclass");
        check(declares(c, "characterRangeTable"), "declares characterRangeTable");
        check(declares(c, "of"), "declares of");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        CharacterRangeInfo i = CharacterRangeInfo.of(0, 5, 0, 10, CharacterRange.FLAG_BLOCK);
        CharacterRangeTableAttribute a = CharacterRangeTableAttribute.of(java.util.List.of(i));
        check(a.characterRangeTable().size() == 1, "size");
        check(a.characterRangeTable().get(0).characterRangeEnd() == 10, "end");

    }

    // ---- Part 3: 工厂（unbound）回读 + null/u2 负例 + 元数据 ----
    static void testFactoryMatrix() {
        CharacterRangeInfo c1 = CharacterRangeInfo.of(0, 1, 1000, 1020, 0x0001);
        CharacterRangeInfo c2 = CharacterRangeInfo.of(0, 2, 2000, 2040, 0x0006);
        CharacterRangeTableAttribute a = CharacterRangeTableAttribute.of(List.of(c1, c2));
        check(a.characterRangeTable().size() == 2, "of(List) size");
        check(a.characterRangeTable().get(0) == c1 && a.characterRangeTable().get(1) == c2, "of(List) keeps elements (identity)");
        check(a.characterRangeTable().get(1).endPc() == 2 && a.characterRangeTable().get(1).flags() == 0x0006, "entry accessors");
        check(CharacterRangeTableAttribute.of(List.of()).characterRangeTable().isEmpty(), "empty list ok");
        // null 负例
        try { CharacterRangeTableAttribute.of(null); check(false, "of(null) must NPE"); }
        catch (NullPointerException e) { check(true, "of(null) NPE"); }
        // list 尺寸 u2 边界
        CharacterRangeInfo shared = CharacterRangeInfo.of(0, 0, 0, 0, 0);
        check(CharacterRangeTableAttribute.of(Collections.nCopies(65535, shared)).characterRangeTable().size() == 65535, "65535 entries ok");
        try {
            CharacterRangeTableAttribute.of(Collections.nCopies(66000, shared));
            check(false, "66000 entries must IAE");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("u2"), "66000 IAE msg: " + e.getMessage());
        }
        // attribute 元数据（CRT 允许重复出现 + 引用 code 位置 -> LABELS）
        check(a.attributeName().stringValue().equals(Attributes.NAME_CHARACTER_RANGE_TABLE), "attributeName==NAME_CHARACTER_RANGE_TABLE");
        check(a.attributeName().stringValue().equals("CharacterRangeTable"), "attributeName literal");
        check(a.attributeMapper() == Attributes.characterRangeTable(), "attributeMapper==Attributes.characterRangeTable()");
        check(a.attributeMapper().stability() == AttributeMapper.AttributeStability.LABELS, "stability LABELS");
        check(a.attributeMapper().allowMultiple(), "CRT allowMultiple true");
    }

    // ---- Part 4: 发射→re-parse 回读闭环 ----
    static void testEmissionRoundtrip() {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("CRTT1"), cb -> cb
                .withMethodBody("m", MethodTypeDesc.ofDescriptor("()V"), 0, c -> {
                    Label s = c.startLabel(), e = c.endLabel();
                    c.characterRange(s, e, 100, 140, 0x0001).characterRange(s, e, 200, 240, 0x0002).return_();
                }));
        CodeModel code = cf.parse(b).methods().get(0).code().get();
        var crt = code.findAttribute(Attributes.characterRangeTable());
        check(crt.isPresent(), "emission produces CRT");
        List<CharacterRangeInfo> ts = crt.get().characterRangeTable();
        check(ts.size() == 2, "2 emitted ranges: " + ts.size());
        check(ts.get(0).characterRangeStart() == 100 && ts.get(0).characterRangeEnd() == 140 && ts.get(0).flags() == 0x0001, "range0 exact");
        check(ts.get(1).characterRangeStart() == 200 && ts.get(1).characterRangeEnd() == 240 && ts.get(1).flags() == 0x0002, "range1 exact");
        check(ts.get(0).startPc() == 0 && ts.get(0).endPc() == 0, "pc bounds cover the sole return_ (0..0 inclusive)");
        // DROP_DEBUG 解析交叉：剥 CharacterRange 伪指令但保留 CRT 属性
        CodeModel dropped = cf.withOptions(ClassFile.DebugElementsOption.DROP_DEBUG).parse(b).methods().get(0).code().get();
        int pseudo = 0;
        for (CodeElement el : dropped.elementList()) if (el instanceof CharacterRange) pseudo++;
        check(pseudo == 0, "DROP_DEBUG strips CharacterRange pseudo");
        check(dropped.findAttribute(Attributes.characterRangeTable()).isPresent(), "DROP_DEBUG keeps CRT attribute");
    }

    // ---- Part 5: javac 默认不产 CRT（-Xjcov 才产）----
    static void testRealClass() throws Exception {
        byte[] self = CharacterRangeTableAttributeTest.class
                .getResourceAsStream("CharacterRangeTableAttributeTest.class").readAllBytes();
        boolean any = false;
        for (MethodModel mm : ClassFile.of().parse(self).methods())
            if (mm.code().isPresent()) any |= mm.code().get().findAttribute(Attributes.characterRangeTable()).isPresent();
        check(!any, "javac default emits no CharacterRangeTable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testFactoryMatrix();
        testEmissionRoundtrip();
        testRealClass();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
