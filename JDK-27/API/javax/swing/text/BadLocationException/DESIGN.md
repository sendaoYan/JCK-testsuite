# javax.swing.text.BadLocationException — DESIGN

## 1. API 功能
`BadLocationException extends Exception`，用于报告文档模型中的非法位置（访问不存在的偏移）。方法分组：
- 构造器：`BadLocationException(String s, int offs)`，`s` 为描述信息，`offs` 为被请求的偏移。
- 查询：`offsetRequested()` 返回构造时传入的偏移（未做合法性校验，原样返回）。
- 继承：`getMessage()`（来自 Throwable）返回构造时的描述字符串。

## 2. Java 用户如何使用
`throw new BadLocationException("offset " + offs + " out of range", offs)`，捕获方通过 `e.getMessage()` 获得原因、`e.offsetRequested()` 获得具体非法偏移，用于日志/错误提示或回退到合法位置。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/AbstractDocument.java`：`getText(int, int)` / `getChars` 在校验 `where < 0 || end > length()` 后 `throw new BadLocationException("Invalid location", ...)`，返回值给 GapContent 的边界校验。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/basic/BasicHTML.java`：`modelToView` 中 `catch (BadLocationException e)` 作为 HTML 视图回退路径。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/JTextComponent.java`：`getText(int offs, int len)` 声明 `throws BadLocationException`。

## 4. 测试用例设计（黑盒部分，`BadLocationExceptionTest.testBlackBox`）
1. 构造 + `getMessage()`/`offsetRequested()` 精确往返（message="offset out of range"，offset=42）。
2. 类型链路：instanceof Exception / Throwable，且非 RuntimeException（受检异常）。
3. 边界：空串 message、offset=0、null message、Integer.MAX_VALUE、负 offset 均原样保留（构造器不校验）。
