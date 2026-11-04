# javax.swing.MenuSelectionManager 测试设计

## 1. API 功能
菜单选中路径状态机（defaultManager 单例）：setSelectedPath/getSelectedPath/clearSelectedPath、
ChangeEvent、菜单元素通知回调。

## 2. Java 用户如何使用
BasicMenuUI 在鼠标/键盘事件里驱动；自定义 MenuElement 接收回调。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/MenuSelectionManager.java`；
MenuElement.java；BasicMenuUI.java；JDK 自带 `test/jdk/javax/swing/` 相应模块用例。

## 4. 测试用例设计 + 易错细节
- defaultManager() 跨调用同一实例；初始 path 为空数组。
- 自定义 MenuElement 记录回调：进入 path 收 menuSelectionChanged(true)、清空收 (false)；
  setSelectedPath 内容保序；null 即清空。
- 每次变更触发 ChangeEvent；getChangeListeners 增删对称。
- isComponentPartOfCurrentMenu(null)/无关组件 false。
- EDT 纪律：路径操作用 invokeAndWait 包裹。

自动化实现位于同目录 `*Test.java`：`check(boolean, msg)` 计数断言、`checkThrows` 校验精确异常类型，
`main` 结尾打印 `PASS (N assertions)`；jtreg `@run main` 直接驱动（headless 安全，Metal LaF 先行）。

