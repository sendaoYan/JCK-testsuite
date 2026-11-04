# javax.swing.plaf.basic.ComboPopup — DESIGN

## 1. API 功能
`ComboPopup` 接口（组合框下拉弹窗契约）：`show`/`hide`/`isVisible`、`getList`、三个监听器 getter、`uninstallingUI`。

## 2. Java 用户如何使用
由 `BasicComboPopup` 实现；测试用匿名实现验证 show/hide/isVisible/getList/uninstallingUI 契约。

## 3. 开源代码实际用法调研（来源：JDK 源码 jdk25u-dev + test/jdk）
来源：JDK 源码 `src/java.desktop/share/classes/javax/swing/plaf/basic/ComboPopup.java`；`BasicComboBoxUI.createPopup()` 返回实现类。

## 4. 测试用例设计 + 自动化实现（`ComboPopupTest.testBlackBox`）
匿名实现 → show/hide 切换 isVisible → getList 返回注入列表 → uninstallingUI 隐藏。
