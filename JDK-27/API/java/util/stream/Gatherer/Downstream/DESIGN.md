# java.util.stream.Gatherer.Downstream 测试方案

白盒结构断言在 `DownstreamTest.testWhiteBox()`，黑盒行为断言在 `DownstreamTest.testBlackBox()`。自动化实现见 `DownstreamTest.java` 黑盒部分。

## 1. API 功能（按方法分组）

- **抽象方法**：`boolean push(T)` —— 向下游发射一个元素，返回是否继续接受。
- **默认方法**：`boolean isRejecting()` —— 默认 false，表示下游是否在拒绝。
## 2. Java 用户如何使用

- 典型调用：在 integrator 中调用 `downstream.push(element)` 发射元素，根据返回值决定是否提前终止。
- 真实组合链路：integrator 检查 `downstream.isRejecting()` 后选择是否继续 push。
## 3. 开源代码实际用法调研

> 说明：本环境 WebSearch 不可用，调研基于 JDK-27 实现源码 `/data/yansendao/git/jdk25u-dev/src` 与自带测试 `test/jdk`，逐条标注 file:line，未凭记忆编造。

- `src/.../java/util/stream/test/.../GathererAPITest.java:216`：`gather(...).toList()` 中 Downstream 由框架注入。
## 4. 测试用例设计 + 自动化实现

- **正常**：`push` 返回 true 且元素被收集；默认 `isRejecting()==false`。
- **组合**：rejecting 状态下 `push` 返回 false、`isRejecting()` 反映状态。
- **自动化**：`DownstreamTest.testBlackBox()`。
- **健壮性**：固定输入、无墙钟/无 Random、无 HashMap/HashSet 迭代顺序断言；不改 `System.*`/`TimeZone`/`Locale` 默认；无文件/端口/网络/线程/`System.exit`；纯 headless（无 `@key`）。
