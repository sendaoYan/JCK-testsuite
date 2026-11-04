# javax.naming.spi.NamingManager — 测试设计

## 1. API 功能
- 静态工具类，命名对象/上下文工厂调度核心。
- 常量：`CPE = "java.naming.spi.CannotProceedException"`。
- 对象工厂：`getObjectInstance(Object, Name, Context, Hashtable)`、`getStateToBind(Object, Name, Context, Hashtable)`、`setObjectFactoryBuilder(...)`。
- 初始上下文：`getInitialContext(Hashtable)`、`setInitialContextFactoryBuilder(...)`、`hasInitialContextFactoryBuilder()`。
- 其他：`getURLContext`、`getContinuationContext`。

## 2. Java 用户如何使用
- 提供者与 `InitialContext`/`Context` 使用 `NamingManager.getObjectInstance` 还原对象、`getStateToBind` 转换状态、`getInitialContext` 获取初始上下文。
- 无工厂时 `getObjectInstance` 返回原对象、`getStateToBind` 返回原对象、无工厂类名时 `getInitialContext` 抛 `NoInitialContextException`。
- 组合链路：`getObjectInstance(ref, name, ctx, env)` → 对象；`getInitialContext(env)` → `Context` 或异常。

## 3. 开源代码实际用法调研
来源：`~/git/jdk25u-dev`。
- `javax/naming/InitialContext.java` 通过 `NamingManager.getInitialContext` 获取初始上下文。
- `com/sun/jndi/toolkit/url/GenericURLContext.java` 使用 `NamingManager.getContinuationContext`。
- `javax/naming/Reference.java`、`Context.java` 文档描述对象工厂调度规则。

## 4. 测试用例设计
黑盒断言（`NamingManagerTest.testBlackBox`）：
- `CPE` 常量精确值。
- 默认 `hasInitialContextFactoryBuilder()==false`。
- `getObjectInstance` 普通对象返回原对象、null 返回 null；`getStateToBind` 普通对象返回原对象。
- `getInitialContext(空 env)` 抛 `NoInitialContextException`。
白盒断言（`testWhiteBox`）：public class、继承 `Object`、静态方法存在。
