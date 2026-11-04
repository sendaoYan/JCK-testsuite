# java.beans.EventHandler — 设计案

## 1. API 功能
事件处理器（implements InvocationHandler）。构造器 `(target, action, eventPropertyName, listenerMethodName)`；静态 `create` 返回动态代理，getAction/getTarget/getEventPropertyName/getListenerMethodName。

## 2. Java 用户如何使用
`EventHandler.create(ActionListener.class, target, "fire")` 把监听器事件转发到 target.fire。

## 3. 开源代码实际用法调研
JDK 源码 `EventHandler.java`；JavaBeans/IDE 绑定工具使用。

## 4. 测试用例设计 + 自动化实现
白盒：implements InvocationHandler。黑盒（`testBlackBox`）：构造器 getter 与 create 动态代理调用。

## 5. 深化覆盖（批次 beansF，2026-11，68 断言）
参考来源补全：Oracle JDK 27 javadoc；JavaBeans spec 1.01-A；OpenJDK `test/jdk/java/beans/EventHandler/{Test6179222,Test6788531}` 回归思路；对照 jdk28 源码核实（与 JDK-8 相同，仅去掉 doPrivileged）。

新增内容：全部改用自写嵌套接口 `Lsn{first(Evt),second(Evt)}` + 自写事件类（保留原 ActionListener 检查）。testWhiteBox（含 3 个静态 create 重载计数）、testCtorAndGetters、testCtorValidation（构造期 NPE 矩阵：target/action/listenerInterface；空 action 仅派发期失败）、testProxyNature（Proxy 类/InvocationHandler 身份/两代理同类不同实例）、testObjectMethods（equals/hashCode/toString）、testNullaryActionAllMethods（null listenerMethodName 全方法连线、void 返回 null）、testMethodFilter（非匹配方法 no-op）、testPropertyExtraction（get/is/裸名三种提取、"" 传事件本体、source.text 点路径、Boolean→boolean setter）、testActionForms（裸方法/显式 setter/属性名 set 回退/sub.text 限定/action 返回值透传）、testQualifiedActionMutatesHandler（限定 action 派发后 handler 字段被改写——记录性行为）、testPropertyChangeListenerIntegration（propertyName 提取）、testDispatchExceptions（全部 RuntimeException：未知属性/中间缺失/未知 action/空 action/null 事件/target 异常同实例透传）。

双侧异常类型一致，无版本差异断言。
