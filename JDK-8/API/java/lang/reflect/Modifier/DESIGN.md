# java.lang.reflect.Modifier 测试设计案（JDK-8）

> 覆盖目标：完整契约。12 个修饰符常量位值、每个 is* 谓词对自身/无关/0/组合位、toString(int)
> 空/单/组合/全掩码精确格式、六个 *Modifiers() 掩码精确位、以及 JDK-8 特有的 public 无参构造器。

## 1. API 功能

- 12 个 public static final int 常量：PUBLIC..STRICT（0x1..0x800）。
- 12 个 is* 静态谓词 + `toString(int)` + 6 个 `*Modifiers()` 掩码工厂。
- JDK-8：`Modifier` 是普通类，带 public 无参构造器。

## 2. 用户如何使用

- `Modifier.isPublic(m.getModifiers())` 判断成员可见性。
- `Modifier.toString(mods)` 生成可读修饰符串（无固定顺序，仅按固定位序）。

## 3. 参考来源

- Oracle Java SE 8 javadoc（java/lang/reflect/Modifier.html）。
- OpenJDK 8 `~/git/jdk8u-dev/jdk/src/share/classes/java/lang/reflect/Modifier.java`（掩码常量值经源码核对）。
- JCK runtime-8b `tests/api/java_lang/reflect/Modifier/`。

## 4. 测试用例设计（映射到 ModifierTest.java）

| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | 12 常量位值 + 幂次方 + OR=0xfff | testConstants |
| P0 | is* 谓词自身/0/无关位/组合位/未知位 | testPredicates |
| P0 | toString 空/单/组合/全掩码/未知位忽略 | testToString |
| P0 | 6 个 *Modifiers() 精确位（含 interface 无 FINAL/INTERFACE）| testMaskHelpers |
| P1 | public 无参构造器（JDK-8 only）| testConstructor |

## 易错规范细节（已锁入断言）

- `classModifiers()==0xc1f`（PUBLIC|PRIVATE|PROTECTED|STATIC|FINAL|ABSTRACT|STRICT，无 INTERFACE）。
- `interfaceModifiers()==0xc0f`（无 FINAL、无 INTERFACE）——已核对源码。
- `parameterModifiers()==FINAL`；`toString(0x1000|PUBLIC)=="public"`（未知位忽略）。
- 每个 is* 谓词是纯单比特测试，无关位不影响结果。

## 深化批次（2026-11-05）

新增方法（只追加）：testPredicateTruthTable / testToStringExactOrder /
testMaskMembershipPerJavadoc / testRealMemberModifiers。断言 80→179。

### 覆盖增量

- 12 个 is* 谓词对全部 4096 个掩码（0x000..0xfff）的穷举真值表（谓词 ≡ (mask & bit)!=0，
  首个失配掩码进断言消息）；代表组合含全 1 掩码、去位/XOR 近失配、未知高位。
- toString 规范序精确串：PSF="public static final"（输入顺序无关）、0xfff 全串、
  "public protected abstract"、"static transient volatile"、"native strictfp"、
  "strictfp interface"、"private final"、"abstract strictfp"、"transient volatile"、
  "static final volatile"；同掩码重复调用 equals；constructorModifiers/fieldModifiers
  掩码 toString 全等。
- 6 个 *Modifiers() 掩码按 javadoc 成员/排除逐位断言（field 含 volatile/transient、
  排 synchronized/native/abstract/strict；method 排 volatile/transient/interface；
  constructor 仅 3 个访问位 ==0x7；interface 无 FINAL/INTERFACE；parameter==FINAL）。
- 真实成员端到端：String.class="public final"、CharSequence="public abstract interface"、
  int.class="public abstract final"、Integer.valueOf="public static"、
  Object.hashCode="public native"、String.value="private final"；自造样本
  AbsSample(0x409)/IfaceSample(0x609)/MemberSample(0x8) 与源码成员
  natM(0x109)/syncM(0x29)/sfpM(0x809, javac-8 保留 ACC_STRICT)/tF(0x81)/vF(0x41)/
  fF(0x11)/SF(0x19) 位与串全等。

### 版本差异（对照 JDK-27，javap+探针实测）

- javap：27 未新增任何方法；Modifier 类在 27 为 final 且无 public 构造器（旧断言已覆盖）。
- strictfp：javac-8 仍发射 ACC_STRICT（sfpM==0x809，本侧断言）；javac-17+（JEP 306）不再
  发射，27 侧源码 strictfp 方法 ==0x9，按侧断言。其余掩码/toString/谓词行为逐字节一致。
