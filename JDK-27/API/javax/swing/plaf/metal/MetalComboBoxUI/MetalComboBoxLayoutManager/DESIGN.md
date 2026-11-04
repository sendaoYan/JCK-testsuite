# javax.swing.plaf.metal.MetalComboBoxUI.MetalComboBoxLayoutManager — DESIGN

## 1. API 功能
`MetalComboBoxUI` 的内部类，继承 `BasicComboBoxUI.ComboBoxLayoutManager`：`layoutContainer`/`superLayout`。

## 2. Java 用户如何使用
由 `MetalComboBoxUI` 在安装监听器/布局时创建并注册；测试通过外部类实例 `new MetalComboBoxUI()` 再用 `o.new MetalComboBoxLayoutManager(...)`（或 `new MetalComboBoxLayoutManager(...)`）实例化，并断言其运行时类型契约。

## 3. 开源代码实际用法调研（来源：JDK 源码 jdk25u-dev + test/jdk）
来源：JDK 源码 `src/java.desktop/share/classes/javax/swing/plaf/metal/MetalComboBoxUI.java`（jdk25u-dev）。该类是 `MetalComboBoxUI` 的 public 内部类，作为事件监听器/布局管理器被注册到组件。

## 4. 测试用例设计 + 自动化实现（`MetalComboBoxLayoutManagerTest.testBlackBox`）
实例化 → instanceof 无直接接口/父类 ComboBoxLayoutManager 断言
