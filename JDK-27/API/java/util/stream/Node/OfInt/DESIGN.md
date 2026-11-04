# java.util.stream.Node.OfInt 测试方案

该类型为**包私有 SPI**（非 public，无法在包外引用/实例化），白盒反射断言在 `OfIntTest.testWhiteBox()`，黑盒仅能断言其不可作为公共 API 触达（`testBlackBox()`）。自动化实现见 `OfIntTest.java`。

## 1. API 功能（按方法分组）

- **继承**：`extends Node.OfPrimitive<Integer, IntConsumer, int[], Spliterator.OfInt, Node.OfInt>`。
- **方法**：`forEach(IntConsumer)`、`copyInto(int[], int)`、`truncate(...)`、`newArray(int)`、`getShape()`。
## 2. Java 用户如何使用

- 用户不可直接引用（包私有）；`Node.OfInt` 描述一块 int[] 数据，由 `Nodes.node(int[])` 产出并内部消费。
## 3. 开源代码实际用法调研

> 说明：本环境 WebSearch 不可用，调研基于 JDK-27 实现源码 `/data/yansendao/git/jdk25u-dev/src` 与自带测试 `test/jdk`，逐条标注 file:line，未凭记忆编造。

- `src/.../java/util/stream/Nodes.java:193`：`static Node.OfInt node(int[] array)`。
- `src/.../java/util/stream/Nodes.java:69`：`EMPTY_INT_NODE`（空 Node）。
## 4. 测试用例设计 + 自动化实现

- **白盒**：`isInterface`、继承 `Node$OfPrimitive`、`forEach/copyInto/truncate/newArray/getShape` 方法形态。
- **黑盒**：包私有 SPI，断言其不可作为公共 API 触达。
- **自动化**：`OfIntTest.testWhiteBox()` + `testBlackBox()`。
- **健壮性**：纯反射、固定输入；无墙钟/无 Random；不改全局状态；无文件/端口/网络/线程/`System.exit`；纯 headless（无 `@key`）。
