# javax.swing.InputMap 测试设计

## 1. API 功能
KeyStroke→action-name 绑定表 + 父链；ComponentInputMap 变体要求父链同组件。

## 2. Java 用户如何使用
`comp.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ctrl X"), "cut")`。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/InputMap.java`；
ComponentInputMap.java；JComponent.getInputMap()；JDK 自带 `test/jdk/javax/swing/` 相应模块用例。

## 4. 测试用例设计 + 易错细节
- 从未 put 过的 map：keys()/allKeys() 返回 null；put 后再全部 remove 则 keys() 变为空数组（非 null）。
- put(ks,null) 移除绑定；get(null) 返回 null。
- 父链：跨链 get、own keys、allKeys 并集、影子、clear 保留父。
- ComponentInputMap：getParent 要求同组件的 ComponentInputMap，否则 IllegalArgumentException
  （普通 InputMap 作父同样拒绝）；**易错**：子绑定对同 key 覆盖父绑定（验证用不同 key 走父链）。

自动化实现位于同目录 `*Test.java`：`check(boolean, msg)` 计数断言、`checkThrows` 校验精确异常类型，
`main` 结尾打印 `PASS (N assertions)`；jtreg `@run main` 直接驱动（headless 安全，Metal LaF 先行）。

