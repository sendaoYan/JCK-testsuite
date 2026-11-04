# javax.swing.text.Caret — DESIGN

## 1. API 功能
文本插入点（光标）抽象接口。一个 Caret 维护两个文档位置：`dot`（当前光标）与 `mark`（选区另一端），无选区时二者相等。方法分组：
- 生命周期：`install(JTextComponent)` / `deinstall(JTextComponent)`。
- 位置查询/设置：`getDot()` / `getMark()` / `setDot(int)` / `moveDot(int)`。
- 可见性：`isVisible()` / `setVisible(boolean)` / `isSelectionVisible()` / `setSelectionVisible(boolean)`。
- 闪烁：`setBlinkRate(int)` / `getBlinkRate()`（毫秒，0 表示不闪烁）。
- 视觉位置记忆：`setMagicCaretPosition(Point)` / `getMagicCaretPosition()`。
- 渲染与监听：`paint(Graphics)` / `addChangeListener` / `removeChangeListener`。

## 2. Java 用户如何使用
`new DefaultCaret()`（接口的默认实现）→ `install(textArea)` 绑定到组件 → `setDot(n)` 定位（mark 与 dot 同步）→ `moveDot(m)` 形成选区（mark 留在原地）→ 读取 `getDot()/getMark()`；`setBlinkRate(500)` 控制光标闪烁；`setVisible(true)` 显示；`setMagicCaretPosition(p)` 记忆上下移动的列位置；`addChangeListener` 监听位置变化。

## 3. 开源代码实际用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/test/jdk/javax/swing/text/DefaultCaret/SetCaretRateTest.java`：`Caret caret = new DefaultCaret(); caret.setBlinkRate(0); caret.setBlinkRate(100); caret.setBlinkRate(0);` —— 验证未绑定组件的 Caret 设置闪烁速率不抛异常。
- `~/git/jdk25u-dev/test/jdk/javax/swing/text/DefaultCaret/7083457/bug7083457.java`：`DefaultCaret caret = new DefaultCaret(); ... caret.setVisible(active); if (caret.isActive() != active) { ... }` —— setVisible 与 isActive 的关系。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/ParagraphView.java`：`magicPoint = (c != null) ? c.getMagicCaretPosition() : null;` —— 视图用 magicCaretPosition 记忆上下移动的视觉列位置。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/DefaultCaret.java`：`install()` 里 `dot = mark = 0; dotBias = markBias = Position.Bias.Forward;` —— 安装时重置位置。

## 4. 测试用例设计（黑盒部分，`CaretTest.testBlackBox`）
1. install 后 dot/mark 初始为 0；setDot 同时移动 dot 与 mark；moveDot 只移动 dot 留下 mark（选区）。
2. setDot 越界钳制：负值→0、超过文档长度→文档末尾。
3. 闪烁速率默认 0、set/get 往返、恢复 0。
4. magicCaretPosition 默认 null、set/get 往返。
5. 可见性/选区可见性默认 false、set/get 往返。
6. ChangeListener 在 setDot 改变位置时触发；removeChangeListener 后不再触发。
7. deinstall 正常完成。
