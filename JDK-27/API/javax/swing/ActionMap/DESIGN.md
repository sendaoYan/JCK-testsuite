# javax.swing.ActionMap 测试设计

## 1. API 功能
Action key→Action 映射 + 父链：put/get/remove/keys/allKeys/size/clear/setParent，
可挂到 JComponent.getActionMap()。

## 2. Java 用户如何使用
组件输入映射的配套动作表：`comp.getActionMap().put("cut", action)`。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/ActionMap.java`；
InputMap.java；JComponent.getActionMap()；JDK 自带 `test/jdk/javax/swing/` 相应模块用例。

## 4. 测试用例设计 + 易错细节
- 空 map size 0；keys() 永不返回 null（返回空数组）——**与 InputMap.keys() 可返回 null 相反**。
- put(k,null) 等价 remove；remove 不存在 key 无操作返回 null。
- 两级父链：get 跨链、keys() 只含本级、allKeys 跨链、影子（子覆盖父）、clear 保留父、setParent(null) 断链。
- JPanel.getActionMap() 非空并可 put。
- **易错**：setParent(自身) 造成无限递归（StackOverflowError），不做此断言。

自动化实现位于同目录 `*Test.java`：`check(boolean, msg)` 计数断言、`checkThrows` 校验精确异常类型，
`main` 结尾打印 `PASS (N assertions)`；jtreg `@run main` 直接驱动（headless 安全，Metal LaF 先行）。

