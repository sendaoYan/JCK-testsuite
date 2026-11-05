/*
 * @test
 * @summary java.lang.classfile.ClassFileTransform conformance: white-box structure + black-box behavior.
 * @run main ClassFileTransformTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.*;
import java.nio.file.*;

public class ClassFileTransformTest {
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
        Class<?> c = Class.forName("java.lang.classfile.ClassFileTransform");
        check(c != null, "java.lang.classfile.ClassFileTransform loads");
        check(c.getName().equals("java.lang.classfile.ClassFileTransform"), "binary name java.lang.classfile.ClassFileTransform");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.ClassFileTransform is public");
        check(c.isInterface(), "java.lang.classfile.ClassFileTransform is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.ClassFileTransform has no superclass");
        check(declares(c, "accept"), "declares accept");
        check(declares(c, "atEnd"), "declares atEnd");
        check(declares(c, "atStart"), "declares atStart");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ClassFileTransform<ClassTransform, ClassElement, ClassBuilder> t = ClassTransform.ACCEPT_ALL;
        check(t.andThen(ClassTransform.ACCEPT_ALL) != null, "andThen returns non-null");
        check(ClassTransform.ACCEPT_ALL == ClassTransform.ACCEPT_ALL, "ACCEPT_ALL singleton");

    }

    // ---- Part 3: 类型层次——四个具体变换接口都是 ClassFileTransform（javap 核对继承） ----
    static void testHierarchy() throws Exception {
        check(ClassFileTransform.class.isAssignableFrom(ClassTransform.class), "ClassTransform is-a ClassFileTransform");
        check(ClassFileTransform.class.isAssignableFrom(CodeTransform.class), "CodeTransform is-a ClassFileTransform");
        check(ClassFileTransform.class.isAssignableFrom(MethodTransform.class), "MethodTransform is-a ClassFileTransform");
        check(ClassFileTransform.class.isAssignableFrom(FieldTransform.class), "FieldTransform is-a ClassFileTransform");
        // 泛型参数面：ClassFileTransform<ClassTransform, ClassElement, ClassBuilder> 可直接引用 ACCEPT_ALL
        ClassFileTransform<ClassTransform, ClassElement, ClassBuilder> t = ClassTransform.ACCEPT_ALL;
        check(t == ClassTransform.ACCEPT_ALL, "ACCEPT_ALL assignable to raw-typed ClassFileTransform reference");
        // transformClass 只收 ClassTransform：自造"原始 ClassFileTransform"必须以 ClassTransform 形态实现（见 Part 4）
        check(ClassTransform.class.isInterface() && ClassTransform.class.getInterfaces().length >= 1, "ClassTransform extends interfaces");
        boolean extendsCFT = false;
        for (Class<?> i : ClassTransform.class.getInterfaces()) if (i == ClassFileTransform.class) extendsCFT = true;
        check(extendsCFT, "ClassTransform directly extends ClassFileTransform");
        // 接口方法签名白盒（javap 锁定）
        Method accept = ClassFileTransform.class.getMethod("accept", ClassFileBuilder.class, ClassFileElement.class);
        check(accept != null && !accept.isDefault(), "accept(B,E) abstract");
        Method atEnd = ClassFileTransform.class.getMethod("atEnd", ClassFileBuilder.class);
        check(atEnd.isDefault(), "atEnd(B) default");
        Method atStart = ClassFileTransform.class.getMethod("atStart", ClassFileBuilder.class);
        check(atStart.isDefault(), "atStart(B) default");
        Method andThen = ClassFileTransform.class.getMethod("andThen", ClassFileTransform.class);
        check(!andThen.isDefault(), "andThen(C) abstract");
    }

    // ---- Part 4: 自造原始 ClassTransform（裸 ClassFileTransform 面 accept/atEnd/atStart）----
    static void testRawTransformLifecycle() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("CFTR"), cb -> cb
                .withField("f", ConstantDescs.CD_int, 0)
                .withMethodBody("m", ConstantDescs.MTD_void, 0, cob -> cob.return_()));
        ClassModel src = cf.parse(b);
        List<String> log = new ArrayList<>();
        ClassTransform raw = new ClassTransform() {
            public void accept(ClassBuilder cb, ClassElement e) { log.add("accept"); cb.with(e); }
            public void atStart(ClassBuilder cb) { log.add("atStart"); cb.with(SourceFileAttribute.of("Raw.java")); }
            public void atEnd(ClassBuilder cb) { log.add("atEnd"); cb.with(DeprecatedAttribute.of()); }
        };
        ClassModel out = cf.parse(cf.transformClass(src, raw));
        // 次序：atStart → 每元素 accept → atEnd（实测）
        check(log.get(0).equals("atStart"), "lifecycle begins with atStart");
        check(log.get(log.size() - 1).equals("atEnd"), "lifecycle ends with atEnd");
        long accepts = log.stream().filter(s -> s.equals("accept")).count();
        check(accepts == src.elementList().size(), "accept invoked once per source element: " + accepts + " vs " + src.elementList().size());
        check(log.indexOf("atEnd") == log.size() - 1, "atEnd strictly last");
        // atStart/atEnd 注入的元素出现在产物中
        check(out.findAttribute(Attributes.sourceFile()).isPresent(), "atStart-injected SourceFile present");
        check(out.findAttribute(Attributes.deprecated()).isPresent(), "atEnd-injected Deprecated present");
        check(out.fields().size() == 1 && out.methods().size() == 1, "accept passthrough conserves members");
        check(cf.verify(out).isEmpty(), "raw transform output verifies");
    }

    // ---- Part 5: 默认 atEnd/atStart 为 no-op + andThen 生命周期次序（t1 完整生命周期先于 t2）----
    static void testDefaultsAndAndThen() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("CFTD"), cb -> cb.withField("f", ConstantDescs.CD_int, 0));
        ClassModel src = cf.parse(b);
        // 只覆写 accept：默认 atStart/atEnd 不抛不注（no-op 语义）
        List<String> log1 = new ArrayList<>();
        ClassTransform onlyAccept = new ClassTransform() {
            public void accept(ClassBuilder cb, ClassElement e) { log1.add("a"); cb.with(e); }
        };
        ClassModel out1 = cf.parse(cf.transformClass(src, onlyAccept));
        check(out1.fields().size() == 1 && out1.attributes().isEmpty(), "default atStart/atEnd inject nothing");
        check(log1.size() == src.elementList().size(), "only accept per element");
        // andThen 生命周期：t1 的 endHandler 先于 t2 的 endHandler（实测 [eh1, eh2]），两侧效果都落盘
        List<String> order = new ArrayList<>();
        ClassTransform t1 = new ClassTransform() {
            public void accept(ClassBuilder cb, ClassElement e) { cb.with(e); }
            public void atEnd(ClassBuilder cb) { order.add("t1end"); cb.withField("inj1", ConstantDescs.CD_int, 0); }
        };
        ClassTransform t2 = new ClassTransform() {
            public void accept(ClassBuilder cb, ClassElement e) { cb.with(e); }
            public void atEnd(ClassBuilder cb) { order.add("t2end"); cb.withField("inj2", ConstantDescs.CD_int, 0); }
        };
        ClassModel out2 = cf.parse(cf.transformClass(src, t1.andThen(t2)));
        check(order.equals(List.of("t1end", "t2end")), "andThen lifecycle: t1.atEnd before t2.atEnd: " + order);
        List<String> fnames = new ArrayList<>();
        out2.fields().forEach(f -> fnames.add(f.fieldName().stringValue()));
        check(fnames.equals(List.of("f", "inj1", "inj2")), "andThen: both end-effects land in order: " + fnames);
        // andThen 类型：ClassTransform.andThen 返回 ClassTransform（可继续组合）
        ClassTransform chained = t1.andThen(t2).andThen(t1);
        check(chained != null, "andThen chainable");
        // CodeTransform/MethodTransform/FieldTransform 的 andThen 同族语义（存在性）
        check(CodeTransform.ACCEPT_ALL.andThen(CodeTransform.ACCEPT_ALL) != null, "CodeTransform.andThen");
        check(MethodTransform.ACCEPT_ALL.andThen(MethodTransform.ACCEPT_ALL) != null, "MethodTransform.andThen");
        check(FieldTransform.ACCEPT_ALL.andThen(FieldTransform.ACCEPT_ALL) != null, "FieldTransform.andThen");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testHierarchy();
        testRawTransformLifecycle();
        testDefaultsAndAndThen();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
