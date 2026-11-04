# java.io.ObjectInputFilter.FilterInfo 测试方案 (DESIGN)

## 1. API 功能（按方法分组）
- 回调上下文接口（函数式）：`serialClass()`（当前类，字段级回调为 null）、`arrayLength()`（非数组 -1）、`depth()`（根 1）、`references()`（累计引用计数）、`streamBytes()`（累计字节数）。

## 2. Java 用户如何使用
- lambda 过滤器读取上下文做限额：`info -> info.depth() > 4 || info.references() > 1000 ? Status.REJECTED : Status.UNDECIDED`（JEP 290 反序列化防护）。

## 3. 开源代码实际用法调研（真实片段，来源标注）
- 接口定义与语义注释 — `jdk25u-dev/src/java.base/share/classes/java/io/ObjectInputFilter.java`（FilterInfo 注释与方法）。
- 官方测试 — `jdk25u-dev/test/jdk/java/io/Serializable/serial_filter/SerialFilterFunctionTest.java`（depth/refs/bytes 断言模式）。

## 4. 测试用例设计 + 自动化实现（FilterInfoTest.testBlackBox，探针 P14/P15 先行）
1. 白盒：接口、恰好 5 个抽象访问器、serialClass→Class、其余→long、无常量。
2. 自定义类根对象：serialClass 精确、arrayLength==-1、depth==1、references==1、streamBytes>0。
3. 数组 String[7]：类描述阶段回调（arr=-1）+ 长度检查阶段回调（arr=7），depth 恒 1。
4. 嵌套 Node→next：根 depth 1、后续回调 depth 2 且 serialClass==null、references 递增、streamBytes 非降。
5. **String 走 fast path 从不触发 filter**（REJECTED 也放行、回调计数为 0）——探针实证。
6. 同流两实例：类描述只回调一次、references 计到第二实例、bytes 增长。
- 健壮性: 每流独立 setObjectInputFilter（不动全局 factory/serialFilter）；无线程。

产出：`FilterInfoTest.java`（白盒 + 黑盒共 42 断言）。

## 深化追加（契约级）

新增维度（`testNotFunctional`…`testThreeLevelChain`，断言 42 → 59）：
- **结构**：getDeclaredMethods() 恰 5（无静态/默认方法）；**非** @FunctionalInterface（5 个抽象访问器）。
- **int[3] 双阶段回调（实测）**：恰 2 次回调 —— 第 1 次 `[I, arrayLength=-1`（描述符阶段，长度未知），第 2 次 `[I, arrayLength=3`（长度阶段）；两次 depth 均 1。
- **枚举双回调（实测）**：反序列化 DayOfWeek 触发 2 次回调 —— `java.time.DayOfWeek@depth1/refs1` 后 `java.lang.Enum@depth2/refs2`（枚举超类描述符单独过过滤器）。
- **三级链（实测）**：Node→Node→Node 恰 3 次回调 `[Node d1 r1][null d2 r3][null d3 r4]` —— 同类嵌套实例的回调 **serialClass 为 null**（无新类描述符，仅有类回调携带类）；references 与 streamBytes 随层级**严格递增**。
- **不适用项**：简报提及的 `existingDepth` 访问器不存在（FilterInfo 只有 5 个访问器，已用结构断言锁死）；String 快捷路径（不过滤）已在原有用例覆盖。

负例矩阵：FilterInfo 为纯数据回调接口，无自身抛异常路径（负例归 ObjectInputFilter/Config 侧）。
