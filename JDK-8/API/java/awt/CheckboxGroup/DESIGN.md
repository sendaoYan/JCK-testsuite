# java.awt.CheckboxGroup 行为测试设计

## 1. API 功能
选择管理：`getSelectedCheckbox/setSelectedCheckbox` 及 `getCurrent/setCurrent` 别名对；Serializable；toString。

## 2. Java 用户如何使用
```java
CheckboxGroup g = new CheckboxGroup();
Checkbox a = new Checkbox("A", true, g);
g.setSelectedCheckbox(null);
```
构造 → true 成员入组 → 别名对迁移/清除。

## 3. 开源实际用法调研
来源（本地 JDK 源码与自带测试，web 未使用）：
`~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/CheckboxGroup.java`。

## 4. 测试用例设计 + 自动化实现
白盒：public/继承关系/方法面。黑盒：构造→属性往返→事件登记→负例矩阵（`testContractCheckboxGroup()`）。
对应 `CheckboxGroupTest.java`（本批新增 `testContract*()` 深化方法；断言计数见 PASS 输出）。

## 5. 契约深化（本轮追加）
- 初始无选择；构造为 true 的 Checkbox 加入组即成为 selection。
- `setCurrent`/`setSelectedCheckbox` 互为别名（两侧走查）；自赋值保持选择。
- `setSelectedCheckbox(null)` 取消成员的 state（不是仅清引用）。
- toString 精确格式 `java.awt.CheckboxGroup[selectedCheckbox=...]`，null 选择时输出 selectedCheckbox=null。
- implements Serializable：序列化往返后选择关系保留。
