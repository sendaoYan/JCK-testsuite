# java.awt.TextComponent 行为测试设计

## 1. API 功能
基类文本能力：`getText/setText`、`getSelectedText/select/setSelectionStart/setSelectionEnd`、`getCaretPosition/setCaretPosition`、`isEditable/setEditable`、TextListener 增删查。包私有构造（白盒反射覆盖）。

## 2. Java 用户如何使用
```java
TextArea ta = new TextArea();
ta.setText("hello");
ta.selectAll();
ta.getSelectedText();
```
以 TextArea/TextField 两个具体子类驱动基类契约。

## 3. 开源实际用法调研
来源（本地 JDK 源码与自带测试，web 未使用）：
`~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/TextComponent.java`。

## 4. 测试用例设计 + 自动化实现
白盒：public/继承关系/方法面。黑盒：构造→属性往返→事件登记→负例矩阵（`testContractTextComponent()`）。
对应 `TextComponentTest.java`（本批新增 `testContract*()` 深化方法；断言计数见 PASS 输出）。

## 5. 契约深化（本轮追加）
- `setText(null)` 归一化为 ""（TextArea/TextField 双验证，与 setLabel(null) 保留 null 的控件族形成对照）。
- editable 默认 true，setEditable(false) 可逆。
- selectAll 后 getSelectedText 全文、(0,len)；零宽 select(3,3) 得空选择且 caret=3。
- TextListener 登记/移除对称；程序化编辑（setText/insert）不派发 TextEvent。
