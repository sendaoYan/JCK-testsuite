/*
 * CovTools - static JDK API coverage instrumentation for the JCK-testsuite.
 *
 * Dependencies : ASM 9.11.1 (asm, asm-tree, asm-commons, asm-analysis, asm-util).
 *                The 9.11.1 jars are the ones in /tmp/jcov-tool/asm-new/*.jar.
 *                If you do not have them, download the 9.11.1 release from
 *                https://asm.ow2.io/ (core + tree + commons + analysis + util).
 *                Do NOT check the jars into the repo unless .gitignore covers them.
 *
 * Build / run   : MUST run on JDK-27. The JDK-27 test classes are class file
 *                major version 71, so an older JVM dies with
 *                UnsupportedClassVersionError.
 *
 *                JDK=/data/yansendao/JDK/binary/temurin/jdk-27.0.1.0.2+1
 *                CP="asm-new/asm-9.11.1.jar:asm-new/asm-tree-9.11.1.jar:asm-new/asm-commons-9.11.1.jar:asm-new/asm-analysis-9.11.1.jar:asm-new/asm-util-9.11.1.jar:."
 *                "$JDK/bin/javac" -cp "$CP" -d /tmp/jcov-tool tools/CovTools.java
 *                "$JDK/bin/java"  -cp "$CP:/tmp/jcov-tool" CovTools ...
 *
 *   methods mode: dump every non-private method of every jrt class to a TSV,
 *                plus a class-hierarchy side file and a preview-class list:
 *                  CovTools methods /tmp/jcov-tool/jdk27.methods.txt
 *                    -> /tmp/jcov-tool/jdk27.methods.txt
 *                    -> /tmp/jcov-tool/jdk27.methods.txt.hierarchy  (fqcn<TAB>super<TAB>itf1,itf2<TAB>isInterface)
 *                    -> /tmp/jcov-tool/jdk27.methods.txt.preview    (fqcn of preview API classes)
 *                Preview classes are detected via the
 *                Ljdk/internal/javac/PreviewFeature; class annotation (the JDK
 *                marks preview APIs with a CLASS-retention annotation, NOT with
 *                the old minor-version 0xFFFF trick).
 *
 *   calls mode  : dump invoke sites + string literals + reflective-constructor
 *                targets of compiled test classes:
 *                  CovTools calls /tmp/jcov-tool/calls.txt /tmp/testclasses
 *                Test classes must be compiled ONE FILE PER fqcn into
 *                /tmp/testclasses/<fqcn>/ (a directory named by the dotted fqcn)
 *                so that default-package classes from different fqcn dirs do not
 *                collide. Preview test classes additionally need
 *                --enable-preview --source 27 at compile time, e.g.:
 *                  "$JDK/bin/javac" -nowarn --enable-preview --source 27 \
 *                    --add-modules ALL-SYSTEM @/tmp/jcov-tool/add-exports.txt \
 *                    -d "/tmp/testclasses/$fqcn" "JDK-27/API/${fqcn//./\/}"/*.java
 *
 *                Output records (one per line, TAB separated):
 *                  src<TAB>INVOKE<TAB>owner<TAB>name<TAB>desc      every invoke*
 *                  src<TAB>INVOKEIF<TAB>owner<TAB>name<TAB>desc    invokeinterface / itf=true
 *                  src<TAB>LDC<TAB>string                          string literal (<=300 chars)
 *                  src<TAB>CTOR<TAB>owner<TAB>desc                 reflective ctor: owner.<init>(desc)
 *                src is the first path component under the scan root, i.e. the
 *                dotted fqcn of the test class.
 *
 * The reflective-constructor recovery runs ASM's Analyzer with a small custom
 * interpreter that tags stack values as Class / Constructor / String and tracks
 * them through locals, DUP and (conservatively) control-flow merges, so that
 * `Class.forName("X").getConstructor(..).newInstance(..)` and
 * `Constructor c = X.class.getConstructor(..); ... c.newInstance(..)` are both
 * recognised as a call to X.<init>(..).
 *
 * This is the static call-site fallback for jcov: the jcov dynamic agent is not
 * usable on JDK-27 (its ASM is too old), so coverage is approximated from
 * bytecode invoke sites + string literals + reflective-constructor recovery.
 */
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.net.URI;
import java.io.*;
import java.util.*;

public class CovTools {
  static final int ACC_PRIVATE = 0x0002;
  static final int ACC_PUBLIC = 0x0001;
  static final int ACC_PROTECTED = 0x0004;

  public static void main(String[] args) throws Exception {
    String mode = args[0];
    if (mode.equals("methods")) dumpMethods(args[1]);
    else if (mode.equals("calls")) dumpCalls(args[1], args[2]);
    else { System.err.println("unknown mode"); System.exit(2); }
  }

  // ---------------------------------------------------------------------------
  // methods mode
  // ---------------------------------------------------------------------------
  static void dumpMethods(String outFile) throws Exception {
    FileSystem fs = FileSystems.getFileSystem(URI.create("jrt:/"));
    BufferedWriter w = new BufferedWriter(new FileWriter(outFile), 1 << 20);
    BufferedWriter hw = new BufferedWriter(new FileWriter(outFile + ".hierarchy"), 1 << 20);
    BufferedWriter pw = new BufferedWriter(new FileWriter(outFile + ".preview"), 1 << 20);
    try (var s = Files.walk(fs.getPath("/modules"))) {
      for (Path p : (Iterable<Path>) s::iterator) {
        String pn = p.toString();
        if (!pn.endsWith(".class")) continue;
        if (pn.endsWith("module-info.class") || pn.endsWith("package-info.class")) continue;
        byte[] b = Files.readAllBytes(p);
        try {
          ClassReader cr = new ClassReader(b);
          final String[] cn = new String[1];
          final String[] sup = new String[1];
          final String[][] itfs = new String[1][];
          final boolean[] preview = new boolean[1];
          final boolean[] isItf = new boolean[1];
          cr.accept(new ClassVisitor(Opcodes.ASM9) {
            public void visit(int v, int a, String name, String sig, String su, String[] itf) {
              cn[0] = name; sup[0] = su; itfs[0] = itf; isItf[0] = (a & Opcodes.ACC_INTERFACE) != 0;
            }
            public AnnotationVisitor visitAnnotation(String d, boolean vis) {
              if (d.equals("Ljdk/internal/javac/PreviewFeature;")) preview[0] = true;
              return null;
            }
            public MethodVisitor visitMethod(int acc, String name, String desc, String sig, String[] ex) {
              if ((acc & ACC_PRIVATE) != 0) return null;
              if (!((acc & ACC_PUBLIC) != 0 || (acc & ACC_PROTECTED) != 0)) return null;
              if (name.equals("<clinit>")) return null;
              try { w.write(cn[0].replace('/', '.') + "\t" + name + "\t" + desc + "\t" + acc + "\n"); }
              catch (IOException e) { throw new RuntimeException(e); }
              return null;
            }
          }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES);
          String dotted = cn[0].replace('/', '.');
          String ssup = (sup[0] == null) ? "" : sup[0].replace('/', '.');
          StringBuilder sb = new StringBuilder();
          if (itfs[0] != null) for (String it : itfs[0]) { if (sb.length() > 0) sb.append(','); sb.append(it.replace('/', '.')); }
          hw.write(dotted + "\t" + ssup + "\t" + sb + "\t" + (isItf[0] ? "1" : "0") + "\n");
          if (preview[0]) pw.write(dotted + "\n");
        } catch (Exception e) { }
      }
    }
    w.close(); hw.close(); pw.close();
  }

  // ---------------------------------------------------------------------------
  // calls mode
  // ---------------------------------------------------------------------------
  static void dumpCalls(String outFile, String rootDir) throws Exception {
    BufferedWriter w = new BufferedWriter(new FileWriter(outFile), 1 << 20);
    List<Path> files = new ArrayList<>();
    Files.walkFileTree(Paths.get(rootDir), new SimpleFileVisitor<Path>() {
      public FileVisitResult visitFile(Path f, BasicFileAttributes a) {
        if (f.toString().endsWith(".class")) files.add(f);
        return FileVisitResult.CONTINUE;
      }
    });
    Path root = Paths.get(rootDir);
    for (Path f : files) {
      byte[] b = Files.readAllBytes(f);
      String src;
      Path rel = root.relativize(f);
      src = (rel.getNameCount() > 1) ? rel.getName(0).toString() : "(root)";
      try {
        ClassReader cr = new ClassReader(b);
        ClassNode cn = new ClassNode();
        cr.accept(cn, 0);
        // Class objects are frequently stashed in static fields
        // (e.g. `static Class<?> PEM_CLASS = Class.forName("java.security.PEM");`).
        // Resolve those first so a later `PEM_CLASS.getConstructor(...)` can be
        // attributed to the right owner.
        Map<String,String> staticFields = new HashMap<>();
        for (MethodNode mn : cn.methods) collectStaticFields(staticFields, mn);
        for (MethodNode mn : cn.methods) {
          emitRawRecords(w, src, mn);
          emitCtorRecords(w, src, cn.name, mn, staticFields);
        }
      } catch (Exception e) { }
    }
    w.close();
  }

  static void emitRawRecords(BufferedWriter w, String src, MethodNode mn) throws IOException {
    for (AbstractInsnNode insn = mn.instructions.getFirst(); insn != null; insn = insn.getNext()) {
      if (insn instanceof MethodInsnNode) {
        MethodInsnNode mi = (MethodInsnNode) insn;
        w.write(src + "\tINVOKE\t" + mi.owner + "\t" + mi.name + "\t" + mi.desc + "\n");
        if (mi.itf || mi.getOpcode() == Opcodes.INVOKEINTERFACE) {
          w.write(src + "\tINVOKEIF\t" + mi.owner + "\t" + mi.name + "\t" + mi.desc + "\n");
        }
      } else if (insn instanceof LdcInsnNode) {
        Object cst = ((LdcInsnNode) insn).cst;
        if (cst instanceof String) {
          String s = (String) cst;
          if (s.length() <= 300) w.write(src + "\tLDC\t" + s.replace("\t", " ").replace("\n", " ") + "\n");
        }
      }
    }
  }

  static void emitCtorRecords(BufferedWriter w, String src, String className, MethodNode mn, Map<String,String> staticFields) throws IOException {
    if (mn.instructions.size() == 0) return;
    Frame<BasicValue>[] frames;
    try {
      Analyzer<BasicValue> a = new Analyzer<>(new CovInterpreter(mn, staticFields));
      frames = a.analyze(className, mn);
    } catch (AnalyzerException e) {
      return;
    }
    InsnList insns = mn.instructions;
    int idx = 0;
    for (AbstractInsnNode insn = insns.getFirst(); insn != null; insn = insn.getNext(), idx++) {
      if (idx >= frames.length) break;
      if (insn instanceof MethodInsnNode) {
        MethodInsnNode mi = (MethodInsnNode) insn;
        if (mi.owner.equals("java/lang/reflect/Constructor") && mi.name.equals("newInstance")) {
          Frame<BasicValue> fr = frames[idx];
          int nArgs = Type.getArgumentTypes(mi.desc).length;
          int recvPos = fr.getStackSize() - nArgs - 1;
          if (recvPos >= 0) {
            BasicValue recv = fr.getStack(recvPos);
            if (recv instanceof CovValue) {
              CovValue r = (CovValue) recv;
              if (r.tag == CovValue.CTOR && r.clsName != null && r.ctorDesc != null) {
                w.write(src + "\tCTOR\t" + r.clsName.replace('/', '.') + "\t" + r.ctorDesc + "\n");
              }
            }
          }
        }
      }
    }
  }

  // Light pre-pass over one method: record static-field Class assignments
  // (Class.forName / X.class / Integer.TYPE stored via PUTSTATIC). Only CLASS/STR
  // values are tracked; everything else is UNKNOWN, which is enough to resolve
  // the reflective-construction receivers used in other methods.
  static void collectStaticFields(Map<String,String> staticFields, MethodNode mn) {
    ArrayDeque<CovValue> stack = new ArrayDeque<>();
    for (AbstractInsnNode insn = mn.instructions.getFirst(); insn != null; insn = insn.getNext()) {
      int op = insn.getOpcode();
      if (insn instanceof LdcInsnNode) {
        Object cst = ((LdcInsnNode) insn).cst;
        if (cst instanceof Type) stack.push(CovValue.cls(((Type) cst).getInternalName()));
        else if (cst instanceof String) stack.push(CovValue.str((String) cst));
        else stack.push(CovValue.unknown(Type.getType(Object.class)));
      } else if (insn instanceof FieldInsnNode) {
        FieldInsnNode fi = (FieldInsnNode) insn;
        if (fi.getOpcode() == Opcodes.GETSTATIC && isPrimitiveTypeField(fi.owner, fi.name)) {
          stack.push(CovValue.cls(primitiveDescriptor(fi.owner)));
        } else if (fi.getOpcode() == Opcodes.PUTSTATIC) {
          CovValue v = pop1(stack);
          if (v.tag == CovValue.CLASS && v.clsName != null) staticFields.put(fi.owner + "." + fi.name, v.clsName);
        } else if (fi.getOpcode() == Opcodes.GETFIELD) {
          pop1(stack); stack.push(CovValue.unknown(Type.getType(Object.class)));
        } else if (fi.getOpcode() == Opcodes.GETSTATIC) {
          String c = staticFields.get(fi.owner + "." + fi.name);
          stack.push(c != null ? CovValue.cls(c) : CovValue.unknown(Type.getType(Object.class)));
        } else if (fi.getOpcode() == Opcodes.PUTFIELD) {
          pop2(stack);
        }
      } else if (insn instanceof MethodInsnNode) {
        MethodInsnNode mi = (MethodInsnNode) insn;
        boolean isStatic = mi.getOpcode() == Opcodes.INVOKESTATIC;
        Type[] args = Type.getArgumentTypes(mi.desc);
        int nPop = args.length + (isStatic ? 0 : 1);
        CovValue[] popped = new CovValue[nPop];
        for (int i = 0; i < nPop; i++) popped[i] = pop1(stack);
        if (mi.owner.equals("java/lang/Class") && (mi.name.equals("forName") || mi.name.equals("forName0")) && nPop >= 1) {
          CovValue s = popped[nPop - 1];
          if (s.tag == CovValue.STR && s.clsName != null && s.clsName.indexOf('.') > 0) stack.push(CovValue.cls(s.clsName.replace('.', '/')));
          else stack.push(CovValue.unknown(Type.getType(Object.class)));
        } else if (Type.getReturnType(mi.desc).getSort() != Type.VOID) {
          stack.push(CovValue.unknown(Type.getReturnType(mi.desc)));
        }
      } else if (insn instanceof TypeInsnNode) {
        TypeInsnNode ti = (TypeInsnNode) insn;
        if (ti.getOpcode() == Opcodes.NEW) stack.push(CovValue.unknown(Type.getType(Object.class)));
        else if (ti.getOpcode() == Opcodes.ANEWARRAY) { pop1(stack); stack.push(CovValue.unknown(Type.getType(Object.class))); }
        else if (ti.getOpcode() == Opcodes.INSTANCEOF) { pop1(stack); stack.push(CovValue.unknown(Type.getType(Object.class))); }
      } else if (insn instanceof IntInsnNode) {
        if (op == Opcodes.NEWARRAY) { pop1(stack); stack.push(CovValue.unknown(Type.getType(Object.class))); }
        else stack.push(CovValue.unknown(Type.INT_TYPE));
      } else if (insn instanceof VarInsnNode) {
        VarInsnNode vi = (VarInsnNode) insn;
        if (vi.getOpcode() == Opcodes.ASTORE) pop1(stack);
        else if (vi.getOpcode() == Opcodes.ALOAD) stack.push(CovValue.unknown(Type.getType(Object.class)));
        else if (vi.getOpcode() == Opcodes.ISTORE || vi.getOpcode() == Opcodes.LSTORE || vi.getOpcode() == Opcodes.FSTORE || vi.getOpcode() == Opcodes.DSTORE) pop1(stack);
        else if (vi.getOpcode() == Opcodes.ILOAD || vi.getOpcode() == Opcodes.LLOAD || vi.getOpcode() == Opcodes.FLOAD || vi.getOpcode() == Opcodes.DLOAD) stack.push(CovValue.unknown(Type.INT_TYPE));
      } else if (insn instanceof JumpInsnNode) {
        if (op != Opcodes.GOTO) pop1(stack);
        stack.clear();
      } else if (insn instanceof TableSwitchInsnNode || insn instanceof LookupSwitchInsnNode) {
        pop1(stack); stack.clear();
      } else if (insn instanceof InsnNode) {
        if (op == Opcodes.DUP) { CovValue v = stack.peek(); stack.push(v != null ? v : CovValue.unknown(Type.getType(Object.class))); }
        else if (op == Opcodes.POP) pop1(stack);
        else if (op == Opcodes.POP2) pop1(stack);
        else if (op == Opcodes.AALOAD) { pop2(stack); stack.push(CovValue.unknown(Type.getType(Object.class))); }
        else if (op == Opcodes.AASTORE) pop3(stack);
        else if (op == Opcodes.ACONST_NULL || (op >= Opcodes.ICONST_M1 && op <= Opcodes.DCONST_1)) stack.push(CovValue.unknown(Type.INT_TYPE));
        else if (op == Opcodes.RETURN || op == Opcodes.ARETURN || op == Opcodes.IRETURN || op == Opcodes.LRETURN || op == Opcodes.FRETURN || op == Opcodes.DRETURN || op == Opcodes.ATHROW) stack.clear();
        else if (op == Opcodes.MONITORENTER || op == Opcodes.MONITOREXIT || op == Opcodes.ARRAYLENGTH) { pop1(stack); stack.push(CovValue.unknown(Type.INT_TYPE)); }
        else if (op >= Opcodes.IALOAD && op <= Opcodes.SALOAD) { pop2(stack); stack.push(CovValue.unknown(Type.INT_TYPE)); }
        else if (op >= Opcodes.IASTORE && op <= Opcodes.SASTORE) pop3(stack);
        else if ((op >= Opcodes.IADD && op <= Opcodes.LXOR) || (op >= Opcodes.LCMP && op <= Opcodes.DCMPG)) { pop2(stack); stack.push(CovValue.unknown(Type.INT_TYPE)); }
        else if ((op >= Opcodes.INEG && op <= Opcodes.LNEG) || (op >= Opcodes.I2L && op <= Opcodes.I2S)) { pop1(stack); stack.push(CovValue.unknown(Type.INT_TYPE)); }
        else stack.clear();
      }
    }
  }

  static CovValue pop1(ArrayDeque<CovValue> s) { return s.isEmpty() ? CovValue.unknown(Type.getType(Object.class)) : s.pop(); }
  static void pop2(ArrayDeque<CovValue> s) { if (!s.isEmpty()) s.pop(); if (!s.isEmpty()) s.pop(); }
  static void pop3(ArrayDeque<CovValue> s) { pop2(s); if (!s.isEmpty()) s.pop(); }

  // ---- value abstraction for the reflective-constructor analysis ----
  static final class CovValue extends BasicValue {
    static final int UNKNOWN = 0, CLASS = 1, CTOR = 2, STR = 3;
    final int tag;
    final String clsName;   // CLASS: internal type name; CTOR: internal owner
    final String ctorDesc;  // CTOR: <init> descriptor, e.g. "()V"
    CovValue(Type t, int tag, String clsName, String ctorDesc) {
      super(t); this.tag = tag; this.clsName = clsName; this.ctorDesc = ctorDesc;
    }
    static CovValue unknown(Type t) { return new CovValue(t, UNKNOWN, null, null); }
    static CovValue cls(String internal) { return new CovValue(Type.getObjectType("java/lang/Class"), CLASS, internal, null); }
    static CovValue ctor(String internal, String desc) { return new CovValue(Type.getObjectType("java/lang/reflect/Constructor"), CTOR, internal, desc); }
    static CovValue str(String s) { return new CovValue(Type.getObjectType("java/lang/String"), STR, s, null); }
  }

  static final class CovInterpreter extends BasicInterpreter {
    final MethodNode mn;
    final Map<String,String> staticFields;
    CovInterpreter(MethodNode mn, Map<String,String> staticFields) { super(Opcodes.ASM9); this.mn = mn; this.staticFields = staticFields; }

    @Override public CovValue newValue(Type type) {
      if (type == null || type.getSort() == Type.VOID) return null;
      return CovValue.unknown(type);
    }

    @Override public CovValue newOperation(AbstractInsnNode insn) throws AnalyzerException {
      if (insn instanceof LdcInsnNode) {
        Object c = ((LdcInsnNode) insn).cst;
        if (c instanceof Type) return CovValue.cls(((Type) c).getInternalName());
        if (c instanceof String) return CovValue.str((String) c);
      } else if (insn instanceof FieldInsnNode) {
        FieldInsnNode fi = (FieldInsnNode) insn;
        if (fi.getOpcode() == Opcodes.GETSTATIC && isPrimitiveTypeField(fi.owner, fi.name)) {
          return CovValue.cls(primitiveDescriptor(fi.owner));
        } else if (fi.getOpcode() == Opcodes.GETSTATIC) {
          String c = staticFields.get(fi.owner + "." + fi.name);
          if (c != null) return CovValue.cls(c);
        }
      }
      return wrap(super.newOperation(insn));
    }

    @Override public CovValue copyOperation(AbstractInsnNode insn, BasicValue value) {
      if (value instanceof CovValue) return (CovValue) value; // DUP/ALOAD carry the tag
      return wrap(value);
    }

    @Override public CovValue unaryOperation(AbstractInsnNode insn, BasicValue value) throws AnalyzerException {
      return wrap(super.unaryOperation(insn, value));
    }

    @Override public CovValue binaryOperation(AbstractInsnNode insn, BasicValue v1, BasicValue v2) throws AnalyzerException {
      return wrap(super.binaryOperation(insn, v1, v2));
    }

    @Override public CovValue ternaryOperation(AbstractInsnNode insn, BasicValue v1, BasicValue v2, BasicValue v3) throws AnalyzerException {
      return wrap(super.ternaryOperation(insn, v1, v2, v3));
    }

    @Override public CovValue naryOperation(AbstractInsnNode insn, List<? extends BasicValue> values) throws AnalyzerException {
      if (insn instanceof MethodInsnNode) {
        MethodInsnNode mi = (MethodInsnNode) insn;
        if (mi.owner.equals("java/lang/Class") && (mi.name.equals("forName") || mi.name.equals("forName0")) && !values.isEmpty()) {
          BasicValue s = values.get(0);
          if (s instanceof CovValue && ((CovValue) s).tag == CovValue.STR && ((CovValue) s).clsName != null
              && ((CovValue) s).clsName.indexOf('.') > 0) {
            return CovValue.cls(((CovValue) s).clsName.replace('.', '/'));
          }
        } else if (mi.owner.equals("java/lang/Class") && (mi.name.equals("getDeclaredConstructor") || mi.name.equals("getConstructor")) && !values.isEmpty()) {
          BasicValue recv = values.get(0);
          if (recv instanceof CovValue && ((CovValue) recv).tag == CovValue.CLASS && ((CovValue) recv).clsName != null) {
            String desc = reconstructCtorDesc(insn);
            if (desc != null) return CovValue.ctor(((CovValue) recv).clsName, desc);
          }
        }
      }
      return wrap(super.naryOperation(insn, values));
    }

    @Override public CovValue merge(BasicValue a, BasicValue b) {
      if (a instanceof CovValue && b instanceof CovValue) {
        CovValue x = (CovValue) a, y = (CovValue) b;
        if (x.tag == y.tag && x.tag != CovValue.UNKNOWN
            && Objects.equals(x.clsName, y.clsName) && Objects.equals(x.ctorDesc, y.ctorDesc)) return x;
      }
      return wrap(super.merge(a, b));
    }

    static CovValue wrap(BasicValue v) {
      if (v == null) return null;
      if (v instanceof CovValue) return (CovValue) v;
      return CovValue.unknown(v.getType());
    }
  }

  // Reconstruct the <init> descriptor from the varargs Class[] built immediately
  // before a getDeclaredConstructor/getConstructor call site. Returns null when
  // the array is not built inline from class literals (can't be sure), in which
  // case the caller does NOT emit a CTOR record (conservative: avoid overcount).
  static String reconstructCtorDesc(AbstractInsnNode call) {
    List<String> argTypes = new ArrayList<>(); // collected in reverse order
    AbstractInsnNode p = call.getPrevious();
    while (p != null) {
      if (p instanceof LdcInsnNode) {
        Object c = ((LdcInsnNode) p).cst;
        if (c instanceof Type) argTypes.add(((Type) c).getDescriptor());
        else break;
      } else if (p instanceof FieldInsnNode) {
        FieldInsnNode fi = (FieldInsnNode) p;
        if (fi.getOpcode() == Opcodes.GETSTATIC && isPrimitiveTypeField(fi.owner, fi.name)) argTypes.add(primitiveDescriptor(fi.owner));
        else break;
      } else if (p instanceof TypeInsnNode) {
        TypeInsnNode ti = (TypeInsnNode) p;
        if (ti.getOpcode() == Opcodes.ANEWARRAY && ti.desc.equals("java/lang/Class")) break; // reached array creation
        else break;
      } else if (p instanceof InsnNode) {
        int o = p.getOpcode();
        if (o == Opcodes.DUP || o == Opcodes.AASTORE || o == Opcodes.ACONST_NULL ||
            o == Opcodes.ICONST_0 || o == Opcodes.ICONST_1 || o == Opcodes.ICONST_2 ||
            o == Opcodes.ICONST_3 || o == Opcodes.ICONST_4 || o == Opcodes.ICONST_5) {
          // varargs array scaffolding: skip
        } else break;
      } else if (p instanceof IntInsnNode) {
        // BIPUSH/SIPUSH array index — skip
      } else {
        break; // label / frame / jump / anything else -> bail
      }
      p = p.getPrevious();
    }
    Collections.reverse(argTypes);
    StringBuilder sb = new StringBuilder("(");
    for (String t : argTypes) sb.append(t);
    sb.append(")V");
    return sb.toString();
  }

  static boolean isPrimitiveTypeField(String owner, String name) {
    if (!name.equals("TYPE")) return false;
    switch (owner) {
      case "java/lang/Integer": case "java/lang/Long": case "java/lang/Boolean":
      case "java/lang/Character": case "java/lang/Byte": case "java/lang/Short":
      case "java/lang/Float": case "java/lang/Double": case "java/lang/Void":
        return true;
      default: return false;
    }
  }

  static String primitiveDescriptor(String owner) {
    switch (owner) {
      case "java/lang/Integer": return "I";
      case "java/lang/Long": return "J";
      case "java/lang/Boolean": return "Z";
      case "java/lang/Character": return "C";
      case "java/lang/Byte": return "B";
      case "java/lang/Short": return "S";
      case "java/lang/Float": return "F";
      case "java/lang/Double": return "D";
      case "java/lang/Void": return "V";
      default: return "Ljava/lang/Object;";
    }
  }
}
