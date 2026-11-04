# javax.swing.plaf.basic.BasicSplitPaneUI.BasicVerticalLayoutManager — DESIGN

## 1. API 功能
`BasicSplitPaneUI` 的内部类，继承 `BasicHorizontalLayoutManager`（间接实现 `LayoutManager2`）：垂直分栏布局。

## 2. Java 用户如何使用
由 `BasicSplitPaneUI` 在安装监听器/布局时创建并注册；测试通过外部类实例 `new BasicSplitPaneUI()` 再用 `o.new BasicVerticalLayoutManager(...)`（或 `new BasicVerticalLayoutManager(...)`）实例化，并断言其运行时类型契约。

## 3. 开源代码实际用法调研（来源：JDK 源码 jdk25u-dev + test/jdk）
来源：JDK 源码 `src/java.desktop/share/classes/javax/swing/plaf/basic/BasicSplitPaneUI.java`（jdk25u-dev）。该类是 `BasicSplitPaneUI` 的 public 内部类，作为事件监听器/布局管理器被注册到组件。

## 4. 测试用例设计 + 自动化实现（`BasicVerticalLayoutManagerTest.testBlackBox`）
实例化 → instanceof 无直接接口/父类 java.lang.Object 断言 + 字段/访问器行为断言
