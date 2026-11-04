# javax.swing.plaf.nimbus.State — DESIGN

## 1. API 功能
`State<T extends JComponent>` 是 Nimbus 组件状态谓词基类（public abstract 泛型类，`extends Object`，无接口）。唯一方法 `toString()`（返回构造名）；protected 抽象方法 `isInState(T)` 由子类实现。来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/nimbus/State.java`（class=75）。

## 2. Java 用户如何使用
匿名子类定义状态：`new State<JComponent>("MyState") { protected boolean isInState(JComponent c){ return true; } }`；`toString()` 用于样式表中的状态名匹配。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/nimbus/State.java:61`：javadoc 示例 `public final class IndeterminateState extends State<JProgressBar>`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/nimbus/NimbusStyle.java` 大量使用 `State` 作为运行时状态集合元素（如 `RuntimeState`）。

## 4. 测试用例设计（黑盒部分，`StateTest.testBlackBox`）
1. 同名子类 `toString()` 等于构造名。
2. 不同名子类 `toString()` 不同。
3. 同名不同实例 `toString()` 相等但 `!=`（按值相等、按引用不等）。
