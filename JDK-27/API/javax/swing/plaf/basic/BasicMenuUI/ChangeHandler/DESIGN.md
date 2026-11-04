# javax.swing.plaf.basic.BasicMenuUI.ChangeHandler — DESIGN

## 1. API 功能
`BasicMenuUI` 的内部类，实现 `ChangeListener`：`stateChanged`，含 public 字段 `menu`/`ui`/`isSelected`/`wasFocused`。

## 2. Java 用户如何使用
由 `BasicMenuUI` 在安装监听器/布局时创建并注册；测试通过外部类实例 `new BasicMenuUI()` 再用 `o.new ChangeHandler(...)`（或 `new ChangeHandler(...)`）实例化，并断言其运行时类型契约。

## 3. 开源代码实际用法调研（来源：JDK 源码 jdk25u-dev + test/jdk）
来源：JDK 源码 `src/java.desktop/share/classes/javax/swing/plaf/basic/BasicMenuUI.java`（jdk25u-dev）。该类是 `BasicMenuUI` 的 public 内部类，作为事件监听器/布局管理器被注册到组件。

## 4. 测试用例设计 + 自动化实现（`ChangeHandlerTest.testBlackBox`）
实例化 → instanceof ChangeListener/父类 java.lang.Object 断言 + 字段/访问器行为断言
