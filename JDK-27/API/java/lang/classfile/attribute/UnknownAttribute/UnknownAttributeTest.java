/*
 * @test
 * @summary java.lang.classfile.attribute.UnknownAttribute conformance: white-box structure + black-box behavior.
 * @run main UnknownAttributeTest
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

public class UnknownAttributeTest {
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
        Class<?> c = Class.forName("java.lang.classfile.attribute.UnknownAttribute");
        check(c != null, "java.lang.classfile.attribute.UnknownAttribute loads");
        check(c.getName().equals("java.lang.classfile.attribute.UnknownAttribute"), "binary name java.lang.classfile.attribute.UnknownAttribute");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.attribute.UnknownAttribute is public");
        check(c.isInterface(), "java.lang.classfile.attribute.UnknownAttribute is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.attribute.UnknownAttribute has no superclass");
        check(declares(c, "contents"), "declares contents");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        AttributeMapper<CA> mapper = new AttributeMapper<CA>() {
            public String name() { return "MyAttr"; }
            public CA readAttribute(AttributedElement e, ClassReader cr, int p) { return new CA(this); }
            public void writeAttribute(BufWriter bw, CA a) { bw.writeIndex(a.attributeName()); bw.writeInt(2); bw.writeU2(0xCAFE); }
            public AttributeStability stability() { return AttributeStability.STATELESS; }
        };
        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.with(new CA(mapper)));
        UnknownAttribute ua = null;
        for (Attribute<?> a : ClassFile.of().parse(b).attributes())
            if (a instanceof UnknownAttribute x && x.attributeName().stringValue().equals("MyAttr")) ua = x;
        check(ua != null, "unknown attribute parsed");
        check(ua.contents().length == 2, "contents length 2");
        check((ua.contents()[0] & 0xff) == 0xCA && (ua.contents()[1] & 0xff) == 0xFE, "contents CAFE");
        check(ua.attributeName().stringValue().equals("MyAttr"), "attributeName");

    }


    // ---- 自定义未注册属性（facts §9 模板）：唯一自然产生通道 ----
    static final byte[] UK_PAYLOAD = {1, 2, 3, 4};
    static final AttributeMapper<CA> UK_MAPPER = new AttributeMapper<>() {
        public String name() { return "MyAttr"; }
        public CA readAttribute(AttributedElement encl, ClassReader r, int pos) { return new CA(UK_MAPPER); }
        public void writeAttribute(BufWriter buf, CA attr) {
            buf.writeIndex(buf.constantPool().utf8Entry(name()));
            buf.writeInt(UK_PAYLOAD.length);
            buf.writeBytes(UK_PAYLOAD);
        }
        public AttributeMapper.AttributeStability stability() { return AttributeMapper.AttributeStability.CP_REFS; }
    };

    static byte[] buildWithCustomAttr() {
        return ClassFile.of(ClassFile.AttributeMapperOption.of(e -> e.equalsString("MyAttr") ? UK_MAPPER : null))
                .build(ClassDesc.of("DynUK"), cb -> cb
                        .with(new CA(UK_MAPPER))
                        .withMethod("m", MethodTypeDesc.ofDescriptor("()V"), 0, mb -> mb
                                .with(new CA(UK_MAPPER)).withCode(cob -> cob.return_())));
    }

    // ---- Part 3: 注册 mapper build → 无 mapper parse → UnknownAttribute 呈现 ----
    static void testUnknownPresentation() throws Exception {
        ClassModel m = ClassFile.of().parse(buildWithCustomAttr()); // 未注册 mapper
        UnknownAttribute cls = null;
        for (Attribute<?> at : m.attributes()) if (at instanceof UnknownAttribute u) cls = u;
        check(cls != null, "class-level UnknownAttribute present");
        check(cls.attributeName().stringValue().equals("MyAttr"), "custom name preserved: " + cls.attributeName().stringValue());
        check(Arrays.equals(cls.contents(), UK_PAYLOAD), "payload bytes exact: " + Arrays.toString(cls.contents()));
        UnknownAttribute mth = null;
        for (Attribute<?> at : m.methods().get(0).attributes()) if (at instanceof UnknownAttribute u) mth = u;
        check(mth != null && mth.contents().length == 4, "method-level UnknownAttribute present");
        check(mth.attributeName().stringValue().equals("MyAttr"), "method-level name");
    }

    // ---- Part 4: 选项交叉（DROP_UNKNOWN_ATTRIBUTES 剥除；注册后 readAttribute 走自定义通道）----
    static void testOptionCross() throws Exception {
        ClassModel m = ClassFile.of().parse(buildWithCustomAttr());
        byte[] dropped = ClassFile.of().withOptions(ClassFile.AttributesProcessingOption.DROP_UNKNOWN_ATTRIBUTES)
                .transformClass(m, ClassTransform.ACCEPT_ALL);
        int cnt = 0;
        for (Attribute<?> at : ClassFile.of().parse(dropped).attributes()) if (at instanceof UnknownAttribute) cnt++;
        check(cnt == 0, "DROP_UNKNOWN_ATTRIBUTES strips custom attr");
        // 注册 mapper 的 parse 走 readAttribute 自定义通道（不再 Unknown）
        ClassModel reg = ClassFile.of(ClassFile.AttributeMapperOption.of(e -> e.equalsString("MyAttr") ? UK_MAPPER : null))
                .parse(buildWithCustomAttr());
        boolean sawCustom = false, sawUnknown = false;
        for (Attribute<?> at : reg.attributes()) {
            if (at instanceof UnknownAttribute) sawUnknown = true;
            if (at.attributeName().stringValue().equals("MyAttr") && !(at instanceof UnknownAttribute)) sawCustom = true;
        }
        check(sawCustom, "registered parse reads CustomAttribute");
        check(!sawUnknown, "registered parse no UnknownAttribute");
        // 默认 PASS_ALL_ATTRIBUTES 保留（Part 3 已证）
        check(ClassFile.AttributesProcessingOption.values().length == 3, "AttributesProcessingOption 3 values");
    }

    // ---- Part 5: 白盒面（接口层次 + 无 Attributes.unknown() mapper）----
    static void testWhiteBoxSurface() {
        Class<?> c = UnknownAttribute.class;
        check(c.isInterface(), "is interface");
        check(Attribute.class.isAssignableFrom(c), "extends Attribute");
        check(ClassElement.class.isAssignableFrom(c) && MethodElement.class.isAssignableFrom(c)
                && FieldElement.class.isAssignableFrom(c) && CodeElement.class.isAssignableFrom(c), "4 element faces");
        check(declares(c, "contents"), "declares contents");
        boolean hasUnknownMapper = false;
        for (Method mm : Attributes.class.getMethods()) if (mm.getName().equals("unknown")) hasUnknownMapper = true;
        check(!hasUnknownMapper, "Attributes has no unknown() mapper (only instanceof discovery)");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testUnknownPresentation();
        testOptionCross();
        testWhiteBoxSurface();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
