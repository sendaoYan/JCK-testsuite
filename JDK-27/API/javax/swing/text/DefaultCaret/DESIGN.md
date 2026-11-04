# javax.swing.text.DefaultCaret — DESIGN

## 1. API 功能
Caret 的默认实现（`extends java.awt.Rectangle`，implements Caret/FocusListener/MouseListener/MouseMotionListener）。方法分组：
- 构造器：`DefaultCaret()`。
- 文档更新策略：`setUpdatePolicy(int)/getUpdatePolicy()`，常量 `UPDATE_WHEN_ON_EDT=0`、`NEVER_UPDATE=1`、`ALWAYS_UPDATE=2`。
- 位置：`setDot(int)/setDot(int,Bias)/moveDot(int)/moveDot(int,Bias)/getDot()/getMark()/getDotBias()/getMarkBias()`。
- 闪烁：`setBlinkRate(int)/getBlinkRate()`（负值抛 `IllegalArgumentException`）。
- 可见性：`setVisible/isVisible/isActive/setSelectionVisible/isSelectionVisible`。
- 视觉位置：`setMagicCaretPosition/getMagicCaretPosition`。
- 其他：`install/deinstall`、`addChangeListener/removeChangeListener/getChangeListeners`、`equals`（对象同一性）、`toString`（"Dot=(..) Mark=(..)"）。

## 2. Java 用户如何使用
`new DefaultCaret()` → `setUpdatePolicy(ALWAYS_UPDATE)` 控制文档变更时光标是否跟随 → `install(area)` → `setDot(4)` / `moveDot(6)` 定位与选区 → `setBlinkRate(500)` 设置闪烁 → `setVisible(true)` 显示 → `setSelectionVisible(true)` 显示选区 → `getDotBias()/getMarkBias()` 查询 Bidi 偏置。

## 3. 开源代码实际用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/test/jdk/javax/swing/text/DefaultCaret/7083457/bug7083457.java`：`DefaultCaret caret = new DefaultCaret(); caret.setVisible(active); if (caret.isActive() != active) throw ...` —— 未绑定组件时 setVisible 只影响 isActive。
- `~/git/jdk25u-dev/test/jdk/javax/swing/text/DefaultCaret/SetCaretRateTest.java`：连续 `caret.setBlinkRate(0/100/0)` 验证未绑定组件的闪烁速率路径（JDK-8301989）。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/DefaultCaret.java`：`handleSetDot` 中 `dot = Math.min(dot, doc.getLength()); dot = Math.max(dot, 0);` —— setDot 将位置钳制到 `[0, docLength]`；`setBlinkRate` 中 `if (rate < 0) throw new IllegalArgumentException(...)`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/DefaultCaret.java`：`equals` 改为 `return (this == obj);`、`toString` 输出 `"Dot=(" + dot + ", " + dotBias + ") Mark=(...)"`。

## 4. 测试用例设计（黑盒部分，`DefaultCaretTest.testBlackBox`）
1. updatePolicy 默认 EDT、三个常量往返。
2. 未绑定组件：setBlinkRate 往返、负值 IAE、equals 同一性、setVisible/isActive 关系。
3. install 后：dot/mark/bias 初始为 Forward、0。
4. setDot/moveDot + Bias 变体：getDot/getMark/getDotBias/getMarkBias；null bias 抛 IAE。
5. setDot 越界钳制（负值→0、超长→文档长度）。
6. toString 含 "Dot="/"Mark="；magicCaretPosition 往返。
7. 绑定组件后 setVisible/isVisible、setSelectionVisible/isSelectionVisible 往返；installed setBlinkRate 往返并复位。
