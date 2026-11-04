# javax.imageio.spi.RegisterableService — DESIGN

## 1. API 功能
`RegisterableService` 是服务提供者的可选注册回调接口，只有两个方法：
- `void onRegistration(ServiceRegistry registry, Class<?> category)`：注册后回调一次。
- `void onDeregistration(ServiceRegistry registry, Class<?> category)`：注销时回调。

## 2. Java 用户如何使用
提供者类实现该接口（`IIOServiceProvider` 已实现），`ServiceRegistry` 在注册/注销时自动调用回调。用户可直接调用 `onRegistration(registry, category)`/`onDeregistration(...)`，或实现接口记录回调次数做测试。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码）
- `src/java.desktop/share/classes/javax/imageio/spi/IIOServiceProvider.java`：`public abstract class IIOServiceProvider implements RegisterableService`，并提供空实现的两个回调。
- `src/java.desktop/share/classes/javax/imageio/spi/ServiceRegistry.java`（内部 `SubRegistry`）：`registerServiceProvider` 中 `if (provider instanceof RegisterableService) rs.onRegistration(registry, category)`，`deregisterServiceProvider` 中调用 `onDeregistration`。

## 4. 测试用例设计 + 自动化实现（`RegisterableServiceTest.testBlackBox`）
- 将真实 PNG writer SPI 强转为 `RegisterableService`，在默认 `IIORegistry` 上调用 `onRegistration`/`onDeregistration`，断言无异常。
- 自定义匿名实现记录回调计数，手动触发后断言各回调恰好派发一次。
