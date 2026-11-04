# java.beans.PropertyEditorManager — 设计案

## 1. API 功能
属性编辑器注册表。registerEditor/findEditor、get/setEditorSearchPath。

## 2. Java 用户如何使用
`PropertyEditorManager.registerEditor(MyType.class, MyEditor.class)`；`findEditor(type)` 获取编辑器。

## 3. 开源代码实际用法调研
JDK 源码 `PropertyEditorManager.java`；IDE 属性编辑器查找。

## 4. 测试用例设计 + 自动化实现
白盒：类。黑盒（`testBlackBox`）：register/find/unregister 及搜索路径 set/get。

## 5. 深化覆盖（批次 beansF，2026-11，68 断言）
参考来源补全：Oracle JDK 27 javadoc；JavaBeans spec 1.01-A 第 9.3 节；OpenJDK `test/jdk/java/beans/PropertyEditor/{Test7087876,Test6397609}` 回归思路；对照 jdk28 的 com/sun/beans/finder/{PropertyEditorFinder,InstanceFinder}.java 核实。

新增测试方法：testWhiteBox（恰 4 个 public 方法全 static）、testDefaultEditorMatrix（默认路径恰 {"sun.beans.editors"}、8 包装类非空、Character/void/Object null、int/boolean primitive 预置可用、wrapper vs primitive 独立实例）、testLegacyRegistrationAndSearchPath（保留旧检查后恢复全局状态并断言默认编辑器回归）、testFreshInstancePerFind、testRegisterLifecycle（注册→覆盖→清除→再注册，含每次新实例）、testPrimitiveRegistration（char.class 注册/清除）、testSearchPathSemantics（防御克隆双向、**setEditorSearchPath(null)→空数组而非默认**、恢复）、testEmptyPathHidesSearchPathDefaults（空路径隐藏搜索路径默认但保留注册表 primitive 与显式注册——两段式查找实证）、testEnumFallback（嵌套枚举 tags+往返）、testNullHandling（findEditor(null)→NPE；registerEditor(null,ed) 不抛且可查，finally 清除后再次 NPE）。

全局状态纪律：仅注册本文件独有类型，全部 finally/尾部清除并恢复默认搜索路径（与 JDK-8 侧同源）。JDK-27 与 JDK-8 行为一致（NPE 消息为 helpful message，不断言消息）。
