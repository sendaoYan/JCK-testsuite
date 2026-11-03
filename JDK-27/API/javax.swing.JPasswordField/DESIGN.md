# javax.swing.JPasswordField 黑盒行为测试设计

## 1. API 功能
JPasswordField 是密码输入控件，继承 JTextField（自身不新增接口）。

- 构造器：`JPasswordField()`、`JPasswordField(String)`、`JPasswordField(int)`、`JPasswordField(String, int)`、`JPasswordField(Document, String, int)`。
- 密码：`getPassword()`（返回 char[]）、`getText()`（继承，已弃用）、`getText(int, int)`。
- 回显字符：`getEchoChar()/setEchoChar(char)`、`echoCharIsSet()`；默认由 L&F 决定（Metal/Ocean 为 bullet），setEchoChar(0) 关闭回显。
- UI：`getUIClassID()` 返回 "PasswordFieldUI"。
- 继承 JTextField：`getColumns`、`setCaretPosition/getCaretPosition`、`getDocument` 等。

## 2. Java 用户如何使用
- `JPasswordField pf = new JPasswordField();` 输入后 `char[] pwd = pf.getPassword();` 校验后清零数组。
- `pf.setEchoChar('*')` 指定回显字符；`pf.setEchoChar(0)` 明文显示。
- `pf.echoCharIsSet()` 判断是否设置了回显。

## 3. 开源代码实际用法调研
JDK 自带测试（`~/git/jdk25u-dev/test/jdk`）：
- `test/jdk/javax/swing/plaf/basic/BasicTextUI/PasswordSelectionWordTest.java` 用 `new JPasswordField(str)` 并校验 getUI。
- `test/jdk/java/awt/a11y/AccessibleTextTest.java` 对 JPasswordField 做无障碍文本断言。
- `test/jdk/com/sun/java/swing/plaf/gtk/4928019/bug4928019.java` 覆盖密码回显行为。
真实项目（登录/改密界面）通用模式：`new JPasswordField(16)` + `getPassword()` 后 `Arrays.fill(pwd, '\0')`。以上来源为 `~/git/jdk25u-dev/test/jdk`。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`JPasswordFieldTest.testBlackBox()`）：
1. 默认值：空文本 / columns 0 / echoCharIsSet true（L&F 设置回显字符，不断言具体字符）/ getUIClassID / PlainDocument / 空密码。
2. 文本构造器：getText 与 getPassword 往返。
3. 列宽构造器语义、Document 构造器引用保留。
4. setText/getText + 光标链 + getText(off,len) 子串。
5. setEchoChar('$')/getEchoChar/echoCharIsSet 往返；setEchoChar(0) 关闭回显；恢复。
6. setColumns/getColumns 往返。
