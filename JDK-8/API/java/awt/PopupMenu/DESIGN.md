# java.awt.PopupMenu 行为测试设计

## 1. API 功能
构造：`PopupMenu()`/`(String)`；显示：`show(Component,int,int)`；继承 Menu 的增删项。

## 2. Java 用户如何使用
```java
PopupMenu p = new PopupMenu("edit");
p.add(new MenuItem("cut"));
host.add(p);
p.show(host, 5, 5);
```
构造 → 挂到 host → 显示前置条件 → 显示路径。

## 3. 开源实际用法调研
来源（本地 JDK 源码与自带测试，web 未使用）：
`~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/PopupMenu.java`。

## 4. 测试用例设计 + 自动化实现
白盒：public/继承关系/方法面。黑盒：构造→属性往返→事件登记→负例矩阵（`testContractPopupMenu()`）。
对应 `PopupMenuTest.java`（本批新增 `testContract*()` 深化方法；断言计数见 PASS 输出）。

## 5. 契约深化（本轮追加）
- `show(origin, x, y)` 前置条件：origin 非 showing 时抛精确 RuntimeException（非 IllegalComponentStateException 子类）；显示 host 后 show 成功且项可枚举。
- ctor(String) label 保留（含 null）；继承 Menu 的全部增删契约。
