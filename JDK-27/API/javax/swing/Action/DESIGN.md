# javax.swing.Action 测试设计

## 1. API 功能
Action 接口（Swing 动作抽象）：11 个标准 key 常量（NAME/SHORT_DESCRIPTION/SMALL_ICON 等）、
enabled 状态、putValue/getValue 值袋 + PropertyChange 通知。

## 2. Java 用户如何使用
`JButton.setAction(a)` / `JMenuItem.setAction(a)` 绑定后由组件监听 enabled/"enabled" 事件联动；
程序侧自定义 AbstractAction 覆写 actionPerformed。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/Action.java`；
AbstractAction.java；JButton/JMenuItem 的 setAction 实现；JDK 自带 `test/jdk/javax/swing/` 相应模块用例。

## 4. 测试用例设计 + 易错细节
- key 常量字符串逐一精确断言（含内部 SwingDisplayedMnemonicIndexKey/SwingLargeIconKey/
  SwingSelectedKey），并断言 11 个常量互异。
- 契约经 AbstractAction 验证：enabled 往返、putValue 后 getValue、同值 putValue 不发事件、
  "enabled" 事件 old/new、putValue 以 key 本身为 propertyName、监听器移除后静默。
- **易错**：DISPLAYED_MNEMONIC_INDEX_KEY 等三个"Swing 前缀" key 是后来加的公共常量但值是内部字符串。

自动化实现位于同目录 `*Test.java`：`check(boolean, msg)` 计数断言、`checkThrows` 校验精确异常类型，
`main` 结尾打印 `PASS (N assertions)`；jtreg `@run main` 直接驱动（headless 安全，Metal LaF 先行）。

