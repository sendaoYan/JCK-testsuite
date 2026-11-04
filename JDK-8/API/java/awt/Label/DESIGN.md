# java.awt.Label 行为测试设计

## 1. API 功能
构造：`Label()`/`(String)`/`(String,int)`；常量 LEFT=0/CENTER=1/RIGHT=2；文本与对齐：`getText/setText`、`getAlignment/setAlignment`。

## 2. Java 用户如何使用
```java
Label l = new Label("hi", Label.CENTER);
l.setText("changed");
```
构造 → 常量 → 属性往返 → 越界反例。

## 3. 开源实际用法调研
来源（本地 JDK 源码与自带测试，web 未使用）：
`~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/Label.java`。

## 4. 测试用例设计 + 自动化实现
白盒：public/继承关系/方法面。黑盒：构造→属性往返→事件登记→负例矩阵（`testContractLabel()`）。
对应 `LabelTest.java`（本批新增 `testContract*()` 深化方法；断言计数见 PASS 输出）。

## 5. 契约深化（本轮追加）
- 常量精确值 LEFT 0/CENTER 1/RIGHT 2。
- `Label(null)`、`Label(null,alignment)`、`setText(null)` 均保留 null（不归一化为空串）。
- `setAlignment(99)`/`setAlignment(-1)` 与 `new Label(t,9)`/`(t,-1)` 均抛 IAE（improper alignment）；失败的 set 后原值保持。
