/*
 * @test
 * @summary java.lang.classfile.AnnotationElement conformance: white-box structure + black-box behavior.
 * @run main AnnotationElementTest
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

public class AnnotationElementTest {
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
        Class<?> c = Class.forName("java.lang.classfile.AnnotationElement");
        check(c != null, "java.lang.classfile.AnnotationElement loads");
        check(c.getName().equals("java.lang.classfile.AnnotationElement"), "binary name java.lang.classfile.AnnotationElement");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.AnnotationElement is public");
        check(c.isInterface(), "java.lang.classfile.AnnotationElement is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.AnnotationElement has no superclass");
        check(declares(c, "name"), "declares name");
        check(declares(c, "value"), "declares value");
        check(declares(c, "of"), "declares of");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        AnnotationElement e = AnnotationElement.ofString("value", "unchecked");
        check(e.name().stringValue().equals("value"), "name");
        check(e.value().tag() == AnnotationValue.TAG_STRING, "string tag");
        check(((AnnotationValue.OfString) e.value()).stringValue().equals("unchecked"), "string value");
        check(AnnotationElement.ofInt("n", 5).value().tag() == AnnotationValue.TAG_INT, "ofInt tag");
        check(AnnotationElement.ofBoolean("b", true).value().tag() == AnnotationValue.TAG_BOOLEAN, "ofBoolean tag");
        check(AnnotationElement.ofLong("l", 9L).value().tag() == AnnotationValue.TAG_LONG, "ofLong tag");
        check(AnnotationElement.ofClass("c", ClassDesc.of("java.lang.String")).value().tag() == AnnotationValue.TAG_CLASS, "ofClass tag");
        check(AnnotationElement.ofArray("a", AnnotationValue.ofInt(1), AnnotationValue.ofInt(2)).value().tag() == AnnotationValue.TAG_ARRAY, "ofArray tag");

    }

    // ---- Part 3: 全工厂族矩阵（javap 锁定 14 个静态 of*）——tag + 类型化访问器逐值断言 ----
    static void testFactoryMatrix() {
        // ofString
        AnnotationElement es = AnnotationElement.ofString("s", "hello");
        check(es.name().stringValue().equals("s"), "ofString name");
        check(es.value() instanceof AnnotationValue.OfString vs && vs.stringValue().equals("hello"), "ofString value");
        check(es.value().tag() == AnnotationValue.TAG_STRING, "ofString tag 's'");
        check(((AnnotationValue.OfString) es.value()).constant() instanceof Utf8Entry, "OfString constant is Utf8Entry");
        // ofInt
        AnnotationElement ei = AnnotationElement.ofInt("i", -42);
        check(ei.value() instanceof AnnotationValue.OfInt vi && vi.intValue() == -42, "ofInt value");
        check(ei.value().tag() == AnnotationValue.TAG_INT, "ofInt tag 'I'");
        // ofLong / ofShort / ofByte / ofChar / ofBoolean
        AnnotationElement el = AnnotationElement.ofLong("l", 9999999999L);
        check(el.value() instanceof AnnotationValue.OfLong vl && vl.longValue() == 9999999999L, "ofLong value");
        check(el.value().tag() == AnnotationValue.TAG_LONG, "ofLong tag 'J'");
        AnnotationElement esh = AnnotationElement.ofShort("sh", (short) -300);
        check(esh.value() instanceof AnnotationValue.OfShort vsh && vsh.shortValue() == (short) -300, "ofShort value");
        check(esh.value().tag() == AnnotationValue.TAG_SHORT, "ofShort tag 'S'");
        AnnotationElement eby = AnnotationElement.ofByte("by", (byte) 7);
        check(eby.value() instanceof AnnotationValue.OfByte vby && vby.byteValue() == 7, "ofByte value");
        check(eby.value().tag() == AnnotationValue.TAG_BYTE, "ofByte tag 'B'");
        AnnotationElement ech = AnnotationElement.ofChar("ch", 'Z');
        check(ech.value() instanceof AnnotationValue.OfChar vch && vch.charValue() == 'Z', "ofChar value");
        check(ech.value().tag() == AnnotationValue.TAG_CHAR, "ofChar tag 'C'");
        AnnotationElement ebo = AnnotationElement.ofBoolean("bo", false);
        check(ebo.value() instanceof AnnotationValue.OfBoolean vbo && !vbo.booleanValue(), "ofBoolean value false");
        check(ebo.value().tag() == AnnotationValue.TAG_BOOLEAN, "ofBoolean tag 'Z'");
        // ofDouble / ofFloat（位模式精确）
        AnnotationElement ed = AnnotationElement.ofDouble("d", 0.5);
        check(ed.value() instanceof AnnotationValue.OfDouble vd && vd.doubleValue() == 0.5, "ofDouble value");
        check(ed.value().tag() == AnnotationValue.TAG_DOUBLE, "ofDouble tag 'D'");
        AnnotationElement ef = AnnotationElement.ofFloat("f", 2.5f);
        check(ef.value() instanceof AnnotationValue.OfFloat vf && vf.floatValue() == 2.5f, "ofFloat value");
        check(ef.value().tag() == AnnotationValue.TAG_FLOAT, "ofFloat tag 'F'");
        // ofClass（OfClass: className/classSymbol）
        AnnotationElement ec = AnnotationElement.ofClass("c", ClassDesc.of("java.lang.String"));
        check(ec.value() instanceof AnnotationValue.OfClass, "ofClass instanceof OfClass");
        check(ec.value().tag() == AnnotationValue.TAG_CLASS, "ofClass tag 'c'");
        check(((AnnotationValue.OfClass) ec.value()).className().stringValue().equals("Ljava/lang/String;"), "OfClass className");
        check(((AnnotationValue.OfClass) ec.value()).classSymbol().equals(ClassDesc.of("java.lang.String")), "OfClass classSymbol");
        // ofArray（values() 逐项）
        AnnotationElement ea = AnnotationElement.ofArray("a", AnnotationValue.ofInt(1), AnnotationValue.ofInt(2), AnnotationValue.ofInt(3));
        check(ea.value() instanceof AnnotationValue.OfArray va && va.values().size() == 3, "ofArray size");
        check(ea.value().tag() == AnnotationValue.TAG_ARRAY, "ofArray tag '['");
        check(((AnnotationValue.OfArray) ea.value()).values().get(2) instanceof AnnotationValue.OfInt i3 && i3.intValue() == 3, "ofArray element readback");
        // ofAnnotation（嵌套）
        Annotation nested = Annotation.of(ClassDesc.of("pkg.N"), AnnotationElement.ofInt("x", 9));
        AnnotationElement en = AnnotationElement.ofAnnotation("n", nested);
        check(en.value() instanceof AnnotationValue.OfAnnotation, "ofAnnotation instanceof");
        check(en.value().tag() == AnnotationValue.TAG_ANNOTATION, "ofAnnotation tag '@'");
        check(((AnnotationValue.OfAnnotation) en.value()).annotation().className().stringValue().equals("Lpkg/N;"), "ofAnnotation inner className");
        // of(Utf8Entry, AnnotationValue)（池条目名）
        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        Utf8Entry pooledName = cpb.utf8Entry("pooled");
        AnnotationElement ep = AnnotationElement.of(pooledName, AnnotationValue.ofInt(5));
        check(ep.name() == pooledName, "of(Utf8Entry, v) reuses pool entry");
        check(ep.value() instanceof AnnotationValue.OfInt pi && pi.intValue() == 5, "of(Utf8Entry, v) value");
    }

    // ---- Part 4: null 语义负例（实测 NPE）----
    static void testNulls() {
        try { AnnotationElement.of("n", null); check(false, "of(String,null) must throw"); }
        catch (NullPointerException expected) { check(true, "of(String,null) NPE"); }
        try { AnnotationElement.of((String) null, AnnotationValue.ofInt(1)); check(false, "of((String)null,v) must throw"); }
        catch (NullPointerException expected) { check(true, "of((String)null,v) NPE"); }
        try { AnnotationElement.ofString(null, "v"); check(false, "ofString(null,..) must throw"); }
        catch (NullPointerException expected) { check(true, "ofString(null,..) NPE"); }
        try { AnnotationElement.ofAnnotation("n", null); check(false, "ofAnnotation(null) must throw"); }
        catch (NullPointerException expected) { check(true, "ofAnnotation(null) NPE"); }
        try { AnnotationElement.ofArray("a", (AnnotationValue[]) null); check(false, "ofArray(null varargs) must throw"); }
        catch (NullPointerException expected) { check(true, "ofArray(null varargs) NPE"); }
        try { AnnotationElement.ofClass("c", null); check(false, "ofClass(null) must throw"); }
        catch (NullPointerException expected) { check(true, "ofClass(null) NPE"); }
    }

    // ---- Part 5: 多元素注解顺序保持——build→re-parse 后 name/value/tag 全等 ----
    static void testOrderPreservationRoundTrip() throws Exception {
        ClassFile cf = ClassFile.of();
        // 8 个元素按固定顺序构造（覆盖常量族+引用族+聚合族）
        Annotation anno = Annotation.of(ClassDesc.of("pkg.Ordered"),
                AnnotationElement.ofByte("b1", (byte) 1),
                AnnotationElement.ofChar("c2", 'A'),
                AnnotationElement.ofShort("s3", (short) 3),
                AnnotationElement.ofBoolean("z4", true),
                AnnotationElement.ofInt("i5", 5),
                AnnotationElement.ofLong("j6", 6L),
                AnnotationElement.ofFloat("f7", 7.0f),
                AnnotationElement.ofDouble("d8", 8.0));
        byte[] bytes = cf.build(ClassDesc.of("AEO"), cb -> cb.with(RuntimeVisibleAnnotationsAttribute.of(List.of(anno))));
        Annotation back = cf.parse(bytes).findAttribute(Attributes.runtimeVisibleAnnotations()).get().annotations().get(0);
        check(back.elements().size() == 8, "8 elements round-trip: " + back.elements().size());
        String[] expectNames = {"b1", "c2", "s3", "z4", "i5", "j6", "f7", "d8"};
        int[] expectTags = {AnnotationValue.TAG_BYTE, AnnotationValue.TAG_CHAR, AnnotationValue.TAG_SHORT, AnnotationValue.TAG_BOOLEAN,
                AnnotationValue.TAG_INT, AnnotationValue.TAG_LONG, AnnotationValue.TAG_FLOAT, AnnotationValue.TAG_DOUBLE};
        for (int i = 0; i < 8; i++) {
            check(back.elements().get(i).name().stringValue().equals(expectNames[i]), "order name[" + i + "]: " + back.elements().get(i).name().stringValue());
            check(back.elements().get(i).value().tag() == expectTags[i], "order tag[" + i + "]");
        }
        check(((AnnotationValue.OfByte) back.elements().get(0).value()).byteValue() == 1, "byte survives");
        check(((AnnotationValue.OfChar) back.elements().get(1).value()).charValue() == 'A', "char survives");
        check(((AnnotationValue.OfBoolean) back.elements().get(3).value()).booleanValue(), "boolean survives");
        check(((AnnotationValue.OfDouble) back.elements().get(7).value()).doubleValue() == 8.0, "double survives");
        // name() 返回 Utf8Entry（接口面）
        check(back.elements().get(0).name() instanceof Utf8Entry, "name() is Utf8Entry");
        check(back.elements().get(0).name().equalsString("b1"), "name equalsString");
    }

    // ---- Part 6: 聚合族深嵌套（array 内嵌 annotation/array）回读 ----
    static void testDeepNesting() throws Exception {
        ClassFile cf = ClassFile.of();
        AnnotationValue innerAnno = AnnotationValue.ofAnnotation(Annotation.of(ClassDesc.of("pkg.Deep")));
        AnnotationValue innerArr = AnnotationValue.ofArray(AnnotationValue.ofString("x"), AnnotationValue.ofString("y"));
        AnnotationElement agg = AnnotationElement.ofArray("agg", innerAnno, innerArr);
        Annotation anno = Annotation.of(ClassDesc.of("pkg.Agg"), agg);
        byte[] b = cf.build(ClassDesc.of("AED"), cb -> cb.with(RuntimeVisibleAnnotationsAttribute.of(List.of(anno))));
        Annotation back = cf.parse(b).findAttribute(Attributes.runtimeVisibleAnnotations()).get().annotations().get(0);
        AnnotationValue.OfArray arr = (AnnotationValue.OfArray) back.elements().get(0).value();
        check(arr.values().size() == 2, "aggregating array size");
        check(arr.values().get(0) instanceof AnnotationValue.OfAnnotation oa && oa.annotation().className().stringValue().equals("Lpkg/Deep;"), "array[0] nested annotation");
        check(arr.values().get(1) instanceof AnnotationValue.OfArray ia && ia.values().size() == 2, "array[1] nested array");
        check(((AnnotationValue.OfArray) arr.values().get(1)).values().get(0) instanceof AnnotationValue.OfString s0 && s0.stringValue().equals("x"), "nested array[0] string");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testFactoryMatrix();
        testNulls();
        testOrderPreservationRoundTrip();
        testDeepNesting();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
