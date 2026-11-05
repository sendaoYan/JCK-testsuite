# org.omg.PortableServer.ServantActivatorOperations 契约覆盖设计（仅 JDK-8）

## API 功能
恰 2 方法：`incarnate(byte[], POA) -> Servant`（恰 throws ForwardRequest）、
`etherealize(byte[], POA, Servant, boolean, boolean) -> void`（无 checked 异常）。

## 使用方式
RETAIN 回调面；父接口仅 ServantManagerOperations。

## 参考来源
- OMG POA；jdk8u372-b07 javap

## 测试用例设计 + 易错细节
1. 恰 2 方法、签名逐参精确（etherealize 两个 boolean 分别为 cleanup_in_progress/remaining_activations）。
