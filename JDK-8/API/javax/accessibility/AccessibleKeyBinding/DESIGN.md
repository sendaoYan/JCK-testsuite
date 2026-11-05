# javax.accessibility.AccessibleKeyBinding 测试设计

## 1. API 功能
表示对象的键盘绑定（助记符/快捷键）。

按方法分组：
- 查询: getAccessibleKeyBindingCount(), getAccessibleKeyBinding(int)

## 2. Java 用户如何使用
辅助技术读取对象的快捷键数量与每个绑定。测试用匿名实现返回固定绑定数组断言数量与元素。

## 3. 开源代码实际用法调研
- JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/javax/accessibility/AccessibleKeyBinding.java
- JDK 测试: ~/git/jdk25u-dev/test/jdk/java/awt/a11y/

## 4. 测试用例设计 + 自动化实现
- 白盒: 接口、两个方法
- 黑盒: 绑定数量/元素；空绑定

自动化实现位于同目录 `AccessibleKeyBindingTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。

## 5. 契约深化（黑盒行为）
`testContractDeep()`（2026-11 契约深化）追加断言：
- 绑定两种形态：Integer（助记码）与 javax.swing.KeyStroke；KeyStroke 须经工厂 getKeyStroke 获得（构造器不可用）
- 键绑定不经 AccessibleContext（无该方法），经 AccessibleExtendedComponent 暴露
- 真组件闭环：JButton 无助记码时 getAccessibleKeyBinding()=null；setMnemonic(VK_P) 后恰 1 个绑定且为 KeyStroke "pressed P"
