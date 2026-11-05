# javax.accessibility.AccessibleExtendedComponent 测试设计

## 1. API 功能
扩展 AccessibleComponent，增加 tooltip 文本、带边框标题、键绑定的只读访问。

按方法分组：
- 查询: getToolTipText(), getTitledBorderText(), getAccessibleKeyBinding()
- 继承: AccessibleComponent 全部方法

## 2. Java 用户如何使用
辅助技术读取组件的 tooltip/title 与快捷键。测试用匿名实现返回固定值并验证继承的 set/get。

## 3. 开源代码实际用法调研
- JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JComponent.java (AccessibleJComponent 实现)
- JDK 测试: ~/git/jdk25u-dev/test/jdk/java/awt/a11y/

## 4. 测试用例设计 + 自动化实现
- 白盒: 接口、继承 AccessibleComponent、三个新方法
- 黑盒: tooltip/title/keybinding 返回；继承 set/get

自动化实现位于同目录 `AccessibleExtendedComponentTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。

## 5. 契约深化（黑盒行为）
`testContractDeep()`（2026-11 契约深化）追加断言：
- 继承 AccessibleComponent；三自有方法 getToolTipText/getTitledBorderText/getAccessibleKeyBinding
- 无 getAccessibleKeyBindingCount（该能力在 AccessibleKeyBinding 上）
- 真组件对照：JButton 的 AccessibleComponent 实为 AccessibleExtendedComponent，setToolTipText("help") 后经 getToolTipText 读回；JLabel titledBorder 默认 null
