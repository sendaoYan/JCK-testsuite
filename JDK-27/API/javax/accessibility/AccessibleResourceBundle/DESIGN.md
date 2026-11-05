# javax.accessibility.AccessibleResourceBundle 测试设计

## 1. API 功能
已废弃的 ListResourceBundle，保存无障碍 role/relation/state key 的本地化显示串。

按方法分组：
- 查询: getContents() -> Object[][]
- 继承: getKeys(), handleGetObject(Object), getString(String)

## 2. Java 用户如何使用
内部用于把 key 映射为显示串。测试遍历 getContents 校验 key/value 行，并验证已知 key 与未知 key 异常。

## 3. 开源代码实际用法调研
- JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/javax/accessibility/AccessibleResourceBundle.java
- JDK 测试: ~/git/jdk25u-dev/test/jdk/java/awt/a11y/

## 4. 测试用例设计 + 自动化实现
- 白盒: 具体类、继承 ListResourceBundle、@Deprecated、getContents
- 黑盒: getContents 行结构；getString 已知/未知 key；handleGetObject；getKeys 数量

自动化实现位于同目录 `AccessibleResourceBundleTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。

## 5. 契约深化（黑盒行为）
`testContractDeep()`（2026-11 契约深化）追加断言：
- @Deprecated（1.3 起保留兼容）；继承 ListResourceBundle；getContents() 返回 Object[][]
- 内容表：恰 80 个键值对；"pushbutton" → "push button"、"enabled" 存在（role/state 键空间）
- getKeys() 仍可枚举（bundle 功能性未随弃用移除）
