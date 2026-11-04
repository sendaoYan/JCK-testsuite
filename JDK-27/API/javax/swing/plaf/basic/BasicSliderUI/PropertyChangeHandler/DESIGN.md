# javax.swing.plaf.basic.BasicSliderUI.PropertyChangeHandler — DESIGN

## 1. API 功能
`BasicSliderUI` 的内部类，实现 `PropertyChangeListener`：`propertyChange`。

## 2. Java 用户如何使用
由 `BasicSliderUI` 在安装监听器/布局时创建并注册；测试通过外部类实例 `new BasicSliderUI()` 再用 `o.new PropertyChangeHandler(...)`（或 `new PropertyChangeHandler(...)`）实例化，并断言其运行时类型契约。

## 3. 开源代码实际用法调研（来源：JDK 源码 jdk25u-dev + test/jdk）
来源：JDK 源码 `src/java.desktop/share/classes/javax/swing/plaf/basic/BasicSliderUI.java`（jdk25u-dev）。该类是 `BasicSliderUI` 的 public 内部类，作为事件监听器/布局管理器被注册到组件。

## 4. 测试用例设计 + 自动化实现（`PropertyChangeHandlerTest.testBlackBox`）
实例化 → instanceof PropertyChangeListener/父类 java.lang.Object 断言
