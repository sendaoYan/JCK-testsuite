# JVMS 4.11 Limitations of the Java Virtual Machine 测试设计

## 1. 覆盖的规范上限

JVMS 4.11 列出的 class 文件维度上限（均为 u2 编码所限）：
constant_pool_count ≤ 65535、methods/fields_count ≤ 65535、code_length ≤ 65535、
名称（CONSTANT_Utf8_info 长度域）≤ 65535 字节、方法参数 ≤ 255、数组维度 ≤ 255。

设计思路借鉴 JCK vm/classfmt/lmt 的"恰好达限正值接受 + count 失配 malformed 负例"
二分法（仅学设计，实现全部自研：DataOutputStream 手写 class 字节 +
URLClassLoader.defineClass 加载）；所有生成类只用 Utf8/Class 单槽 CP 条目，
避免 long/double 双槽对齐问题。

## 2. 测试清单

| 测试 | 断言内容 |
|---|---|
| ConstantPoolCountPositiveTest | cp_count=65535（65534 条）加载成功（既有） |
| MethodsCountPositiveTest | methods_count=65535 个 abstract 方法加载成功（既有） |
| FieldsCountPositiveTest | fields_count=65535 加载成功（既有） |
| CodeLengthPositiveTest | code_length=65535 加载并执行（既有） |
| CodeLengthLimitTest | code_length=65536 → ClassFormatError（既有, jasm） |
| ParamCountPositiveTest / ManyArgsLimitTest | 255 参数正例 / 256 参数负例（既有, jasm） |
| NameLengthTest | 字段/方法名恰 65535 字节 UTF-8 → 加载成功且反射可见；空名 → ClassFormatError |
| DimensionsTest | 描述符 255 个 '[' → 加载成功 + getComponentType 链 255 层到 int + Array.newInstance 造 255 维实例；256 个 '['（可写出）→ ClassFormatError |
| StackAndLocalsTest | max_stack=65535/max_locals=65535 远超实际用量 → 接受；max_stack=0 但 iconst 压栈 → VerifyError；max_locals=0 但 astore_1 → VerifyError（4.9 约束） |
| CountMismatchNegativesTest | methods_count 声明 10 实给 3 → ClassFormatError；constant_pool_count=0 → ClassFormatError；name_index=0 → ClassFormatError；name_index==count（首个无效索引）→ ClassFormatError；fields_count=0 → 正常加载（正值对照） |

## 3. 异常锁定表（JDK-8 jdk8u372-b07 与 JDK-27 27.0.1.0.2 实测一致）

| 构造 | 异常 | 消息前缀 |
|---|---|---|
| 方法名空串 | ClassFormatError | `Illegal method name ""` |
| 字段名空串 | ClassFormatError | `Illegal field name ""` |
| 描述符 256 维 | ClassFormatError | `Array type descriptor has more than 255 dimensions` |
| max_stack=0 + iconst_1 | VerifyError | `Operand stack overflow`（详情 Exceeded max stack size） |
| max_locals=0 + astore_1 | VerifyError | `Local variable table overflow`（详情 Local index 1 is invalid） |
| methods_count 10 实给 3 | ClassFormatError | `Truncated class file` |
| constant_pool_count=0 | ClassFormatError | `Illegal constant pool size 0` |
| name_index=0 | ClassFormatError | `Illegal constant pool index 0 for method name` |
| name_index==count | ClassFormatError | `Illegal constant pool index 6 for method name` |

## 4. 版本差异

无：全部构造在 JDK-8 与 JDK-27 上异常类与消息逐字一致，双侧共用同一断言。
生成类为版本 52.0，两个 JDK 均接受。

## 5. 运行纪律

纯 headless @run main（无图形/声卡/打印机，无需 @key）；每文件独立 workDir；
jtreg 直至双侧 0 fail。既有测试一律不改不删。
