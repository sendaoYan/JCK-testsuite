# javax.swing.plaf.basic.BasicScrollPaneUI.HSBChangeListener — DESIGN

## 1. API 功能
`BasicScrollPaneUI` 的内部类，实现 `ChangeListener`：`stateChanged`。

## 2. Java 用户如何使用
由 `BasicScrollPaneUI` 在安装监听器/布局时创建并注册；测试通过外部类实例 `new BasicScrollPaneUI()` 再用 `o.new HSBChangeListener(...)`（或 `new HSBChangeListener(...)`）实例化，并断言其运行时类型契约。

## 3. 开源代码实际用法调研（来源：JDK 源码 jdk25u-dev + test/jdk）
来源：JDK 源码 `src/java.desktop/share/classes/javax/swing/plaf/basic/BasicScrollPaneUI.java`（jdk25u-dev）。该类是 `BasicScrollPaneUI` 的 public 内部类，作为事件监听器/布局管理器被注册到组件。

## 4. 测试用例设计 + 自动化实现（`HSBChangeListenerTest.testBlackBox`）
实例化 → instanceof ChangeListener/父类 java.lang.Object 断言
