# javax.imageio.IIOParamController — DESIGN

## 1. API 功能
`IIOParamController` 是函数式接口，单一抽象方法 `boolean activate(IIOParam param)`，用于在读写前以 GUI 或编程方式为用户填充 `IIOParam` 参数。返回 `true` 表示参数已就绪，`false` 表示用户取消（`IIOParam` 不被改动）。

## 2. Java 用户如何使用
用户实现控制器后挂到参数对象：`param.setController(p -> { ...配置参数...; return true; }); boolean ok = param.activateController();`。也可用 lambda `IIOParamController c = param -> false;`。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码）
- `src/java.desktop/share/classes/javax/imageio/IIOParam.java:670`：`activateController()` 实现为 `if (!hasController()) throw new IllegalStateException(...); return getController().activate(this);`，明确 `this` 被传给控制器（源码内）。
- `src/java.desktop/share/classes/javax/imageio/IIOParam.java:587`：`setController` 直接存引用；`hasController()` 判断 `controller != null`。

## 4. 测试用例设计 + 自动化实现（`IIOParamControllerTest.testBlackBox`）
- lambda 实现返回 false/true 的直接调用。
- 记录式控制器：断言收到的 param 与传入一致。
- 端到端：`ImageReadParam.setController` + `activateController()` 分派到控制器并透传返回值。
- 白盒：isInterface、public、无直接父接口、单抽象方法 `activate` 返回 boolean 且参数为 IIOParam。
- CORE-B 补充：无控制器时 activateController 实抛 ISE（javadoc 声称返回 false 的文档分歧，双侧一致）；setController(null) 摘除；控制器内修改参数激活后可见；控制器 RuntimeException 连消息穿透；defaultController 仅 informational，显式 setController(getDefaultController()) 后才运行；结构断言（无字段/嵌套类型/无 checked 异常/activate 为 public abstract）。
