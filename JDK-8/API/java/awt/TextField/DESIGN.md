# java.awt.TextField 行为测试设计

## 1. API 功能
构造：`TextField()`/`(String)`/`(int columns)`/`(String,int)`；文本与列：`getText/setText`、`getColumns/setColumns`；回显：`setEchoChar/getEchoChar/echoCharIsSet`；选择/caret/editable 继承；ActionListener；`getPreferredSize(int)`。

## 2. Java 用户如何使用
```java
TextField tf = new TextField("abcde");
tf.setEchoChar('*');
tf.addActionListener(e -> {});
```
构造 → 列宽推导 → 回显语义 → 监听器。

## 3. 开源实际用法调研
来源（本地 JDK 源码与自带测试，web 未使用）：
`~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/TextField.java`。

## 4. 测试用例设计 + 自动化实现
白盒：public/继承关系/方法面。黑盒：构造→属性往返→事件登记→负例矩阵（`testContractTextField()`）。
对应 `TextFieldTest.java`（本批新增 `testContract*()` 深化方法；断言计数见 PASS 输出）。

## 5. 契约深化（本轮追加）
- 列宽推导：`TextField("abcde").getColumns()==5`（默认列数取文本长度）；`TextField()`/`TextField(null)` 为 0 与空文本。
- `setColumns(0)` 允许（负数 IAE）；setColumns 往返。
- 回显语义：默认 echoChar=0 且 echoCharIsSet==false；`setEchoChar('x')` 后为真；**NUL echoChar（'\0'）取消回显**（echoCharIsSet 回 false）——清回显的合法路径。
- `setText(null)` 归一化为空串；getSelectedText/getText 不受回显字符影响（回显纯显示层）。
- 同一 ActionListener 重复注册计数 2；getListeners 与显式 getter 一致。
