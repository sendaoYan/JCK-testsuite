/*
 * @test
 * @summary java.lang.classfile.ClassReader conformance: white-box structure + black-box behavior.
 * @run main ClassReaderTest
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

public class ClassReaderTest {
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
        Class<?> c = Class.forName("java.lang.classfile.ClassReader");
        check(c != null, "java.lang.classfile.ClassReader loads");
        check(c.getName().equals("java.lang.classfile.ClassReader"), "binary name java.lang.classfile.ClassReader");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.ClassReader is public");
        check(c.isInterface(), "java.lang.classfile.ClassReader is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.ClassReader has no superclass");
        check(declares(c, "customAttributes"), "declares customAttributes");
        check(declares(c, "flags"), "declares flags");
        check(declares(c, "thisClassEntry"), "declares thisClassEntry");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        AttributeMapper<CA2> mapper = new AttributeMapper<CA2>() {
            public String name() { return "MyAttr"; }
            public CA2 readAttribute(AttributedElement e, ClassReader cr, int p) { cap[0] = cr; return new CA2(this); }
            public void writeAttribute(BufWriter bw, CA2 a) { bw.writeIndex(a.attributeName()); bw.writeInt(3); bw.writeBytes(new byte[]{1,2,3}); }
            public AttributeStability stability() { return AttributeStability.CP_REFS; }
        };
        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.with(new CA2(mapper)));
        ClassFile cf = ClassFile.of(ClassFile.AttributeMapperOption.of(u -> { return u.equalsString("MyAttr") ? mapper : null; }));
        ClassModel cm = cf.parse(b);
        cm.attributes();
        check(cap[0] != null, "ClassReader captured");
        ClassReader cr = cap[0];
        check(cr.classfileLength() > 0, "classfileLength");
        check(cr.thisClassEntry().asInternalName().equals("pkg/T"), "thisClassEntry");
        check(cr.superclassEntry().isPresent(), "superclassEntry present");
        check(cr.flags() == (ClassFile.ACC_PUBLIC | ClassFile.ACC_SUPER), "flags PUBLIC|SUPER");
        check(cr.customAttributes() != null, "customAttributes non-null");
        check(cr.readInt(0) == 0xCAFEBABE, "readInt magic");
        check(cr.readU1(0) == 0xCA, "readU1 magic");
        check(cr.readU2(4) == 0, "readU2 minor");
        check(cr.readU2(8) >= 1, "readU2 constant pool count");
        check(cr.readBytes(0, 4).length == 4, "readBytes magic");

    }


    // ---- Part 3: 自造 mapper 捕获 ClassReader：绝对偏移读 + pos 语义 + 越界矩阵 ----
    static final ClassReader[] cap3 = new ClassReader[1];
    static final int[] posCap = new int[1];
    static final int[] payload = new int[2];
    static class CRA extends CustomAttribute<CRA> { CRA(AttributeMapper<CRA> m) { super(m); } }
    static final AttributeMapper<CRA>[] MAP = new AttributeMapper[1];
    static {
        MAP[0] = new AttributeMapper<CRA>() {
            public String name() { return "ReaderAttr"; }
            public CRA readAttribute(AttributedElement e, ClassReader cr, int pos) {
                cap3[0] = cr; posCap[0] = pos;
                payload[0] = cr.readU1(pos);        // pos = 属性数据起点（非属性头）
                payload[1] = cr.readU1(pos + 1);
                return new CRA(MAP[0]);
            }
            public void writeAttribute(BufWriter bw, CRA a) {
                bw.writeIndex(bw.constantPool().utf8Entry(name()));
                bw.writeInt(2);
                bw.writeU1(0x12);
                bw.writeU1(0x34);
            }
            public AttributeStability stability() { return AttributeStability.STATELESS; }
        };
    }

    static void testCapturedReader() throws Exception {
        ClassFile cfPlain = ClassFile.of();
        ClassFile cf = ClassFile.of(ClassFile.AttributeMapperOption.of(u -> u.equalsString("ReaderAttr") ? MAP[0] : null));
        byte[] b = cfPlain.build(ClassDesc.of("pkg.Rd"), cb -> cb.with(new CRA(MAP[0])));
        check(b.length > 8, "custom attr bytes nonempty");
        ClassModel cm = cf.parse(b);
        check(cm.attributes().size() == 1, "custom attribute read via mapper");
        check(cap3[0] != null, "ClassReader captured in readAttribute");
        ClassReader cr = cap3[0];
        // classfileLength == 字节数全长
        check(cr.classfileLength() == b.length, "classfileLength == bytes.length (" + cr.classfileLength() + "/" + b.length + ")");
        // pos 语义：readAttribute 的 pos 是属性数据起点（实测）
        check(payload[0] == 0x12 && payload[1] == 0x34, "pos points at payload (0x12,0x34)");
        // 类头访问器
        check(cr.thisClassEntry().asInternalName().equals("pkg/Rd"), "thisClassEntry");
        check(cr.thisClassEntry().asSymbol().equals(ClassDesc.of("pkg.Rd")), "thisClassEntry asSymbol");
        check(cr.superclassEntry().isPresent(), "superclassEntry present");
        check(cr.superclassEntry().get().asInternalName().equals("java/lang/Object"), "superclassEntry Object");
        check(cr.flags() == (ClassFile.ACC_PUBLIC | ClassFile.ACC_SUPER), "flags PUBLIC|SUPER");
        // 常量池接口（ClassReader extends ConstantPool）
        check(cr.size() > 0, "ConstantPool.size() > 0");
        check(cr.entryByIndex(cr.thisClassEntry().index()) == cr.thisClassEntry(), "entryByIndex(thisClass index) identity");
        // 绝对偏移读：classfile 头
        check(cr.readInt(0) == 0xCAFEBABE, "readInt(0) magic");
        check(cr.readU1(0) == 0xCA, "readU1(0) 0xCA");
        check(cr.readU1(1) == 0xFE, "readU1(1) 0xFE");
        check(cr.readU2(4) == 0, "readU2(4) minor 0");
        check(cr.readU2(6) == 71, "readU2(6) major 71");
        check(cr.readU2(8) >= 1, "readU2(8) cp count >= 1");
        check(cr.readS1(0) == -54, "readS1(0) -54 (0xCA signed)");
        check(cr.readS2(0) == -13570, "readS2(0) 0xCAFE signed -13570");
        check(cr.readLong(0) == 0xCAFEBABE00000047L, "readLong(0) magic+minor+major");
        check(cr.readFloat(0) == Float.intBitsToFloat(0xCAFEBABE), "readFloat(0) bits");
        byte[] rb = cr.readBytes(0, 4);
        check(rb.length == 4 && (rb[0] & 0xff) == 0xCA && (rb[3] & 0xff) == 0xBE, "readBytes(0,4) CAFEBABE");
        byte[] rb2 = cr.readBytes(4, 4);
        check(rb2.length == 4 && (rb2[0] & 0xff) == 0 && (rb2[3] & 0xff) == 71, "readBytes(4,4) minor+major");
        // customAttributes：注册表函数（对已知名返回 mapper）
        check(cr.customAttributes() != null, "customAttributes non-null");
        check(cr.customAttributes().apply(ConstantPoolBuilder.of().utf8Entry("ReaderAttr")) == MAP[0], "customAttributes resolves ReaderAttr -> mapper");
        check(cr.customAttributes().apply(ConstantPoolBuilder.of().utf8Entry("NoSuchAttr")) == null, "customAttributes unknown -> null");
        // 越界负例（实测）
        try { cr.readInt(-1); check(false, "readInt(-1) must throw"); }
        catch (IllegalArgumentException e) { check(e.getMessage().contains("beyond"), "readInt(-1) IAE beyond bounds"); }
        try { cr.readInt(cr.classfileLength()); check(false, "readInt(len) must throw"); }
        catch (IllegalArgumentException e) { check(e.getMessage().contains("beyond"), "readInt(len) IAE beyond bounds"); }
        try { cr.readU1(cr.classfileLength()); check(false, "readU1(len) must throw"); }
        catch (IllegalArgumentException e) { check(e.getMessage().contains("beyond"), "readU1(len) IAE"); }
        try { cr.readU2(cr.classfileLength() - 1); check(false, "readU2(len-1) must throw"); }
        catch (IllegalArgumentException e) { check(e.getMessage() != null, "readU2(len-1) IAE"); }
        // readEntry(pos)：在字节偏移 pos 读 u2 作为池下标再解析（实测语义）
        try { cr.readEntry(0); check(false, "readEntry(0) must throw"); }
        catch (java.lang.classfile.constantpool.ConstantPoolException e) { check(e.getMessage().contains("Bad CP index"), "readEntry(0) ConstantPoolException: " + e.getMessage()); }
        try { cr.readEntry(cr.classfileLength()); check(false, "readEntry(len) must throw"); }
        catch (IllegalArgumentException e) { check(e.getMessage() != null, "readEntry(len) IAE"); }
        // readEntryOrNull：u2 值为 0（minor 版本偏移 4 处）→ null 宽松通道；坏下标仍抛
        check(cr.readEntryOrNull(4) == null, "readEntryOrNull(minor=0 offset) null");
        try { cr.readEntryOrNull(0); check(false, "readEntryOrNull(0) must throw"); }
        catch (java.lang.classfile.constantpool.ConstantPoolException e) { check(e.getMessage().contains("Bad CP index"), "readEntryOrNull(0) ConstantPoolException"); }
    }

    // ---- Part 4: writeAttribute 内 copyBytesTo（BufWriter 只在该回调可得） ----
    static final boolean[] copied = new boolean[1];
    static class CRB extends CustomAttribute<CRB> { CRB(AttributeMapper<CRB> m) { super(m); } }
    static void testCopyBytesTo() throws Exception {
        AttributeMapper<CRB> m = new AttributeMapper<CRB>() {
            public String name() { return "CopyAttr"; }
            public CRB readAttribute(AttributedElement e, ClassReader cr, int pos) { return new CRB(this); }
            public void writeAttribute(BufWriter bw, CRB a) {
                bw.writeIndex(bw.constantPool().utf8Entry(name()));
                bw.writeInt(4);
                cap3[0].copyBytesTo(bw, 0, 4);   // 拷贝 classfile 头 4 字节（magic）
                copied[0] = true;
            }
            public AttributeStability stability() { return AttributeStability.STATELESS; }
        };
        ClassFile cf = ClassFile.of(ClassFile.AttributeMapperOption.of(u -> u.equalsString("CopyAttr") ? m : null));
        byte[] b = cf.build(ClassDesc.of("pkg.Cp"), cb -> cb.with(new CRB(m)));
        check(copied[0], "copyBytesTo invoked inside writeAttribute");
        check(b.length > 8, "copy build bytes nonempty");
        ClassModel cm = cf.parse(b);
        check(cm.attributes().size() == 1, "copy attr reparsed");
    }

    // ---- Part 5: 未注册 mapper 的 parse 不抛（UnknownAttribute 保留） ----
    static void testUnregisteredParse() throws Exception {
        ClassFile cfPlain = ClassFile.of();
        ClassFile cfReg = ClassFile.of(ClassFile.AttributeMapperOption.of(u -> u.equalsString("ReaderAttr") ? MAP[0] : null));
        byte[] b = cfPlain.build(ClassDesc.of("pkg.Un"), cb -> cb.with(new CRA(MAP[0])));
        ClassModel cm = ClassFile.of().parse(b);   // 未注册 mapper：不抛
        check(cm.attributes().size() == 1, "unregistered attr kept as unknown");
        check(cm.thisClass().asInternalName().equals("pkg/Un"), "unregistered parse thisClass");
        check(ClassFile.of().verify(cm).isEmpty(), "unregistered verify clean");
    }
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testCapturedReader();
        testCopyBytesTo();
        testUnregisteredParse();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
