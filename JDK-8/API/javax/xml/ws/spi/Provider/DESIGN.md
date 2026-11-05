# javax.xml.ws.spi.Provider — 设计文档

## 一、API 功能
`Provider`（public abstract，protected 构造器）是 JAX-WS 全套服务的 SPI 入口：静态 `provider()` 按系统属性 `JAXWSPROVIDER_PROPERTY("javax.xml.ws.spi.Provider")`/ServiceLoader 查找实现；6 个抽象核心工厂——createServiceDelegate x2、createEndpoint x3、createAndPublishEndpoint x2、createW3CEndpointReference x2、readEndpointReference、getPort——及 createDispatch 之外的 Service 门面支撑。

## 二、使用方式
业务代码经 Service/Endpoint 静态方法间接触达；SPI 消费者直接 Provider.provider() 后调工厂。测试覆盖查找、重载数表（createEndpoint=3/createServiceDelegate=2/createAndPublishEndpoint=2/createW3CEndpointReference=2/readEndpointReference=1/getPort=1/provider=1）、delegate 矩阵、端点工厂负例（非法绑定 ID）、EPR 工厂（6/9 参、本地 WSDL）。

## 三、参考来源
- Oracle javadoc: javax.xml.ws.spi.Provider
- JSR 224 §5.1 Provider
- OpenJDK 8 内置 RI: com.sun.xml.internal.ws.spi.ProviderImpl
- 本仓库测试：JDK-8/API/javax/xml/ws/spi/Provider/ProviderTest.java

## 四、测试用例设计 + 易错规范细节
- 实测事实（与想当然相反处）：provider() 不缓存单例（两次调用可不同实例，只断言非 null）；抽象方法 6 个而非 7；无 WSDL delegate 默认 executor 可为 null（lazy）；features 版 createServiceDelegate 在本 RI 抛 "Doesn't support any Service specific features"。
- 负例表：createEndpoint(bogus binding) → WebServiceException "Wrong binding ID"；createServiceDelegate(...,null) → NPE；readEndpointReference(null) → IAE "source parameter must not be null"；createW3CEndpointReference(null,...) → ISE "Address in an EPR cannot be null"。
- 9 参 createW3CEndpointReference 参数序为 (address, interfaceName, serviceName, portName, refParams, wsdl, metadata, extensions, attributes)——interfaceName 在第 2 位，与 6 参形态不同。
- WSDL 用本地 HttpServer 回环提供；createAndPublishEndpoint 用 localhost:0 且立即 stop（否则 Dispatcher 非守护线程挂住 JVM）。
