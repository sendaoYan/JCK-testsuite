# javax.swing.plaf.basic.BasicTreeUI.NodeDimensionsHandler — DESIGN

## 1. API 功能
`BasicTreeUI` 的内部类，继承 `AbstractLayoutCache.NodeDimensions`：`getNodeDimensions`/`getRowX`。

## 2. Java 用户如何使用
由 `BasicTreeUI` 在安装监听器/布局时创建并注册；测试通过外部类实例 `new BasicTreeUI()` 再用 `o.new NodeDimensionsHandler(...)`（或 `new NodeDimensionsHandler(...)`）实例化，并断言其运行时类型契约。

## 3. 开源代码实际用法调研（来源：JDK 源码 jdk25u-dev + test/jdk）
来源：JDK 源码 `src/java.desktop/share/classes/javax/swing/plaf/basic/BasicTreeUI.java`（jdk25u-dev）。该类是 `BasicTreeUI` 的 public 内部类，作为事件监听器/布局管理器被注册到组件。

## 4. 测试用例设计 + 自动化实现（`NodeDimensionsHandlerTest.testBlackBox`）
实例化 → instanceof 无直接接口/父类 NodeDimensions 断言
