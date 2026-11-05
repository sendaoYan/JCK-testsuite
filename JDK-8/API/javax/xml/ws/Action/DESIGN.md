# javax.xml.ws.Action 测试设计

## 1. API 功能

`javax.xml.ws.Action` 是 JAX-WS 2.2（JSR 224）定义的 WS-Addressing 动作注解，仅可标注于服务方法（`ElementType.METHOD`），用于显式指定该操作的输入 / 输出 / 故障消息的 WS-Addressing `Action` URI。当容器启用 Addressing 特性时，运行时据此生成 `wsaw:Action` 声明；`fault()` 成员以 `FaultAction[]` 数组形态为每个故障异常类绑定独立动作 URI。与 `javax.xml.ws.FaultAction` 构成「外层操作动作 + 嵌套故障动作」两级结构。

## 2. 使用方式

```java
@WebService(name = "EchoPort", targetNamespace = "urn:t")
interface EchoSei {
    @WebMethod(operationName = "echo")
    @Action(input = "urn:t:echo:in", output = "urn:t:echo:out",
            fault = { @FaultAction(className = FaultOneException.class, value = "urn:t:fault:one") })
    String echo(String s) throws FaultOneException;
}
```
运行期通过 `Method.getAnnotation(Action.class)` 回读 `input()/output()/fault()`；嵌套元素经 `act.fault()[i].className()/value()` 读取。本测试即以该反射回读闭环为主通道（WS-Addressing 的 wire 层行为属容器职责，纯 headless 下不发起网络）。

## 3. 参考来源

- Oracle javadoc（Java SE 8）: docs.oracle.com/javase/8/docs/api/javax/xml/ws/Action.html
- JSR 224 (JAX-WS 2.2) 第 7 章 Web Services Metadata；WS-Addressing 1.0 WSDL Binding
- OpenJDK 8u RI 实现（rt.jar 内置 Metro 派生，`javax.xml.ws.Action` 源于 JAX-WS 2.2 API）
- 本仓库测试：`JDK-8/API/javax/xml/ws/Action/ActionTest.java`

## 4. 测试用例设计 + 易错规范细节

维度清单：
- 结构面：isInterface / isAnnotation / 无超类 / 仅实现 `java.lang.annotation.Annotation` / 无字段无构造器；成员方法名集合恰为 `{input, output, fault}`。
- 元注解：`@Retention(RUNTIME)`、`@Target` 恰为 `{METHOD}`（无 TYPE/FIELD/PARAMETER）、`@Documented` 存在、非 `@Inherited`、非 `@Repeatable`；声明面元注解恰 3 个（Target+Retention+Documented）。
- 成员契约：三个成员均 public abstract；`input/output` 返回 `String`、`fault` 返回 `javax.xml.ws.FaultAction[]`；逐成员 `getDefaultValue()`：input `""`、output `""`、fault 空数组（长度 0，非 null）。
- 标注回读：全成员标注（双故障项顺序保持 fault[0]/fault[1] 各自 className/value 精确回读）、裸标注（实例回读默认值）、局部标注（只给 input，output 走默认）。
- 通用注解契约：等值实例 equals/hashCode 一致、toString 前缀 `@javax.xml.ws.Action`。
- 负例：未标注方法 getAnnotation 为 null / isAnnotationPresent false；类级不适用。

易错规范细节：
1. **嵌套 @FaultAction 的 className 是强制成员**（JDK-8 实测无默认值），省略将直接编译错误（见 FaultAction 目录 DESIGN）。
2. `fault()` 默认值是**空数组**（`new FaultAction[0]`），不是 null——断言用 `length == 0`。
3. `@Action` 只能出现在 `@WebMethod`/SEI 方法上；单独标注于普通类方法虽能编译，但语义上无消费者——测试仅做反射回读。
4. WS-Addressing 动作 URI 应为绝对 URI；规范未在运行时校验格式，测试不能断言非法 URI 报错。
5. jtreg `@summary` 中不要出现 `@Action` 等带 @ 记号（harness 视为非法 tag）。
