# javax.smartcardio.TerminalFactory 设计案

## 1. API 功能
- `final` 工厂类；`getDefaultType()`、`getDefault()`、三个 `getInstance(type, params[, provider])` 重载。
- 实例方法：`getProvider()`、`getType()`、`terminals()`（返回 CardTerminals）、`toString()`。
- 默认类型解析：系统属性 `javax.smartcardio.TerminalFactory.DefaultType` → `PC/SC`（SunPCSC provider）→ 兜底 `None`。
- `getInstance("None", …)` 不可用（`None` 类型只能由 `getDefault()` 提供）。

## 2. Java 用户如何使用
- `TerminalFactory f = TerminalFactory.getDefault(); CardTerminals ct = f.terminals(); ct.list();`
- 自定义 provider 时 `getInstance("MyType", params, provider)`。

## 3. 开源代码实际用法调研
来源：JDK 源码 `TerminalFactory.java`（`src/java.smartcardio/share/classes/javax/smartcardio/`）、JDK 测试 `test/jdk/javax/smartcardio/TerminalFactorySpiTest.java`。
- 无硬件平台 `getDefaultType()` 返回 `None`，`getDefault()` 返回 provider 名为 `None` 的工厂，`terminals().list()` 为空（JDK 源码静态初始化逻辑）。
- `TerminalFactorySpiTest.java`：注册自定义 Provider 后 `getInstance("MyType", new Object()).terminals()` 触发 `engineTerminals()`（JDK test/jdk）。

## 4. 测试用例设计 + 自动化实现
黑盒部分（`TerminalFactoryTest.testBlackBox`）：
- `getDefaultType()` 非空；`getDefault()` 非 null 且 `getType()==getDefaultType()`、provider/toString 非空、`terminals()` 非空。
- 无硬件（type `None`，本环境实测）：`list()/list(ALL)` 为空、`getTerminal` 返回 null、`waitForChange` 抛 `IllegalStateException`。
- 有 PC/SC 时仅断言 `list()` 可调用（不假设有读卡器）。
- `getInstance("None")` 抛 `NoSuchAlgorithmException`；`getInstance(null, …)` 抛 NPE。
