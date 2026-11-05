# java.lang.classfile.ClassFile.ShortJumpsOption — 设计案

## 1. API 功能
`java.lang.classfile.ClassFile.ShortJumpsOption`（enum）。工厂/静态方法：values, valueOf。查询/实例方法：(none)。枚举常量：FIX_SHORT_JUMPS, FAIL_ON_SHORT_JUMPS

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；枚举通过 `valueOf`/`values` 与常量精确断言。 本测试在 `ShortJumpsOptionTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.ClassFile.ShortJumpsOption` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`ShortJumpsOptionTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.ClassFile$ShortJumpsOption")` + public/enum/继承 + 关键成员。黑盒（`ShortJumpsOptionTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

## 5. 深化条目（2026-11-05：FIX vs FAIL 长跳转实测，40000 nop 反向跳）
新增 `testOptionSemantics()`（30 断言，12→30）：
- **场景构造**：反向 goto（labelBinding 在前、nop×N、goto_ 收尾）——无死代码、无需 SMT、跳距=N+3（对齐 JDK ShortJumpsFixTest.generateBack 思路，避免死码 patch 干扰断言）。
- **短跳（1 nop）**：两选项产物同 [NOP, GOTO]——FIX 不会把短跳误扩成 GOTO_W。
- **长跳（40000 nop）**：FIX → build 成功，末指令 **GOTO_W**（自动加宽），指令数 40001、CodeAttribute.codeLength==40005（40000×1 + GOTO_W 5 字节）、verify 干净；FAIL_ON_SHORT_JUMPS → build 抛 **IAE "Label target offset overflow"**（DirectCodeBuilder$LabelOverflowException 是 IAE 子类，捕获 IAE 即可）。
- **边界内（30000 nop）**：FAIL 下仍成功且保持 GOTO 不加宽。
- 显式 `cob.goto_w(label)` 发射存在性断言；FIX 产物 transform(ACCEPT_ALL) 后 GOTO_W 保持、verify 干净。
- 枚举 ordinal 0 = FIX_SHORT_JUMPS（默认）。
- 易错点：40000 次 `cob.nop()` 循环开销可忽略（毫秒级），无需改小规模；若用前向 goto 会引入死代码区被 PATCH_DEAD_CODE 改写，干扰指令序列断言。
