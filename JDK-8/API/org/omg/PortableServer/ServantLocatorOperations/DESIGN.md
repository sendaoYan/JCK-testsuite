# org.omg.PortableServer.ServantLocatorOperations 契约覆盖设计（仅 JDK-8）

## API 功能
恰 2 方法：`preinvoke(byte[], POA, String, CookieHolder) -> Servant`（恰 throws ForwardRequest）、
`postinvoke(byte[], POA, String, Object, Servant) -> void`（无 checked 异常）。

## 使用方式
NON_RETAIN 回调面；父接口仅 ServantManagerOperations。

## 参考来源
- OMG POA；jdk8u372-b07 javap

## 测试用例设计 + 易错细节
1. 恰 2 方法；preinvoke 第 4 参是 CookieHolder（出参 cookie），postinvoke 第 4 参是裸 Object——易混。
