# javax.accessibility.AccessibleTableModelChange 测试设计

## 1. API 功能
描述表格模型变更的类型与受影响行列区间。

按方法分组：
- 常量: INSERT=1, UPDATE=0, DELETE=-1
- 查询: getType/getFirstRow/getLastRow/getFirstColumn/getLastColumn

## 2. Java 用户如何使用
表格模型变更事件携带该接口描述变更。测试断言常量精确值与匿名实现返回值。

## 3. 开源代码实际用法调研
- JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/javax/accessibility/AccessibleTableModelChange.java
- JDK 测试: ~/git/jdk25u-dev/test/jdk/java/awt/a11y/

## 4. 测试用例设计 + 自动化实现
- 白盒: 接口、五个方法
- 黑盒: INSERT/UPDATE/DELETE 常量；匿名实现 getter 组合

自动化实现位于同目录 `AccessibleTableModelChangeTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。
