# javax.swing.plaf.basic.BasicSliderUI.ComponentHandler — DESIGN

## 1. API 功能
`BasicSliderUI` 的内部类，继承 `ComponentAdapter`：`componentResized`。

## 2. Java 用户如何使用
由 `BasicSliderUI` 在安装监听器/布局时创建并注册；测试通过外部类实例 `new BasicSliderUI()` 再用 `o.new ComponentHandler(...)`（或 `new ComponentHandler(...)`）实例化，并断言其运行时类型契约。

## 3. 开源代码实际用法调研（来源：JDK 源码 jdk25u-dev + test/jdk）
来源：JDK 源码 `src/java.desktop/share/classes/javax/swing/plaf/basic/BasicSliderUI.java`（jdk25u-dev）。该类是 `BasicSliderUI` 的 public 内部类，作为事件监听器/布局管理器被注册到组件。

## 4. 测试用例设计 + 自动化实现（`ComponentHandlerTest.testBlackBox`）
实例化 → instanceof 无直接接口/父类 ComponentAdapter 断言
