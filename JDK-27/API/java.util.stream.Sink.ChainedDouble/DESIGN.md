# java.util.stream.Sink.ChainedDouble 测试方案

该类型为**包私有 SPI**（非 public，无法在包外引用/实例化），白盒反射断言在 `ChainedDoubleTest.testWhiteBox()`，黑盒仅能断言其不可作为公共 API 触达（`testBlackBox()`）。自动化实现见 `ChainedDoubleTest.java`。

## 1. API 功能（按方法分组）

- **类**：抽象类 `implements Sink.OfDouble`，持有 `protected final Sink<? super E_OUT> downstream`。
- **方法**：`begin(long)`、`end()`、`cancellationRequested()` 委托下游；构造器接收下游 Sink。
## 2. Java 用户如何使用

- 用户不可直接引用（包私有）；内部管道中间操作继承它实现“链式 Sink”。
## 3. 开源代码实际用法调研

> 说明：本环境 WebSearch 不可用，调研基于 JDK-27 实现源码 `/data/yansendao/git/jdk25u-dev/src` 与自带测试 `test/jdk`，逐条标注 file:line，未凭记忆编造。

- `src/.../java/util/stream/DoublePipeline.java:174`：`return new Sink.ChainedDouble<>(sink) {...}`（map 等中间操作）。
## 4. 测试用例设计 + 自动化实现

- **白盒**：抽象类、超类 Object、实现 `Sink$OfDouble`、`begin/end/cancellationRequested`、`downstream` 字段、单参构造器。
- **黑盒**：包私有 SPI，断言其不可作为公共 API 触达。
- **自动化**：`ChainedDoubleTest.testWhiteBox()` + `testBlackBox()`。
- **健壮性**：纯反射、固定输入；无墙钟/无 Random；不改全局状态；无文件/端口/网络/线程/`System.exit`；纯 headless（无 `@key`）。
