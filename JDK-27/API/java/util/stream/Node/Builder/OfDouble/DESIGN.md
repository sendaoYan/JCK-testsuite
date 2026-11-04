# java.util.stream.Node.Builder.OfDouble 测试方案

该类型为**包私有 SPI**（非 public，无法在包外引用/实例化），白盒反射断言在 `OfDoubleTest.testWhiteBox()`，黑盒仅能断言其不可作为公共 API 触达（`testBlackBox()`）。自动化实现见 `OfDoubleTest.java`。

## 1. API 功能（按方法分组）

- **继承**：`extends Node.Builder<Double>, Sink.OfDouble`。
- **方法**：`build()` 产出 `Node.OfDouble`；`accept(double)` 继承自 `Sink.OfDouble`。
## 2. Java 用户如何使用

- 用户不可直接引用（包私有）；由 `Nodes.doubleBuilder()` 等内部工厂实现并消费。
- 内部实现：累积 double 元素后 `build()` 出 `Node.OfDouble`。
## 3. 开源代码实际用法调研

> 说明：本环境 WebSearch 不可用，调研基于 JDK-27 实现源码 `/data/yansendao/git/jdk25u-dev/src` 与自带测试 `test/jdk`，逐条标注 file:line，未凭记忆编造。

- `src/.../java/util/stream/Nodes.java:1594`：`implements Node.Builder.OfDouble`。
## 4. 测试用例设计 + 自动化实现

- **白盒**：`isInterface`、包私有、继承 `Node$Builder` + `Sink$OfDouble`、`build()`/`accept(double)` 存在。
- **黑盒**：包私有 SPI，断言其不可作为公共 API 触达。
- **自动化**：`OfDoubleTest.testWhiteBox()` + `testBlackBox()`。
- **健壮性**：纯反射、固定输入；无墙钟/无 Random；不改全局状态；无文件/端口/网络/线程/`System.exit`；纯 headless（无 `@key`）。
