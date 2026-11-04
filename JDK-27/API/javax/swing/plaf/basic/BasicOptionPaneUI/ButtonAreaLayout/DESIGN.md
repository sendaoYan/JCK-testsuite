# javax.swing.plaf.basic.BasicOptionPaneUI.ButtonAreaLayout — DESIGN

## 1. API 功能
`BasicOptionPaneUI` 的静态内部类，实现 `LayoutManager`：`setSyncAllWidths`/`getSyncAllWidths`/`setPadding`/`getPadding`/`setCentersChildren`/`getCentersChildren`、`layoutContainer`、`preferredLayoutSize`。

## 2. Java 用户如何使用
由 `BasicOptionPaneUI` 在安装监听器/布局时创建并注册；测试通过外部类实例 `new BasicOptionPaneUI()` 再用 `o.new ButtonAreaLayout(...)`（或 `new ButtonAreaLayout(...)`）实例化，并断言其运行时类型契约。

## 3. 开源代码实际用法调研（来源：JDK 源码 jdk25u-dev + test/jdk）
来源：JDK 源码 `src/java.desktop/share/classes/javax/swing/plaf/basic/BasicOptionPaneUI.java`（jdk25u-dev）。该类是 `BasicOptionPaneUI` 的 public 内部类，作为事件监听器/布局管理器被注册到组件。

## 4. 测试用例设计 + 自动化实现（`ButtonAreaLayoutTest.testBlackBox`）
实例化 → instanceof LayoutManager/父类 java.lang.Object 断言 + 字段/访问器行为断言
