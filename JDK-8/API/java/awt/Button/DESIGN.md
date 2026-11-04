# java.awt.Button 行为测试设计

## 1. API 功能
构造：`Button()`/`Button(String)`；标签与命令：`getLabel/setLabel`、`getActionCommand/setActionCommand`；事件：`addActionListener/removeActionListener/getActionListeners`；继承 Component 的 setEnabled/setBackground/setBounds。

## 2. Java 用户如何使用
```java
Button b = new Button("OK");
b.setActionCommand("ok");
b.addActionListener(e -> {});
panel.add(b);
```
构造 → 设标签/命令 → 加监听器 → add 到显示 Frame → 断言。

## 3. 开源实际用法调研
来源（本地 JDK 源码与自带测试，web 未使用）：
`~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/Button.java`。

## 4. 测试用例设计 + 自动化实现
白盒：public/继承关系/方法面。黑盒：构造→属性往返→事件登记→负例矩阵（`testContractButton()`）。
对应 `ButtonTest.java`（本批新增 `testContract*()` 深化方法；断言计数见 PASS 输出）。

## 5. 契约深化（本轮追加）
- `Button()` 默认 label 是空串（非 null）；`Button(null)` 与 `setLabel(null)` 原样保留 null，不归一化。
- actionCommand 三段回退语义：默认回退 label、显式 setActionCommand 优先、`setActionCommand(null)` 重新回退 label（实测三方锁定的回退链）。
- 监听器登记语义：同一 listener 注册两次计数为 2（不去重）；`addActionListener(null)`/`removeActionListener(null)` 均 no-op；`getListeners(ActionListener.class)` 与 getActionListeners 一致。
- 程序化 `setLabel`/`setEnabled` 不派发 ActionEvent（peer 前，EventQueue 排空后计数为 0）。
