# java.awt.TextComponent 测试设计

## 1. API 功能
文本组件抽象基类，提供文本选择/编辑 API，继承 Component。

## 2. Java 用户如何使用
构造器包私有；子类 TextField/TextArea 需图形环境。getText/setText/select/getCaretPosition 等。

## 3. 开源代码实际用法调研
JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/TextComponent.java

## 4. 测试用例设计 + 自动化实现
白盒: 继承 Component、implements Accessible、声明文本 API；黑盒降级: 包私有构造且 Component 构造抛 HeadlessException。

自动化实现位于同目录 `TextComponentTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。

## ⑤ 契约深化（本轮追加）
- `setText(null)` 归一化为 ""（TextArea/TextField 双验证，与 setLabel(null) 保留 null 的控件族形成对照）。
- editable 默认 true，setEditable(false) 可逆。
- selectAll 后 getSelectedText 全文、(0,len)；零宽 select(3,3) 得空选择且 caret=3。
- TextListener 登记/移除对称；程序化编辑（setText/insert）不派发 TextEvent。
