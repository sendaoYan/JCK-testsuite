# javax.swing.plaf.basic.BasicTableHeaderUI.MouseInputHandler — DESIGN

## 1. API 功能
`BasicTableHeaderUI` 的内部类，实现 `MouseInputListener`：`mouseClicked`/`mousePressed` 等。

## 2. Java 用户如何使用
由 `BasicTableHeaderUI` 在安装监听器/布局时创建并注册；测试通过外部类实例 `new BasicTableHeaderUI()` 再用 `o.new MouseInputHandler(...)`（或 `new MouseInputHandler(...)`）实例化，并断言其运行时类型契约。

## 3. 开源代码实际用法调研（来源：JDK 源码 jdk25u-dev + test/jdk）
来源：JDK 源码 `src/java.desktop/share/classes/javax/swing/plaf/basic/BasicTableHeaderUI.java`（jdk25u-dev）。该类是 `BasicTableHeaderUI` 的 public 内部类，作为事件监听器/布局管理器被注册到组件。

## 4. 测试用例设计 + 自动化实现（`MouseInputHandlerTest.testBlackBox`）
实例化 → instanceof MouseInputListener/父类 java.lang.Object 断言
