/*
 * @test
 * @summary java.lang.classfile.attribute.CharacterRangeInfo conformance: white-box structure + black-box behavior.
 * @run main CharacterRangeInfoTest
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

public class CharacterRangeInfoTest {
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
        Class<?> c = Class.forName("java.lang.classfile.attribute.CharacterRangeInfo");
        check(c != null, "java.lang.classfile.attribute.CharacterRangeInfo loads");
        check(c.getName().equals("java.lang.classfile.attribute.CharacterRangeInfo"), "binary name java.lang.classfile.attribute.CharacterRangeInfo");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.attribute.CharacterRangeInfo is public");
        check(c.isInterface(), "java.lang.classfile.attribute.CharacterRangeInfo is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.attribute.CharacterRangeInfo has no superclass");
        check(declares(c, "startPc"), "declares startPc");
        check(declares(c, "endPc"), "declares endPc");
        check(declares(c, "characterRangeStart"), "declares characterRangeStart");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        CharacterRangeInfo i = CharacterRangeInfo.of(1, 6, 2, 12, 0);
        check(i.startPc() == 1, "startPc");
        check(i.endPc() == 6, "endPc");
        check(i.characterRangeStart() == 2, "characterRangeStart");
        check(i.characterRangeEnd() == 12, "characterRangeEnd");
        check(i.flags() == 0, "flags");

    }

    // ---- Part 3: 工厂参数序 + 访问器全等 ----
    static void testFactoryOrder() {
        CharacterRangeInfo i = CharacterRangeInfo.of(3, 17, 1000, 1020, 0x0005);
        check(i.startPc() == 3, "of arg0 -> startPc");
        check(i.endPc() == 17, "of arg1 -> endPc");
        check(i.characterRangeStart() == 1000, "of arg2 -> characterRangeStart");
        check(i.characterRangeEnd() == 1020, "of arg3 -> characterRangeEnd");
        check(i.flags() == 0x0005, "of arg4 -> flags");
        // 零值与极值 u2 边界合法
        CharacterRangeInfo z = CharacterRangeInfo.of(0, 0, 0, 0, 0);
        check(z.startPc() == 0 && z.endPc() == 0 && z.characterRangeStart() == 0 && z.characterRangeEnd() == 0 && z.flags() == 0, "all-zero ok");
        CharacterRangeInfo mx = CharacterRangeInfo.of(0, 65535, 0, 0, 65535);
        check(mx.endPc() == 65535 && mx.flags() == 65535, "u2 max edge ok");
    }

    // ---- Part 4: u2 边界负例（startPc/endPc/flags 均 checkU2）----
    static void testUBoundaries() {
        try { CharacterRangeInfo.of(-1, 0, 0, 0, 0); check(false, "neg startPc must IAE"); }
        catch (IllegalArgumentException e) { check(e.getMessage() != null && e.getMessage().contains("start pc"), "startPc IAE msg: " + e.getMessage()); }
        try { CharacterRangeInfo.of(0, 65536, 0, 0, 0); check(false, "endPc 65536 must IAE"); }
        catch (IllegalArgumentException e) { check(e.getMessage() != null && e.getMessage().contains("end pc"), "endPc IAE msg"); }
        try { CharacterRangeInfo.of(0, 0, 0, 0, -1); check(false, "neg flags must IAE"); }
        catch (IllegalArgumentException e) { check(e.getMessage() != null && e.getMessage().contains("flags"), "flags IAE msg"); }
        try { CharacterRangeInfo.of(0, 0, 0, 0, 65536); check(false, "flags 65536 must IAE"); }
        catch (IllegalArgumentException e) { check(e.getMessage() != null && e.getMessage().contains("flags"), "flags 65536 IAE msg"); }
        // characterRangeStart/End 是 u4 语义（源位置），不做 u2 校验：可承载任意 int
        check(CharacterRangeInfo.of(0, 0, -1, 0, 0).characterRangeStart() == -1, "characterRangeStart unvalidated");
        check(CharacterRangeInfo.of(0, 0, 0, 0x7FFFFFFF, 0).characterRangeEnd() == 0x7FFFFFFF, "characterRangeEnd int max");
        check(CharacterRangeInfo.of(0, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, 0).characterRangeStart() == Integer.MIN_VALUE, "full int range");
    }

    // ---- Part 5: 发射→re-parse 回读闭环（经 CodeBuilder.characterRange → CRT 属性）----
    static void testEmissionRoundtrip() {
        byte[] b = ClassFile.of().build(ClassDesc.of("CRIT1"), cb -> cb
                .withMethodBody("m", MethodTypeDesc.ofDescriptor("()V"), 0, c -> {
                    Label s = c.startLabel(), e = c.endLabel();
                    c.characterRange(s, e, 3000, 3040, 0x0002).return_();
                }));
        CodeModel code = ClassFile.of().parse(b).methods().get(0).code().get();
        var crt = code.findAttribute(Attributes.characterRangeTable());
        check(crt.isPresent(), "characterRange emission produces CRT attribute");
        check(crt.get().characterRangeTable().size() == 1, "CRT 1 entry");
        CharacterRangeInfo r = crt.get().characterRangeTable().get(0);
        check(r.startPc() == 0 && r.endPc() == 0, "emitted pc range covers the sole return_ (start 0, inclusive end 0)");
        check(r.characterRangeStart() == 3000 && r.characterRangeEnd() == 3040, "emitted range 3000..3040");
        check(r.flags() == 0x0002, "emitted flags 0x2");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testFactoryOrder();
        testUBoundaries();
        testEmissionRoundtrip();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
