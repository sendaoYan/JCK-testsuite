# javax.swing.text.BadLocationException — DESIGN

## 1. API 功能
`BadLocationException extends Exception`，用于报告文档模型中的非法位置（访问不存在的偏移）。方法分组：
- 构造器：`BadLocationException(String s, int offs)`，`s` 为描述信息，`offs` 为被请求的偏移。
- 查询：`offsetRequested()` 返回构造时传入的偏移（未做合法性校验，原样返回）。
- 继承：`getMessage()`（来自 Throwable）返回构造时的描述字符串。

## 2. Java 用户如何使用
`throw new BadLocationException("offset " + offs + " out of range", offs)`，捕获方通过 `e.getMessage()` 获得原因、`e.offsetRequested()` 获得具体非法偏移，用于日志/错误提示或回退到合法位置。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk8u/jdk/src/share/classes/javax/swing/text/AbstractDocument.java`：`getText(int, int)` / `getChars` 在校验 `where < 0 || end > length()` 后 `throw new BadLocationException("Invalid location", ...)`，返回值给 GapContent 的边界校验。
- `~/git/jdk8u/jdk/src/share/classes/javax/swing/plaf/basic/BasicHTML.java`：`modelToView` 中 `catch (BadLocationException e)` 作为 HTML 视图回退路径。
- `~/git/jdk8u/jdk/src/share/classes/javax/swing/text/JTextComponent.java`：`getText(int offs, int len)` 声明 `throws BadLocationException`。

## 4. 测试用例设计 + 自动化实现
`testBasic/testSerialization`（10 断言）：
- `BadLocationException(String,int)` 的 getMessage 精确回传入描述、offsetRequested() 精确回传偏移。
- 异常类型继承链：直接父类 java.lang.Exception。
- 序列化 round-trip：desc 与 offsetRequested 均精确保留（含负偏移）。

### 易错规范细节
- offsetRequested 是契约核心：各文档/Content 实现对同一非法操作报告的值不同，本套件在各用例中逐一锁定精确值。
