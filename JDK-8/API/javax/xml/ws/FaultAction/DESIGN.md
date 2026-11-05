# javax.xml.ws.FaultAction 测试设计

## 1. API 功能

`javax.xml.ws.FaultAction` 为 WS-Addressing 故障动作注解，作为 `@Action.fault()` 的嵌套元素使用，把某个故障异常类（`className()`，类型边界 `Class<? extends Exception>`）绑定到显式动作 URI（`value()`）。它自身虽声明 `@Target(METHOD)`，但规范用法仅是 Action 的数组元素；单独标注方法没有运行时消费者。

## 2. 使用方式

```java
@Action(input = "urn:bank:withdraw:in",
        fault = { @FaultAction(className = InsufficientFundsException.class, value = "urn:bank:fault:funds") })
double withdraw(double amount) throws InsufficientFundsException;
```
回读通道：`method.getAnnotation(Action.class).fault()[i].className()/value()`。本测试覆盖双故障项（顺序与取值逐项精确）、单故障项（value 省略走默认）、裸 `@Action`（fault 空数组）三形态。

## 3. 参考来源

- Oracle javadoc（Java SE 8）: docs.oracle.com/javase/8/docs/api/javax/xml/ws/FaultAction.html
- JSR 224 (JAX-WS 2.2)；WS-Addressing 1.0 WSDL Binding（fault action 映射）
- OpenJDK 8u RI（注解源码即 API 契约的一部分）
- 本仓库测试：`JDK-8/API/javax/xml/ws/FaultAction/FaultActionTest.java`

## 4. 测试用例设计 + 易错规范细节

维度清单：
- 结构/元注解：成员集合恰 `{className, value}`；className 返回 `Class`、value 返回 `String`；RUNTIME / Target 恰 {METHOD} / Documented / 非 Inherited 非 Repeatable / 声明元注解恰 3 个。
- 默认值：**className 无默认值**（`getDefaultValue() == null`）；value 默认 `""`。
- 泛型边界：`className` 的 generic 签名含 `java.lang.Class<? extends java.lang.Exception>`（toGenericString 断言）。
- 回读：嵌套数组顺序、className 精确 Class 对象（`==` 比较）、value 精确串；单元素省略 value → 实例回读空串；equals/toString 契约。
- 负例：裸 `@Action` 方法上 standalone FaultAction 不可见；类级不适用。

易错规范细节：
1. **最大坑：javadoc 声称 className 默认 `FaultAction.class`（哨兵），JDK-8 实测源码无 default**——`@FaultAction(value="x")` 直接**编译错误** "missing a default value for the element 'className'"。即 className 为强制成员，测试只能用 `getDefaultValue() == null` 锁定该事实，不能写出省略 className 的源码。
2. `className` 的值须是 `Exception` 子类；用自定义静态嵌套异常类（`public static class XxxException extends Exception`）作载体。
3. 嵌套实例的 equals 是值语义，但跨数组下标比较同一元素 equals 自身恒真——不要对两个不同 className 的元素断言 equals。
4. `@FaultAction` 声明的 Target 是 METHOD，但作为元素嵌套时 Target 不约束嵌套位置（JLS 只对直接标注生效）；不要断言"嵌套非法"。
