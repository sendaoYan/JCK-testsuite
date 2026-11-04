# javax.swing.plaf.basic.BasicTreeUI.TreeCancelEditingAction — DESIGN

## 1. API 功能
`BasicTreeUI` 的内部类，继承 `AbstractAction`：`actionPerformed`/`isEnabled`，取消编辑动作。

## 2. Java 用户如何使用
由 `BasicTreeUI` 在安装监听器/布局时创建并注册；测试通过外部类实例 `new BasicTreeUI()` 再用 `o.new TreeCancelEditingAction(...)`（或 `new TreeCancelEditingAction(...)`）实例化，并断言其运行时类型契约。

## 3. 开源代码实际用法调研（来源：JDK 源码 jdk25u-dev + test/jdk）
来源：JDK 源码 `src/java.desktop/share/classes/javax/swing/plaf/basic/BasicTreeUI.java`（jdk25u-dev）。该类是 `BasicTreeUI` 的 public 内部类，作为事件监听器/布局管理器被注册到组件。

## 4. 测试用例设计 + 自动化实现（`TreeCancelEditingActionTest.testBlackBox`）
实例化 → instanceof 无直接接口/父类 AbstractAction 断言
