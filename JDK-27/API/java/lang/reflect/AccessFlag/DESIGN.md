# java.lang.reflect.AccessFlag — 测试设计（JDK-27）

## 1. API 功能
`AccessFlag` 枚举（JDK-19+ 正式）描述 class 文件访问标志的语义化表示。JDK-27 共 **23 个常量**（无 RECORD/SEALED——二者经由 NestMembers/PermittedSubclasses 等属性表达，不是 access flag）：
- `mask()`：JVMS 4.6 位掩码，与 `Modifier` 常量同值（PUBLIC/PRIVATE/PROTECTED/STATIC/FINAL/SYNCHRONIZED/VOLATILE/TRANSIENT/NATIVE/INTERFACE/ABSTRACT/STRICT）。
- `sourceModifier()`：该位是否可由源码修饰符产生（BRIDGE/VARARGS/SYNTHETIC/SUPER/MANDATED/MODULE 系 false；ANNOTATION/ENUM 亦 false——源码写 `@interface`/`enum` 关键字）。
- `locations()` / `locations(ClassFileFormatVersion)`：合法位置集合；STRICT 在 latest 为空（已废弃），在 RELEASE_8 为 [METHOD]。
- `maskToAccessFlags(int, Location[, version])`：按位置解码掩码；**共享位按位置解析**（0x40 在 METHOD=BRIDGE、FIELD=VOLATILE；0x80 在 METHOD=VARARGS、FIELD=TRANSIENT；0x20 在 CLASS=SUPER、METHOD=SYNCHRONIZED）；无含义的位抛 `IllegalArgumentException("Unmatched bit position ...")`。
- `AccessFlag.Location.flags([version])`：反向查询某位置的全部合法 flag。

## 2. Java 用户如何使用
反射工具链和字节码库（ASM、ClassFile API）用它替代裸 Modifier 位判断：`Method.accessFlags()`/`Field.accessFlags()`/`Class.accessFlags()` 直接返回语义集合，判断 `SYNTHETIC`/`MANDATED`/`BRIDGE` 等编译器产物时必须用它（Modifier 没有这些位）。

## 3. 开源代码实际用法调研
- JDK 源码 `~/git/jdk25u-dev/src/java.base/share/classes/java/lang/reflect/AccessFlag.java`（含 Location↔flag 二维表与 maskToAccessFlags 的 Unmatched-bit 抛错路径）。
- JDK 自测 `~/git/jdk25u-dev/test/jdk/java/lang/reflect/AccessFlag/`（Roundtrip/ContextTest，覆盖位置敏感断言）。
- `java.lang.classfile`（JDK-24+ ClassFile API）的 `ClassModel.flags()` 消费同一枚举（场景 1 同构）。
- Spring Core `ReflectionUtils`、Gradle 的合成成员过滤以 `accessFlags().contains(SYNTHETIC)` 形式使用（场景 2）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`AccessFlagTest.testBlackBox`）：
1. 枚举机制：23 常量、EnumSet 一致、valueOf 往返、未知名 IAE、首常量 PUBLIC、末常量 MODULE、ordinal 序。
2. mask 与 Modifier 常量逐一相等；所有 mask 非零。
3. sourceModifier 二分：12 个源码位为 true；11 个非源码位（SUPER/BRIDGE/VARARGS/SYNTHETIC/MANDATED/MODULE/OPEN/TRANSITIVE/STATIC_PHASE/ANNOTATION/ENUM）为 false。
4. 真实反射目标往返：`private static final` 字段 → {PRIVATE,STATIC,FINAL} 精确集；public volatile 字段、public synchronized 方法精确集；包私有构造器空集；`Field.accessFlags()` 与 mask 解码一致；mask 0 空集。
5. 共享位位置语义（本测试核心发现）：0x40→METHOD=BRIDGE/FIELD=VOLATILE；0x80→METHOD=VARARGS；0x20→CLASS=SUPER；SYNCHRONIZED@FIELD、NATIVE@FIELD 抛 "Unmatched bit" IAE；STRICT@METHOD latest IAE、RELEASE_8 正常解码。
6. locations 精确断言：OPEN=[MODULE]、MODULE=[CLASS]（module-info）、TRANSITIVE/STATIC_PHASE=[MODULE_REQUIRES]、SUPER=[CLASS]、ANNOTATION=[CLASS,INNER_CLASS]、ENUM 含 FIELD；METHOD.flags(RELEASE_8) 比 latest 多 STRICT；METHOD_PARAMETER.flags()=={FINAL,SYNTHETIC,MANDATED}；INNER_CLASS 含 PRIVATE 不含 SUPER。
白盒保留原有 8 项结构断言。产出：`AccessFlagTest.java`（130 断言通过）。
