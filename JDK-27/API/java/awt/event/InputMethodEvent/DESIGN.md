# java.awt.event.InputMethodEvent 行为测试设计

## 1. API 功能
输入法事件：INPUT_METHOD_TEXT_CHANGED=1100/CARET_POSITION_CHANGED=1101，带 AttributedCharacterIterator text、committedCharacterCount、caret/visiblePosition（TextHitInfo），可 consume，带 when。

## 2. Java 用户如何使用
`new InputMethodEvent(component, INPUT_METHOD_TEXT_CHANGED, when, text, committed, caret, visiblePosition)`；InputMethodListener 读 getText/getCommittedCharacterCount/getCaret。

## 3. 开源实际用法调研
来源（本地 JDK 源码与自带测试，web 未使用）：
`~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/event/InputMethodEvent.java`（构造器校验、paramString）。

## 4. 测试用例设计 + 自动化实现
白盒：public、继承 AWTEvent、常量/构造器。黑盒：构造→回读 id/when/text/committed/caret/visiblePosition；consume/isConsumed；CARET_POSITION_CHANGED 需 null text；4 参默认 when；4 个常量精确值；非法 id/caret 带 text/count 越界抛 IAE；paramString。
对应 `InputMethodEventTest.java` 的 `testWhiteBox()` / `testBlackBox()`。

## 5. 契约深化（本轮追加）
- 参数校验矩阵：id 越界 IAE；CARET_POSITION_CHANGED 带 text IAE；committedCharacterCount 负数/超长 IAE；count==iterator 长度为合法边界；4 参构造器同样校验 id。
- 消费语义：可 consume 且生效。
- 序列化：仅 id/when 保留；caret/visiblePosition/committedCharacterCount/text 均 transient（round-trip 后为 null/0）——锁定该行为。
