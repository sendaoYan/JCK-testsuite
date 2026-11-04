# java.beans.EventHandler — 设计案（JDK-8）

## 一、API 功能
`EventHandler implements InvocationHandler`：为监听器接口生成动态代理，把监听方法映射为对 target 的一个简单调用。
- 构造器 `(Object target, String action, String eventPropertyName, String listenerMethodName)`；getter 四件套；`invoke(proxy, method, args)`。
- 静态 `create(listenerInterface, target, action)` / `(…, eventPropertyName)` / `(…, eventPropertyName, listenerMethodName)` 返回 Proxy 实例。
- 语义：listenerMethodName 为 null 时接口**所有**方法都触发 action，否则只有同名方法触发（其余 no-op 返回 null）；eventPropertyName 为 null → 无参调用，为 "" → 传事件对象本身，否则按 `get<Prop>`/`is<Prop>`/裸方法名提取（支持 `a.b.c` 点路径，自左向右）；action 查找顺序：同名方法 → `set<Capitalized>`；action 支持 `a.b` 限定形式（先 target.getA() 再对结果施加 b）。

## 二、使用方式
`EventHandler.create(L.class, target, "setText", "source.text")` 把事件源属性推给 target setter；长持久化/IDE 连线的标准做法。

## 三、参考来源
- Oracle Java SE 8 javadoc：java.beans.EventHandler（点路径语法、"" 与 null 的 eventPropertyName 语义、RuntimeException at dispatch time 承诺）。
- JavaBeans 规范 1.01-A（事件监听器与设计时连线）。
- OpenJDK 回归思路：`test/jdk/java/beans/EventHandler/Test6179222.java`（set 前缀回退）、`Test6788531.java`；`XMLEncoder/java_beans_EventHandler.java`。测试代码全部自写。

## 四、测试用例设计 + 易错规范细节
测试方法与覆盖（68 断言），全部用**自写嵌套监听器接口 Lsn{first(Evt),second(Evt)}**（保留原有一条 java.awt.event.ActionListener 仅作既有检查，纯接口引用、headless 安全）：
1. **testWhiteBox**：implements InvocationHandler、公开方法集合、3 个静态 create 重载。
2. **testCtorAndGetters**：四 getter 恒等（含原有 StringBuilder 样例与 null 默认值）。
3. **testCtorValidation**（易错）：**构造即校验** —— null target/action → NPE；create 的 null listenerInterface/target/action → NPE；空串 action 可创建、仅派发时失败。
4. **testProxyNature**：Proxy.isProxyClass、实现接口、getInvocationHandler 为该 EventHandler、两次 create 代理对象不同但代理类相同（原有 ActionListener 触发检查保留）。
5. **testObjectMethods**：代理的 equals/hashCode(==identityHashCode)/toString 由 handler 处理。
6. **testNullaryActionAllMethods**：listenerMethodName=null 时 first/second 都触发（计数 2）；void action → 代理方法返回 null。
7. **testMethodFilter**：仅 "second" 连线；first 无副作用且返回 null；second 可重复触发。
8. **testPropertyExtraction**（易错）：`"name"`→getProp；`"getName"` 裸方法名等价；`"tag"`；`""`→事件对象本身（recordEvent 收到 Evt）；`"source.text"` 点路径自左向右；`"source.enabled"` 走 is 前缀；提取 Boolean → boolean setter。
9. **testActionForms**：裸方法名 action（label）；显式 setter 名（setLabel）；**属性名 action + 提取实参 → set<Capitalized> 回退**（仅有 setLabel 的 target）；限定 action `sub.text`（target.getSub().setText）；action 有返回值 → 代理方法原样返回。
10. **testQualifiedActionMutatesHandler**（实证实现细节）：直接调 invoke() 与代理等价；**限定 action 派发后 handler 的 target/action 字段被就地改写**（action 缩为末段、target 变为中间对象）——双侧一致，作为记录性断言。
11. **testPropertyChangeListenerIntegration**：java.beans 自带 PropertyChangeListener + `"propertyName"` 提取（action 收到 "theProp"）；null 属性 → 无参 action 对每个事件触发。
12. **testDispatchExceptions**：全部 **RuntimeException**（不包装）：未知事件属性；点路径中间属性缺失；未知 action；空 action；**null 事件 + 非空属性**（"Failed to call method: name on null"）；target action 抛出的 RuntimeException **同实例透传**。

### 易错规范细节（实证记录）
- NPE 矩阵（构造期）：null target / null action / null listenerInterface（双侧一致，消息不同不断言）。
- 派发期异常统一 RuntimeException（javadoc 承诺），类型与双侧实现一致。
- action 优先裸方法名、再 set 前缀；eventPropertyName 优先 get 前缀、再 is 前缀、再裸名。
- getAction/getTarget 在限定 action 派发后会被改写——非规范承诺但 JDK-8/JDK-27 实现一致，已记录。
