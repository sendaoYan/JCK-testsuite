# javax.imageio.IIOParamController — DESIGN

## 1. API 功能
`IIOParamController` 是函数式接口，单一抽象方法 `boolean activate(IIOParam param)`，用于在读写前以 GUI 或编程方式为用户填充 `IIOParam` 参数。返回 `true` 表示参数就绪，`false` 表示用户取消。接口无字段、无嵌套类型、无父接口。

## 2. Java 用户如何使用
用户实现控制器后挂到参数对象：`param.setController(p -> { ...配置参数...; return true; }); boolean ok = param.activateController();`。也可直接 lambda：`IIOParamController c = param -> false;`。

## 3. 开源代码实际用法调研（来源：jdk8u-dev 源码）
- `jdk/src/share/classes/javax/imageio/IIOParam.java:670`：`activateController()` 为 `if (!hasController()) throw new IllegalStateException("hasController() == false!"); return getController().activate(this);` — `this` 被原样传给控制器。
- `IIOParam.java:587` 附近：`setController` 直接存引用；`hasController()` 判断 `controller != null`；`defaultController` 是 protected 字段，JDK 内无任何隐式激活路径（JDK 内置读写器均不设默认控制器）。

## 4. 测试用例设计 + 自动化实现（`IIOParamControllerTest`）
- 白盒：isInterface、public、无父接口、无字段/嵌套类型、单抽象方法 `activate` 为 public abstract、返回 boolean、参数为 IIOParam、无 checked 异常。
- lambda 形态：`param -> false` / `param -> true` 直接调用返回原值。
- 记录式控制器：断言收到的 param 与传入是同一实例。
- 端到端：`setController` + `activateController()` 分派、返回值透传（true/false 都验证）、`hasController/getController`。
- 无控制器：`activateController` 实际抛 ISE —— javadoc 声称返回 false，与实现不符（双侧 JDK 一致，属文档分歧，测试按实测断言）。
- `setController(null)` 摘除控制器。
- 控制器内修改参数在激活后持续可见（setSourceSubsampling 往返）。
- 控制器抛 RuntimeException 原样带消息穿透 `activateController`。
- defaultController 怪癖：仅设置 protected `defaultController` 时 `hasController()` 仍为 false、`activateController` 抛 ISE 且从不隐式调用它；显式 `setController(getDefaultController())` 后正常运行。
