# javax.smartcardio.TerminalFactorySpi 设计案

## 1. API 功能
- 抽象 SPI 类，`protected TerminalFactorySpi()` 构造器。
- 抽象方法：`protected abstract CardTerminals engineTerminals()`。
- 供 `TerminalFactory` 服务提供者框架调用：provider 把 `TerminalFactory.<type>` 映射到实现类。

## 2. Java 用户如何使用
- 实现 SPI：`class MySpi extends TerminalFactorySpi { protected CardTerminals engineTerminals() { … } }`。
- 在 `Provider` 中注册 `TerminalFactory.MyType`，再用 `TerminalFactory.getInstance("MyType", params)` 获取。

## 3. 开源代码实际用法调研
来源：JDK 测试 `test/jdk/javax/smartcardio/TerminalFactorySpiTest.java`。
- 定义 `MyTerminalFactorySpi extends TerminalFactorySpi`，`engineTerminals()` 置标志并返回 null；注册 Provider 后 `TerminalFactory.getInstance("MyType", new Object()).terminals()` 触发该方法（JDK test/jdk）。
- JDK 源码 `TerminalFactory.NoneFactorySpi` 实现 `engineTerminals()` 返回 `NoneCardTerminals`。

## 4. 测试用例设计 + 自动化实现
白盒部分（`TerminalFactorySpiTest.testWhiteBox`）：抽象类、非接口、继承 Object、protected 构造器、`engineTerminals` protected abstract。
黑盒部分（`TerminalFactorySpiTest.testBlackBox`）：子类匿名实现 `engineTerminals()` 返回确定性 CardTerminals，通过子类公开方法调用并断言恒等、且返回对象可 `list(State.ALL)` 得到空列表。
