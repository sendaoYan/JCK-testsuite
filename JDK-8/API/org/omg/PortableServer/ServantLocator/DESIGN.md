# org.omg.PortableServer.ServantLocator 契约覆盖设计（仅 JDK-8）

## API 功能
NON_RETAIN servant 管理器引用接口：本体无自有方法，extends `{ServantLocatorOperations, ServantManager, IDLEntity}`。

## 使用方式
preinvoke/postinvoke 每请求回调；cookie 传递调用间状态。

## 参考来源
- OMG POA；jdk8u372-b07 javap + 实测

## 测试用例设计 + 易错细节
1. 自有方法==0；extends 三接口顺序精确。
2. Proxy preinvoke/postinvoke 按调用顺序路由。
