# javax.xml.ws.spi.ServiceDelegate — 设计文档

## 一、API 功能
`ServiceDelegate`（public abstract，protected 无参构造器）是 javax.xml.ws.Service 门面背后的真实引擎：恰 19 个声明方法全部 public abstract——getPort x5（QName±features、EPR、Class±features）、createDispatch x6（Class 族/JAXB 族 × QName±features/EPR）、addPort、getServiceName、getPorts、getWSDLDocumentLocation、getHandlerResolver/setHandlerResolver、getExecutor/setExecutor。无任何具体方法/公共字段（纯契约类）。

## 二、使用方式
由 Provider.createServiceDelegate 创建，Service 每个公共方法一对一转发。测试用反射锁重载数与精确签名（getMethod 逐形态），再取活 delegate 验证元数据/resolver/executor/addPort 端口发现。

## 三、参考来源
- Oracle javadoc: javax.xml.ws.spi.ServiceDelegate
- JSR 224 §5.2 ServiceDelegate
- OpenJDK 8 内置 RI: com.sun.xml.internal.ws.client.WSServiceDelegate
- 本仓库测试：JDK-8/API/javax/xml/ws/spi/ServiceDelegate/ServiceDelegateTest.java

## 四、测试用例设计 + 易错规范细节
- 面锁定：19 方法、0 具体方法、构造器 protected；getPort=5/createDispatch=6 用重载计数表 + getMethod 精确签名双重锁定。
- 活体：无 WSDL 时 getServiceName 回读精确、getPorts 空、getWSDLDocumentLocation null、默认 resolver null；setHandlerResolver 身份回读；setExecutor/getExecutor 身份且 null 容忍；addPort 后 getPorts 可发现新端口；Service.create 门面元数据一致。
- 易错点：getPorts 返回 Iterator<QName>（非 Set）；executor 断言只做身份/可设，不断言默认非 null（RI 为 lazy）；重载合并断言用 && 聚合保持文件断言密度合规。
