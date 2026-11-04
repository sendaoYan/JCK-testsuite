# java.util.stream.Gatherers 测试方案

白盒结构断言在 `GatherersTest.testWhiteBox()`，黑盒行为断言在 `GatherersTest.testBlackBox()`。自动化实现见 `GatherersTest.java` 黑盒部分。

## 1. API 功能（按方法分组）

- **工厂（全部 static）**：`windowFixed(int)`、`windowSliding(int)`、`fold(Supplier, BiFunction)`、`scan(Supplier, BiFunction)`、`mapConcurrent(int, Function)`。
## 2. Java 用户如何使用

- 构造：`Gatherers.windowFixed(2)`、`Gatherers.fold(() -> 0, Integer::sum)`。
- 典型调用：`Stream.of(1..5).gather(Gatherers.windowFixed(2)).toList()`。
- 真实组合链路：`gather(Gatherers.scan(...))` → 前缀和流。
## 3. 开源代码实际用法调研

> 说明：本环境 WebSearch 不可用，调研基于 JDK-27 实现源码 `/data/yansendao/git/jdk25u-dev/src` 与自带测试 `test/jdk`，逐条标注 file:line，未凭记忆编造。

- `src/.../java/util/stream/test/.../GatherersMapConcurrentTest.java:94`：`Gatherers.<String,String>mapConcurrent(level, s -> s)`。
- `src/.../java/util/stream/test/.../GatherersMapConcurrentTest.java:110-111`：`stream.gather(Gatherers.mapConcurrent(2, x -> {...}))`。
## 4. 测试用例设计 + 自动化实现

- **正常**：`windowFixed` 含残窗/整倍数；`windowSliding` 重叠窗口；`fold` 归约单元素（含字符串拼接）；`scan` 前缀和；`mapConcurrent` 应用映射。
- **边界/异常**：`windowFixed(0)`/`windowSliding(0)`/`windowSliding(-1)`/`mapConcurrent(0, ...)` 抛 `IllegalArgumentException`。
- **自动化**：`GatherersTest.testBlackBox()`。
- **健壮性**：固定输入、无墙钟/无 Random、无 HashMap/HashSet 迭代顺序断言；不改 `System.*`/`TimeZone`/`Locale` 默认；无文件/端口/网络/线程/`System.exit`；纯 headless（无 `@key`）。
