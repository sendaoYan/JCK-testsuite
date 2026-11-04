# javax.accessibility.AccessibleIcon 测试设计

## 1. API 功能
表示对象关联图标：图标描述与宽高。

按方法分组：
- 查询: getAccessibleIconDescription(), getAccessibleIconWidth(), getAccessibleIconHeight()
- 设置: setAccessibleIconDescription(String)

## 2. Java 用户如何使用
按钮等组件通过图标接口向辅助技术暴露图标描述。测试用匿名实现做描述 set/get 回读与宽高只读。

## 3. 开源代码实际用法调研
- JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/ImageIcon.java
- JDK 测试: ~/git/jdk25u-dev/test/jdk/java/awt/a11y/

## 4. 测试用例设计 + 自动化实现
- 白盒: 接口、四个方法
- 黑盒: 描述 set/get 回读；宽高只读

自动化实现位于同目录 `AccessibleIconTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。
