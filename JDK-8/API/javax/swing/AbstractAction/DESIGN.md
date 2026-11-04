# javax.swing.AbstractAction 测试设计

## 1. API 功能
Action 的默认实现：字段袋（ArrayTable）+ enabled + 绑定属性监听，支持 clone（protected）与序列化。

## 2. Java 用户如何使用
`class MyAction extends AbstractAction { public void actionPerformed(...) }`；
构造时给 name/icon；putValue 挂任意 key。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/AbstractAction.java`；
Action.java；JDK 自带 `test/jdk/javax/swing/` 相应模块用例。

## 4. 测试用例设计 + 易错细节
- 三种构造器（无参/名称/名称+icon，SMALL_ICON 存同实例）。
- getKeys 含构造器写入的 NAME：构造+2 put 后 length==3（**易错**：NAME 会被构造器自动写入）。
- getKeys 空时返回 null；put 覆盖旧值。
- 属性事件矩阵："enabled" old/new、同值静默、putValue 以 key 触发、null 值触发。
- clone 经子类暴露 protected clone()：值袋与 enabled 完全独立（改原不影响克隆）。
- 序列化 round-trip：name 保留。

自动化实现位于同目录 `*Test.java`：`check(boolean, msg)` 计数断言、`checkThrows` 校验精确异常类型，
`main` 结尾打印 `PASS (N assertions)`；jtreg `@run main` 直接驱动（headless 安全，Metal LaF 先行）。

