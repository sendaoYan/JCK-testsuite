# java.awt.AWTEventMulticaster 行为测试设计

## 1. API 功能
事件监听器多播器，把多个同类型监听器链成树，一次事件分派到达所有监听器；提供 17 组静态 add/remove 与泛型 getListeners，实现所有 AWT 监听器接口。

## 2. Java 用户如何使用
典型用法：`ComponentListener chain = AWTEventMulticaster.add(a, b);` 然后向组件注册 chain，事件触发时 a、b 都收到回调；`AWTEventMulticaster.remove(chain, a)` 返回只剩 b 的链。

## 3. 开源实际用法调研
来源（本地 JDK 源码与自带测试，web 未使用）：
`~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/AWTEventMulticaster.java`（addInternal/removeInternal/树平衡/getListeners 类型过滤）；`Component.java` 用其实现 addXxxListener 的多播。

## 4. 测试用例设计 + 自动化实现
白盒：public、实现 ComponentListener/ActionListener 等、protected 构造器、add/remove 静态。黑盒：add/remove 的 null 与恒等规则；链式分派到达两/三个监听器；remove 拆分；getListeners 计数与按类型过滤；两接口哑对象经不同接口注册与分派。
对应 `AWTEventMulticasterTest.java` 的 `testWhiteBox()` / `testBlackBox()`。
