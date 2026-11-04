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

## 深化批次（2026-11-05）
新增 Part 3 四个 test 方法（字面值经探针在 JDK 27.0.1 实测锁定）：
1. `testFlagsMaskLiterals`：9 个位置 flagsMask 精确十六进制——CLASS=0xf631、FIELD=0x50df、METHOD=0x15ff、INNER_CLASS=0x761f、METHOD_PARAMETER=0x9010、MODULE=0x9020、MODULE_REQUIRES=0x9060、MODULE_EXPORTS=MODULE_OPENS=0x9000；RELEASE_8 视图 CLASS=0x7631（无 MODULE 位）、FIELD/INNER_CLASS/METHOD_PARAMETER 跨版本稳定。
2. `testModuleLocationsPreJava9`：**4 个模块位置（MODULE/MODULE_REQUIRES/MODULE_EXPORTS/MODULE_OPENS）在 RELEASE_8 下 flags() 为空、flagsMask()==0**，零掩码解码为空集；latest 下恢复非空——模块属性 RELEASE_9 才进 JVMS。
3. `testEnumPlumbing`：每位置 valueOf(name) 往返、toString==name、getDeclaringClass、flags 非空、flagsMask 非零（latest）；EnumSet.allOf 与 values() 一致；首 CLASS 末 MODULE_OPENS、ordinal 0 与 8。
4. `testUnmatchedBitPerLocation`（配 `expectUnmatched` helper）：9 个位置各取一个可证缺席位（CLASS:0x8、FIELD:0x20、METHOD:0x4000、INNER_CLASS:0x20、METHOD_PARAMETER:0x1、MODULE/MODULE_REQUIRES:0x1、EXPORTS/OPENS:0x20=TRANSITIVE 只在 REQUIRES）全部 IAE("Unmatched bit")。
产出：`LocationTest.java` 143 断言通过（旧 56）。

### 易错规范细节
- MODULE 与 MODULE_REQUIRES 的 flagsMask 差一个 0x40 位（STATIC_PHASE 只属于 requires）；EXPORTS 与 OPENS 掩码完全相同（0x9000）。
- flagsMask(version) 对模块位置在 RELEASE_8 返回 0 而不是抛异常——"位置本身不存在"表现为空集/零掩码，与 "位无含义抛 IAE"（maskToAccessFlags 非零未匹配位）是两种不同的负例形态。
