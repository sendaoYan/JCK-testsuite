# java.lang.reflect.Modifier 测试设计案（JDK-27）

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

## JDK-27 差异（已锁入断言）

- JDK-27 `Modifier` 为 `final` 类且无 public 构造器（JDK-8 有 public 无参构造器）；由 `testFinalClass` 覆盖。

## 深化批次（2026-11-05）

新增方法（只追加）：testPredicateTruthTable / testToStringExactOrder /
testMaskMembershipPerJavadoc / testRealMemberModifiers。断言 82→182。

### 覆盖增量

- 12 个 is* 谓词对全部 4096 个掩码穷举真值表；代表组合（全 1 掩码/去位/XOR/未知高位）。
- toString 规范序精确串（PSF 输入顺序无关、0xfff 全串、六种组合串、同掩码重复调用 equals、
  constructorModifiers/fieldModifiers 掩码串全等）。
- 6 个 *Modifiers() 按 javadoc 逐位包含/排除断言。
- 真实成员端到端（String/CharSequence/int.class/Integer.valueOf/Object.hashCode/
  String.value + 自造 AbsSample/IfaceSample/MemberSample 与 native/synchronized/
  transient/volatile/final 成员位与串全等）。

### 版本差异（对照 JDK-8，javap+探针实测）

- javap -public：27 相对 8 无新增方法；Modifier 在 27 为 final 无 public 构造器（旧断言覆盖）。
- strictfp（关键差异）：javac-17+（JEP 306）不再向 class 文件写 ACC_STRICT —— 源码级
  `public static strictfp double sfpM(double)` 编译后 getModifiers()==PUBLIC|STATIC(0x9)，
  toString="public static"（javac-8 则为 0x809/"public static strictfp"）。本侧按实测断言
  0x9，并保留 STRICT 位/令牌经常量的可观测性断言。
- 其余（常量位、谓词、toString 串、掩码值）双侧逐字节一致。
