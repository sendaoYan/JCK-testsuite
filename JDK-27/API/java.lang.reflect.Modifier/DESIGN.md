# java.lang.reflect.Modifier — 测试设计（JDK-27）

## 1. API 功能
`Modifier` 工具类（final、私有构造、全静态）：
- 修饰符位常量：`PUBLIC/PRIVATE/PROTECTED/STATIC/FINAL/SYNCHRONIZED/VOLATILE/TRANSIENT/NATIVE/INTERFACE/ABSTRACT/STRICT`（及包内 BRIDGE/VARARGS/SYNTHETIC/ANNOTATION/ENUM/MANDATED）。
- 判定：`isPublic/isPrivate/isProtected/isStatic/isFinal/isSynchronized/isVolatile/isTransient/isNative/isInterface/isAbstract/isStrict(int)`。
- 展示：`toString(int)` 按规范次序输出（public protected private abstract static final transient volatile synchronized native strictfp interface）。
- 允许掩码：`classModifiers/interfaceModifiers/constructorModifiers/methodModifiers/fieldModifiers/parameterModifiers`。

## 2. Java 用户如何使用
拿 `Field/Method/Constructor/Class.getModifiers()` 的 int → `Modifier.isXxx` 组合判定或 `toString` 展示；允许掩码用于校验某类成员的修饰符合法性（IDE/字节码工具/校验器）。

## 3. 开源代码实际用法调研
- JDK 自测 `~/git/jdk25u-dev/test/jdk/java/lang/reflect/Modifier/`（含 toString 次序与掩码成员断言）。
- JDK 源码 `~/git/jdk25u-dev/src/java.base/share/classes/java/lang/reflect/Modifier.java`：canonical 输出次序与掩码常量的权威定义。
- Commons Lang `Modifier.isPublic` 系列包装（`MemberUtils`）；Spring `ReflectionUtils.isPublic/isPrivate` 同构（真实反射对象 + 判定组合，对应场景 1/2）。
- 注解处理器/字节码库（ASM `ACC_*` 对照）广泛用位常量对照掩码（场景 4）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`ModifierTest.testBlackBox`，固定目标类 `Targets/AbstractTarget`）：
1. 真实成员精确组合：`private static final`（toString 精确串）、`public transient`、`protected volatile`、`public synchronized`、`public native`。
2. 类/接口级：嵌套 abstract 类 "abstract static"、嵌套 public 接口 "public abstract static interface"、注解型接口标志。
3. `toString` 规范次序与退化：`public static final`、`abstract interface`、空串、单标志、`protected transient`。
4. 允许掩码逐成员判定：classModifiers/interfaceModifiers/methodModifiers/fieldModifiers/constructorModifiers/parameterModifiers 的包含与排除集合（以 JDK-27 实测为准：interfaceModifiers 不含 final；parameterModifiers 仅 final）。
白盒保留位常量值与 isXxx 判定。产出：`ModifierTest.java`（52 断言通过）。
