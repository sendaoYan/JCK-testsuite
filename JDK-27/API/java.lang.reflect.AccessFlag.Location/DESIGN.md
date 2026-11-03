# java.lang.reflect.AccessFlag.Location — 测试设计（JDK-27）

## 1. API 功能
`AccessFlag.Location` 嵌套枚举（JDK-19+）标记 access flag 可能出现的位置：
- 9 个常量：CLASS、FIELD、METHOD、INNER_CLASS、METHOD_PARAMETER、MODULE、MODULE_REQUIRES、MODULE_EXPORTS、MODULE_OPENS。
- `flags()` / `flags(ClassFileFormatVersion)`：该位置合法的 flag 集合（版本敏感）。
- `flagsMask()` / `flagsMask(version)`：集合的 OR 掩码。

## 2. Java 用户如何使用
解码 class 文件（含历史版本字节码）时按位置取合法集合做校验；与 `AccessFlag.maskToAccessFlags` 互为逆运算。ClassFile API/ASM 风格工具在逐结构（字段/方法/模块属性）遍历时消费。

## 3. 开源代码实际用法调研
- JDK 源码 `~/git/jdk25u-dev/src/java.base/share/classes/java/lang/reflect/AccessFlag.java`（Location 内部以 `flags`/`maskWork` 表驱动）。
- JDK 自测 `~/git/jdk25u-dev/test/jdk/java/lang/reflect/AccessFlag/ContextTest.java`（位置↔flag 二维覆盖，含版本矩阵）。
- `java.lang.classfile` 属性模型对 MODULE_REQUIRES/EXPORTS/OPENS 的 flag 枚举走同一张表（场景 2）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`LocationTest.testBlackBox`）：
1. 9 常量、声明序（CLASS/FIELD/METHOD 打头）、未知名 IAE。
2. 每位置精确集合（JVMS 表）：CLASS 9 个（顶层类无 PRIVATE/PROTECTED/STATIC；含 SUPER/MODULE/ANNOTATION/ENUM）、FIELD 9、METHOD 11（latest 无 STRICT）、INNER_CLASS 10（无 SUPER/MODULE）、METHOD_PARAMETER 3、MODULE 3（MODULE flag 本身在 CLASS）、MODULE_REQUIRES 4（TRANSITIVE/STATIC_PHASE）、EXPORTS/OPENS 均 {SYNTHETIC,MANDATED} 且互为镜像。
3. 往返一致性：全部位置 `maskToAccessFlags(flagsMask(), loc) == flags()`；flagsMask == 成员 mask 之 OR。
4. 版本敏感：STRICT 仅在 RELEASE_8 的 METHOD（0x15ff→0x1dff 字面值）；ANNOTATION 在 RELEASE_5 已合法；MODULE 不在 RELEASE_5；FIELD 集合跨版本稳定。
5. ordinal 序、getDeclaringClass。
白盒保留原有 6 项断言。产出：`LocationTest.java`（56 断言通过）。
