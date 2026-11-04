# java.awt.MenuComponent 行为测试设计

## 1. API 功能
基类能力：`getName/setName`（自动生成名）、`getParent`、`getFont/setFont`、`removeNotify`、`postEvent/dispatchEvent`、`toString`、Serializable；JDK 8 还有 deprecated `getPeer()`。

## 2. Java 用户如何使用
```java
MenuItem mi = new MenuItem("x");
mi.setName("m1");
Menu m = new Menu("F"); m.add(mi);
```
抽象基类 → 名字/字体/父链 → 版本差异面。

## 3. 开源实际用法调研
来源（本地 JDK 源码与自带测试，web 未使用）：
`~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/MenuComponent.java`。

## 4. 测试用例设计 + 自动化实现
白盒：public/继承关系/方法面。黑盒：构造→属性往返→事件登记→负例矩阵（`testContractMenuComponent()`）。
对应 `MenuComponentTest.java`（本批新增 `testContract*()` 深化方法；断言计数见 PASS 输出）。

## 5. 契约深化（本轮追加）
- 无 parent 初始为 null；`Menu.add` wire 子项 parent 链接；remove 后 JDK 9+ 解除、JDK 8 保留 stale 链接（版本差异，双侧分别断言）。
- 未显式 setName 时自动生成名非 null；setName/getName 往返。
- `setFont(null)` 后 `getFont()` 返回 null——peer 前没有默认字体回退（实测修正了"保留默认字体"的直觉）。
- deprecated `getPeer()`：JDK 8 无 peer 时返回 null；JDK 9+ 方法已删除（反射锁定存在性差异）。
