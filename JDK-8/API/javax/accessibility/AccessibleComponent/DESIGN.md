# javax.accessibility.AccessibleComponent 测试设计

## 1. API 功能
描述具有图形表现的对象：颜色/光标/字体/可见性/位置/大小/焦点等。

按方法分组：
- 颜色: get/setBackground, get/setForeground
- 字体/光标: get/setFont, get/setCursor, getFontMetrics
- 状态: is/setEnabled, is/setVisible, isShowing
- 几何: contains, get/setLocation, get/setBounds, get/setSize, getLocationOnScreen
- 焦点: isFocusTraversable, requestFocus, add/removeFocusListener

## 2. Java 用户如何使用
辅助技术通过 getBounds/getLocation 定位组件、isShowing 判断是否可见、requestFocus 转移焦点。测试用完整匿名实现做 set/get 回读与几何 contains 断言。

## 3. 开源代码实际用法调研
- JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JComponent.java (AccessibleJComponent)
- JDK 测试: ~/git/jdk25u-dev/test/jdk/java/awt/a11y/

## 4. 测试用例设计 + 自动化实现
- 白盒: 接口、无父接口、关键 getter/setter
- 黑盒: set/get 颜色/字体/光标回读；enabled/visible 开关；location/bounds/size 回读；contains 内外点；requestFocus

自动化实现位于同目录 `AccessibleComponentTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。

## 5. 契约深化（黑盒行为）
`testContractDeep()`（2026-11 契约深化）追加断言：
- 记录型子类 RecComp 全表面：颜色/光标/几何（getLocation x=1、getSize 10x10）/isShowing false/contains(Point)/焦点遍历
- `contains` 签名为 `contains(java.awt.Point)`（非 int,int）
- 真组件对照：JLabel 暴露 AccessibleComponent；裸 AccessibleContext 默认 `getAccessibleComponent() == null`
