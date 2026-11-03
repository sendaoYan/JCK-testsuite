# java.util.stream.Node.OfDouble 测试方案

该类型为**包私有 SPI**（非 public，无法在包外引用/实例化），白盒反射断言在 `OfDoubleTest.testWhiteBox()`，黑盒仅能断言其不可作为公共 API 触达（`testBlackBox()`）。自动化实现见 `OfDoubleTest.java`。

## 1. API 功能（按方法分组）

- **继承**：`extends Node.OfPrimitive<Double, DoubleConsumer, double[], Spliterator.OfDouble, Node.OfDouble>`。
- **方法**：`forEach(DoubleConsumer)`、`copyInto(double[], int)`、`truncate(...)`、`newArray(int)`、`getShape()`。
## 2. Java 用户如何使用

- 用户不可直接引用（包私有）；`Node.OfDouble` 描述一块 double[] 数据，由 `Nodes.node(double[])` 产出并内部消费。
## 3. 开源代码实际用法调研

> 说明：本环境 WebSearch 不可用，调研基于 JDK-27 实现源码 `/data/yansendao/git/jdk25u-dev/src` 与自带测试 `test/jdk`，逐条标注 file:line，未凭记忆编造。

- `src/.../java/util/stream/Nodes.java:69-71`：`EMPTY_INT/LONG/DOUBLE_NODE`（空 Node 单例）。
- `src/.../java/util/stream/Nodes.java:267`：`static Node.OfDouble node(final double[] array)`。
## 4. 测试用例设计 + 自动化实现

- **白盒**：`isInterface`、继承 `Node$OfPrimitive`、`forEach/copyInto/truncate/newArray/getShape` 方法形态。
- **黑盒**：包私有 SPI，断言其不可作为公共 API 触达。
- **自动化**：`OfDoubleTest.testWhiteBox()` + `testBlackBox()`。
- **健壮性**：纯反射、固定输入；无墙钟/无 Random；不改全局状态；无文件/端口/网络/线程/`System.exit`；纯 headless（无 `@key`）。
