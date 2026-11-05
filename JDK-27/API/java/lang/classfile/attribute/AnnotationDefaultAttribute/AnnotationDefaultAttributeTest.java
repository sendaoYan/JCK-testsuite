/*
 * @test
 * @summary java.lang.classfile.attribute.AnnotationDefaultAttribute conformance: white-box structure + black-box behavior.
 * @run main AnnotationDefaultAttributeTest
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

public class AnnotationDefaultAttributeTest {
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
        Class<?> c = Class.forName("java.lang.classfile.attribute.AnnotationDefaultAttribute");
        check(c != null, "java.lang.classfile.attribute.AnnotationDefaultAttribute loads");
        check(c.getName().equals("java.lang.classfile.attribute.AnnotationDefaultAttribute"), "binary name java.lang.classfile.attribute.AnnotationDefaultAttribute");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.attribute.AnnotationDefaultAttribute is public");
        check(c.isInterface(), "java.lang.classfile.attribute.AnnotationDefaultAttribute is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.attribute.AnnotationDefaultAttribute has no superclass");
        check(declares(c, "defaultValue"), "declares defaultValue");
        check(declares(c, "of"), "declares of");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        AnnotationDefaultAttribute a = AnnotationDefaultAttribute.of(AnnotationValue.ofInt(3));
        check(a.defaultValue().tag() == AnnotationValue.TAG_INT, "tag");
        check(((AnnotationValue.OfInt) a.defaultValue()).intValue() == 3, "value");

    }

    // ---- Part 3: 工厂矩阵 + null/u2 负例 + attribute 元数据 ----
    static void testFactoryMatrix() {
        // 全类型 default 值构造（unbound 实例直接回读，无需 build）
        AnnotationDefaultAttribute i = AnnotationDefaultAttribute.of(AnnotationValue.ofInt(3));
        check(i.defaultValue().tag() == AnnotationValue.TAG_INT && ((AnnotationValue.OfInt) i.defaultValue()).intValue() == 3, "ofInt tag+value");
        AnnotationDefaultAttribute s = AnnotationDefaultAttribute.of(AnnotationValue.ofString("sv"));
        check(s.defaultValue().tag() == AnnotationValue.TAG_STRING && ((AnnotationValue.OfString) s.defaultValue()).stringValue().equals("sv"), "ofString");
        AnnotationDefaultAttribute z = AnnotationDefaultAttribute.of(AnnotationValue.ofBoolean(true));
        check(z.defaultValue().tag() == AnnotationValue.TAG_BOOLEAN && ((AnnotationValue.OfBoolean) z.defaultValue()).booleanValue(), "ofBoolean");
        AnnotationDefaultAttribute l = AnnotationDefaultAttribute.of(AnnotationValue.ofLong(9L));
        check(l.defaultValue().tag() == AnnotationValue.TAG_LONG && ((AnnotationValue.OfLong) l.defaultValue()).longValue() == 9L, "ofLong");
        AnnotationDefaultAttribute b = AnnotationDefaultAttribute.of(AnnotationValue.ofByte((byte) 1));
        check(b.defaultValue().tag() == AnnotationValue.TAG_BYTE && ((AnnotationValue.OfByte) b.defaultValue()).byteValue() == 1, "ofByte");
        AnnotationDefaultAttribute c = AnnotationDefaultAttribute.of(AnnotationValue.ofChar('x'));
        check(c.defaultValue().tag() == AnnotationValue.TAG_CHAR && ((AnnotationValue.OfChar) c.defaultValue()).charValue() == 'x', "ofChar");
        AnnotationDefaultAttribute sh = AnnotationDefaultAttribute.of(AnnotationValue.ofShort((short) 2));
        check(sh.defaultValue().tag() == AnnotationValue.TAG_SHORT && ((AnnotationValue.OfShort) sh.defaultValue()).shortValue() == 2, "ofShort");
        AnnotationDefaultAttribute f = AnnotationDefaultAttribute.of(AnnotationValue.ofFloat(1.5f));
        check(f.defaultValue().tag() == AnnotationValue.TAG_FLOAT && ((AnnotationValue.OfFloat) f.defaultValue()).floatValue() == 1.5f, "ofFloat");
        AnnotationDefaultAttribute d = AnnotationDefaultAttribute.of(AnnotationValue.ofDouble(2.5));
        check(d.defaultValue().tag() == AnnotationValue.TAG_DOUBLE && ((AnnotationValue.OfDouble) d.defaultValue()).doubleValue() == 2.5, "ofDouble");
        AnnotationDefaultAttribute e = AnnotationDefaultAttribute.of(AnnotationValue.ofEnum(ClassDesc.of("pkg.E"), "A"));
        check(e.defaultValue().tag() == AnnotationValue.TAG_ENUM
                && ((AnnotationValue.OfEnum) e.defaultValue()).classSymbol().descriptorString().equals("Lpkg/E;")
                && ((AnnotationValue.OfEnum) e.defaultValue()).constantName().stringValue().equals("A"), "ofEnum class+name");
        AnnotationDefaultAttribute cl = AnnotationDefaultAttribute.of(AnnotationValue.ofClass(ClassDesc.of("java.lang.String")));
        check(cl.defaultValue().tag() == AnnotationValue.TAG_CLASS
                && ((AnnotationValue.OfClass) cl.defaultValue()).classSymbol().descriptorString().equals("Ljava/lang/String;"), "ofClass");
        AnnotationDefaultAttribute an = AnnotationDefaultAttribute.of(AnnotationValue.ofAnnotation(Annotation.of(ClassDesc.of("pkg.N"),
                List.of(AnnotationElement.of("m", AnnotationValue.ofInt(1))))));
        check(an.defaultValue().tag() == AnnotationValue.TAG_ANNOTATION
                && ((AnnotationValue.OfAnnotation) an.defaultValue()).annotation().className().stringValue().equals("Lpkg/N;")
                && ((AnnotationValue.OfAnnotation) an.defaultValue()).annotation().elements().size() == 1, "ofAnnotation nested");
        // null 负例
        try {
            AnnotationDefaultAttribute.of(null);
            check(false, "of(null) must NPE");
        } catch (NullPointerException ex) {
            check(ex.getMessage() != null || true, "of(null) NPE");
        }
        // 数组 default 的 list 尺寸 u2 边界（与 AnnotationValue.ofArray 共享校验）
        check(AnnotationValue.ofArray(Collections.nCopies(65535, AnnotationValue.ofInt(0))).values().size() == 65535, "ofArray 65535 ok");
        try {
            AnnotationValue.ofArray(Collections.nCopies(66000, AnnotationValue.ofInt(0)));
            check(false, "ofArray 66000 must IAE");
        } catch (IllegalArgumentException ex) {
            check(ex.getMessage() != null && ex.getMessage().contains("u2"), "ofArray 66000 IAE msg: " + ex.getMessage());
        }
        // attribute 元数据：规范名 / mapper 注册表身份 / stability / allowMultiple
        AnnotationDefaultAttribute a = AnnotationDefaultAttribute.of(AnnotationValue.ofInt(1));
        check(a.attributeName().stringValue().equals(Attributes.NAME_ANNOTATION_DEFAULT), "attributeName==NAME_ANNOTATION_DEFAULT");
        check(a.attributeName().stringValue().equals("AnnotationDefault"), "attributeName literal");
        check(a.attributeMapper() == Attributes.annotationDefault(), "attributeMapper==Attributes.annotationDefault()");
        check(a.attributeMapper().stability() == AttributeMapper.AttributeStability.CP_REFS, "stability CP_REFS");
        check(!a.attributeMapper().allowMultiple(), "allowMultiple false");
        check(Attributes.annotationDefault().name().equals("AnnotationDefault"), "mapper.name()");
    }

    // ---- Part 4: build 注入 → re-parse 回读闭环 ----
    static void testBuildRoundtrip() {
        AnnotationValue arr = AnnotationValue.ofArray(
                AnnotationValue.ofInt(7), AnnotationValue.ofString("x"), AnnotationValue.ofBoolean(true),
                AnnotationValue.ofLong(9L), AnnotationValue.ofByte((byte) 1), AnnotationValue.ofChar('c'),
                AnnotationValue.ofShort((short) 2), AnnotationValue.ofFloat(1.5f), AnnotationValue.ofDouble(2.5),
                AnnotationValue.ofEnum(ClassDesc.of("pkg.E"), "A"), AnnotationValue.ofClass(ClassDesc.of("java.lang.String")));
        byte[] bytes = ClassFile.of().build(ClassDesc.of("ADT1"), cb -> cb
                .withMethod("value", MethodTypeDesc.ofDescriptor("()I"), ClassFile.ACC_PUBLIC | ClassFile.ACC_ABSTRACT,
                        mb -> mb.with(AnnotationDefaultAttribute.of(arr))));
        MethodModel mm = ClassFile.of().parse(bytes).methods().get(0);
        var found = mm.findAttribute(Attributes.annotationDefault());
        check(found.isPresent(), "roundtrip attribute present");
        AnnotationValue dv = found.get().defaultValue();
        check(dv.tag() == AnnotationValue.TAG_ARRAY, "roundtrip default is array");
        List<AnnotationValue> vals = ((AnnotationValue.OfArray) dv).values();
        check(vals.size() == 11, "roundtrip array size 11");
        check(((AnnotationValue.OfInt) vals.get(0)).intValue() == 7, "rt int");
        check(((AnnotationValue.OfString) vals.get(1)).stringValue().equals("x"), "rt string");
        check(((AnnotationValue.OfBoolean) vals.get(2)).booleanValue(), "rt boolean");
        check(((AnnotationValue.OfLong) vals.get(3)).longValue() == 9L, "rt long");
        check(((AnnotationValue.OfByte) vals.get(4)).byteValue() == 1, "rt byte");
        check(((AnnotationValue.OfChar) vals.get(5)).charValue() == 'c', "rt char");
        check(((AnnotationValue.OfShort) vals.get(6)).shortValue() == 2, "rt short");
        check(((AnnotationValue.OfFloat) vals.get(7)).floatValue() == 1.5f, "rt float");
        check(((AnnotationValue.OfDouble) vals.get(8)).doubleValue() == 2.5, "rt double");
        check(((AnnotationValue.OfEnum) vals.get(9)).constantName().stringValue().equals("A")
                && ((AnnotationValue.OfEnum) vals.get(9)).classSymbol().descriptorString().equals("Lpkg/E;"), "rt enum");
        check(((AnnotationValue.OfClass) vals.get(10)).classSymbol().descriptorString().equals("Ljava/lang/String;"), "rt class");
        // 属性在 method attributes 列表中可见（AnnotationDefaultAttribute 是 MethodElement）
        boolean inAttrs = false;
        for (Attribute<?> at : mm.attributes()) if (at instanceof AnnotationDefaultAttribute) inAttrs = true;
        check(inAttrs, "attribute visible in method attributes list");
        check(mm.findAttribute(Attributes.annotationDefault()).get().attributeName().stringValue().equals("AnnotationDefault"), "bound attributeName");
    }

    // ---- Part 5: javac 真类产物（@interface 各类型 default 矩阵）----
    @interface RealAnno {
        int v() default 7;
        String s() default "def";
        boolean b() default true;
        long l() default 42L;
        int[] arr() default {1, 2};
        Class<?> c() default Object.class;
        RetentionPolicySrc e() default RetentionPolicySrc.SOURCE;
        byte by() default 3;
        char ch() default 'z';
        short sh() default 4;
        float f() default 1.5f;
        double d() default 2.5;
        String nodefault();
    }
    enum RetentionPolicySrc { SOURCE, CLASS }

    static void testRealClass() throws Exception {
        byte[] self = AnnotationDefaultAttributeTest.class
                .getResourceAsStream("AnnotationDefaultAttributeTest$RealAnno.class").readAllBytes();
        ClassModel cm = ClassFile.of().parse(self);
        Map<String, AnnotationValue> defs = new HashMap<>();
        for (MethodModel mm : cm.methods()) {
            var ad = mm.findAttribute(Attributes.annotationDefault());
            if (ad.isPresent()) defs.put(mm.methodName().stringValue(), ad.get().defaultValue());
        }
        check(defs.size() == 12, "12 methods carry defaults (nodefault excluded): " + defs.size());
        check(cm.methods().size() == 13, "@interface declares 13 methods");
        check(((AnnotationValue.OfInt) defs.get("v")).intValue() == 7, "real v=7");
        check(((AnnotationValue.OfString) defs.get("s")).stringValue().equals("def"), "real s=def");
        check(((AnnotationValue.OfBoolean) defs.get("b")).booleanValue(), "real b=true");
        check(((AnnotationValue.OfLong) defs.get("l")).longValue() == 42L, "real l=42");
        List<AnnotationValue> arr = ((AnnotationValue.OfArray) defs.get("arr")).values();
        check(arr.size() == 2 && ((AnnotationValue.OfInt) arr.get(0)).intValue() == 1 && ((AnnotationValue.OfInt) arr.get(1)).intValue() == 2, "real arr=[1,2]");
        check(((AnnotationValue.OfClass) defs.get("c")).classSymbol().descriptorString().equals("Ljava/lang/Object;"), "real c=Object");
        AnnotationValue.OfEnum en = (AnnotationValue.OfEnum) defs.get("e");
        check(en.classSymbol().descriptorString().endsWith("RetentionPolicySrc;") && en.constantName().stringValue().equals("SOURCE"), "real e=SOURCE");
        check(((AnnotationValue.OfByte) defs.get("by")).byteValue() == 3, "real by=3");
        check(((AnnotationValue.OfChar) defs.get("ch")).charValue() == 'z', "real ch=z");
        check(((AnnotationValue.OfShort) defs.get("sh")).shortValue() == 4, "real sh=4");
        check(((AnnotationValue.OfFloat) defs.get("f")).floatValue() == 1.5f, "real f=1.5f");
        check(((AnnotationValue.OfDouble) defs.get("d")).doubleValue() == 2.5, "real d=2.5");
        // tag 表（JVMS element_value tags）
        check(defs.get("v").tag() == 'I' && defs.get("s").tag() == 's' && defs.get("b").tag() == 'Z' && defs.get("l").tag() == 'J'
                && defs.get("arr").tag() == '[' && defs.get("c").tag() == 'c' && defs.get("e").tag() == 'e'
                && defs.get("by").tag() == 'B' && defs.get("ch").tag() == 'C' && defs.get("sh").tag() == 'S'
                && defs.get("f").tag() == 'F' && defs.get("d").tag() == 'D', "tag table I s Z J [ c e B C S F D");
        // 非 @interface 类不产 AnnotationDefault
        byte[] outer = AnnotationDefaultAttributeTest.class
                .getResourceAsStream("AnnotationDefaultAttributeTest.class").readAllBytes();
        boolean any = false;
        for (MethodModel mm : ClassFile.of().parse(outer).methods())
            any |= mm.findAttribute(Attributes.annotationDefault()).isPresent();
        check(!any, "plain class has no AnnotationDefault");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testFactoryMatrix();
        testBuildRoundtrip();
        testRealClass();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
