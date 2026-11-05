/*
 * @test
 * @summary java.lang.classfile.CustomAttribute conformance: white-box structure + black-box behavior.
 * @run main CustomAttributeTest
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

public class CustomAttributeTest {
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
        Class<?> c = Class.forName("java.lang.classfile.CustomAttribute");
        check(c != null, "java.lang.classfile.CustomAttribute loads");
        check(c.getName().equals("java.lang.classfile.CustomAttribute"), "binary name java.lang.classfile.CustomAttribute");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.CustomAttribute is public");
        check(!c.isInterface() && !c.isEnum(), "java.lang.classfile.CustomAttribute is a class");
        check(declares(c, "attributeMapper"), "declares attributeMapper");
        check(declares(c, "attributeName"), "declares attributeName");
        check(declares(c, "toString"), "declares toString");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        AttributeMapper<CA> mapper = new AttributeMapper<CA>() {
            public String name() { return "MyAttr"; }
            public CA readAttribute(AttributedElement e, ClassReader cr, int p) { return new CA(this); }
            public void writeAttribute(BufWriter bw, CA a) { bw.writeIndex(a.attributeName()); bw.writeInt(3); bw.writeBytes(new byte[]{1,2,3}); }
            public AttributeStability stability() { return AttributeStability.CP_REFS; }
        };
        CA a = new CA(mapper);
        check(a.attributeName().stringValue().equals("MyAttr"), "attributeName");
        check(a.attributeMapper() == mapper, "attributeMapper");
        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.with(a));
        check(ClassFile.of(ClassFile.AttributeMapperOption.of(u -> { return u.equalsString("MyAttr") ? mapper : null; })).parse(b).findAttribute(mapper).isPresent(), "custom attr round-trip");

    }

    // ---- Part 3: payload 字节串真 round-trip（mapper readBytes 读回 writeAttribute 写的字节；实测布局：u4 len + bytes，attribute_length=4+len）----
    static class PayloadAttribute extends CustomAttribute<PayloadAttribute> {
        final byte[] payload;
        PayloadAttribute(AttributeMapper<PayloadAttribute> m, byte[] payload) { super(m); this.payload = payload; }
    }

    static final byte[] SAMPLE_PAYLOAD = {9, 8, 7, 6, 5, 4, 3, 2, 1, 0, (byte) 0xFF, (byte) 0xCA, (byte) 0xFE};

    static final AttributeMapper<PayloadAttribute> PAYLOAD_MAPPER = new AttributeMapper<>() {
        public String name() { return "PayloadAttr"; }
        public PayloadAttribute readAttribute(AttributedElement encl, ClassReader cr, int pos) {
            int len = cr.readInt(pos);                    // 载荷布局：u4 长度 + 原始字节
            return new PayloadAttribute(this, cr.readBytes(pos + 4, len));
        }
        public void writeAttribute(BufWriter bw, PayloadAttribute a) {
            bw.writeIndex(bw.constantPool().utf8Entry(name()));
            bw.writeInt(4 + a.payload.length);            // attribute_length 覆盖载荷全量
            bw.writeInt(a.payload.length);
            bw.writeBytes(a.payload);
        }
        public boolean allowMultiple() { return true; }
        public AttributeStability stability() { return AttributeStability.STATELESS; }
    };

    static ClassFile payloadCf() {
        return ClassFile.of(ClassFile.AttributeMapperOption.of(u -> u.equalsString("PayloadAttr") ? PAYLOAD_MAPPER : null));
    }

    static void testPayloadRoundTrip() throws Exception {
        ClassFile reg = payloadCf();
        byte[] b = reg.build(ClassDesc.of("CAR1"), cb -> cb.with(new PayloadAttribute(PAYLOAD_MAPPER, SAMPLE_PAYLOAD)));
        ClassModel cm = reg.parse(b);
        List<PayloadAttribute> found = cm.findAttributes(PAYLOAD_MAPPER);
        check(found.size() == 1, "payload attr found via custom mapper");
        check(Arrays.equals(found.get(0).payload, SAMPLE_PAYLOAD), "payload bytes round-trip equals: " + Arrays.toString(found.get(0).payload));
        check(found.get(0).attributeMapper() == PAYLOAD_MAPPER, "roundtrip mapper identity");
        check(found.get(0).attributeName().stringValue().equals("PayloadAttr"), "roundtrip attribute name");
        // 二次往返（再 transform 一次再读）仍等
        byte[] t = reg.transformClass(cm, ClassTransform.ACCEPT_ALL);
        check(Arrays.equals(reg.parse(t).findAttributes(PAYLOAD_MAPPER).get(0).payload, SAMPLE_PAYLOAD), "payload survives transform ACCEPT_ALL");
        // 空载荷
        byte[] b2 = reg.build(ClassDesc.of("CAR2"), cb -> cb.with(new PayloadAttribute(PAYLOAD_MAPPER, new byte[0])));
        check(reg.parse(b2).findAttributes(PAYLOAD_MAPPER).get(0).payload.length == 0, "empty payload round-trip");
    }

    // ---- Part 4: allowMultiple=true 注入两个同名自定义属性 → re-parse 得两个（实测）----
    static void testAllowMultiple() throws Exception {
        ClassFile reg = payloadCf();
        byte[] b = reg.build(ClassDesc.of("CAM"), cb -> cb
                .with(new PayloadAttribute(PAYLOAD_MAPPER, SAMPLE_PAYLOAD))
                .with(new PayloadAttribute(PAYLOAD_MAPPER, new byte[]{1, 2, 3})));
        ClassModel cm = reg.parse(b);
        List<PayloadAttribute> attrs = cm.findAttributes(PAYLOAD_MAPPER);
        check(attrs.size() == 2, "two same-name custom attrs retained: " + attrs.size());
        check(Arrays.equals(attrs.get(0).payload, SAMPLE_PAYLOAD), "first payload in order");
        check(Arrays.equals(attrs.get(1).payload, new byte[]{1, 2, 3}), "second payload in order");
        check(cm.attributes().size() == 2, "attributes() sees both");
        // allowMultiple=false 的同名属性注入两个：build 成功但语义未定义唯一性——只断言不抛
        AttributeMapper<PayloadAttribute> once = new AttributeMapper<>() {
            public String name() { return "OnceAttr"; }
            public PayloadAttribute readAttribute(AttributedElement e, ClassReader cr, int p) { return new PayloadAttribute(this, new byte[0]); }
            public void writeAttribute(BufWriter bw, PayloadAttribute a) { bw.writeIndex(bw.constantPool().utf8Entry("OnceAttr")); bw.writeInt(0); }
            public AttributeStability stability() { return AttributeStability.STATELESS; }
        };
        ClassFile onceCf = ClassFile.of(ClassFile.AttributeMapperOption.of(u -> u.equalsString("OnceAttr") ? once : null));
        byte[] b2 = onceCf.build(ClassDesc.of("CAM2"), cb -> cb.with(new PayloadAttribute(once, new byte[0])));
        check(onceCf.parse(b2).findAttribute(once).isPresent(), "single allowMultiple=false attr round-trips");
    }

    // ---- Part 5: 未注册 mapper——parse 不抛、以 UnknownAttribute 保留 attributes() 非空（实测）；transform+DROP_UNKNOWN_ATTRIBUTES 剥除 ----
    static void testUnregisteredSemantics() throws Exception {
        ClassFile reg = payloadCf();
        ClassFile plain = ClassFile.of();
        byte[] b = reg.build(ClassDesc.of("CAU"), cb -> cb
                .with(new PayloadAttribute(PAYLOAD_MAPPER, SAMPLE_PAYLOAD))
                .with(new PayloadAttribute(PAYLOAD_MAPPER, new byte[]{1})));
        // 未注册环境 parse：不抛、保留两个、名字保留、mapper.stability()==UNKNOWN
        ClassModel cm = plain.parse(b);
        check(cm.attributes().size() == 2, "unregistered parse keeps both custom attrs");
        for (Attribute<?> a : cm.attributes()) {
            check(a.attributeName().stringValue().equals("PayloadAttr"), "unregistered name preserved");
            check(a.attributeMapper().name().equals("PayloadAttr"), "unregistered fallback mapper name");
            check(a.attributeMapper().stability() == AttributeMapper.AttributeStability.UNKNOWN, "unregistered stability UNKNOWN");
        }
        // transform + DROP_UNKNOWN_ATTRIBUTES → 剥（facts §5 实测）
        byte[] dropped = plain.withOptions(ClassFile.AttributesProcessingOption.DROP_UNKNOWN_ATTRIBUTES)
                .transformClass(cm, ClassTransform.ACCEPT_ALL);
        check(plain.parse(dropped).attributes().isEmpty(), "DROP_UNKNOWN_ATTRIBUTES strips custom attrs");
        // 注册环境 transform 同一产物 → 保留（对照）
        byte[] kept = reg.transformClass(cm, ClassTransform.ACCEPT_ALL);
        check(reg.parse(kept).attributes().size() == 2, "registered transform keeps custom attrs");
        // DROP_UNSTABLE_ATTRIBUTES 同样剥未注册属性（实测 0）
        byte[] dropped2 = plain.withOptions(ClassFile.AttributesProcessingOption.DROP_UNSTABLE_ATTRIBUTES)
                .transformClass(cm, ClassTransform.ACCEPT_ALL);
        check(plain.parse(dropped2).attributes().isEmpty(), "DROP_UNSTABLE_ATTRIBUTES also strips unknown custom attrs");
    }

    // ---- Part 6: CustomAttribute 类型面——同时是 Attribute/CodeElement/ClassElement/MethodElement/FieldElement ----
    static void testTypeSurface() throws Exception {
        check(Attribute.class.isAssignableFrom(CustomAttribute.class), "CustomAttribute is-a Attribute");
        check(CodeElement.class.isAssignableFrom(CustomAttribute.class), "CustomAttribute is-a CodeElement");
        check(ClassElement.class.isAssignableFrom(CustomAttribute.class), "CustomAttribute is-a ClassElement");
        check(MethodElement.class.isAssignableFrom(CustomAttribute.class), "CustomAttribute is-a MethodElement");
        check(FieldElement.class.isAssignableFrom(CustomAttribute.class), "CustomAttribute is-a FieldElement");
        // attributeMapper() 为 final（白盒）
        check(java.lang.reflect.Modifier.isFinal(CustomAttribute.class.getMethod("attributeMapper").getModifiers()), "attributeMapper() final");
        check(java.lang.reflect.Modifier.isProtected(CustomAttribute.class.getDeclaredConstructors()[0].getModifiers()), "constructor protected");
        // toString 含属性名
        PayloadAttribute pa = new PayloadAttribute(PAYLOAD_MAPPER, SAMPLE_PAYLOAD);
        check(pa.toString() != null && pa.toString().contains("PayloadAttr"), "toString mentions name: " + pa.toString());
        // 构造即绑定 mapper（attributeMapper==传入实例；attributeName 走 mapper.name()）
        check(pa.attributeMapper() == PAYLOAD_MAPPER, "constructor binds mapper");
        check(pa.attributeName().stringValue().equals("PayloadAttr"), "attributeName from mapper.name()");
        // 自定义属性可挂到方法/字段/代码体（多位置可用性）
        ClassFile reg = payloadCf();
        byte[] b = reg.build(ClassDesc.of("CAS"), cb -> cb
                .with(new PayloadAttribute(PAYLOAD_MAPPER, SAMPLE_PAYLOAD))
                .withField("x", ConstantDescs.CD_int, fb -> fb.with(new PayloadAttribute(PAYLOAD_MAPPER, SAMPLE_PAYLOAD)))
                .withMethod("m", ConstantDescs.MTD_void, 0, mb -> mb
                        .withCode(cob -> cob.return_())
                        .with(new PayloadAttribute(PAYLOAD_MAPPER, SAMPLE_PAYLOAD))));
        ClassModel cm = reg.parse(b);
        check(cm.findAttribute(PAYLOAD_MAPPER).isPresent(), "custom attr on class");
        check(cm.fields().get(0).findAttribute(PAYLOAD_MAPPER).isPresent(), "custom attr on field");
        check(cm.methods().get(0).findAttribute(PAYLOAD_MAPPER).isPresent(), "custom attr on method");
        check(Arrays.equals(cm.fields().get(0).findAttribute(PAYLOAD_MAPPER).get().payload, SAMPLE_PAYLOAD), "field-position payload round-trip");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testPayloadRoundTrip();
        testAllowMultiple();
        testUnregisteredSemantics();
        testTypeSurface();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
