# javax.xml.ws.spi.http.HttpHandler — 设计文档

## 一、API 功能
`HttpHandler`（public abstract，公共无参构造器）是容器侧最小处理器契约：恰 1 个抽象方法 `handle(HttpExchange) throws IOException`。Endpoint.publish(HttpContext) 时运行时经 HttpContext.setHandler 装入的正是它的实现（RI 的 PortableHttpHandler），容器每收到匹配请求调用一次。

## 二、使用方式
容器实现 handle：从 exchange 读请求流、交 JAX-WS、写回响应并 close；或测试用它驱动进程内闭环。测试覆盖类面（单方法/throws IOException/公共无参构造器/无接口无字段）、自造派发语义、以及真实 RI 处理器对自造 exchange 的完整 SOAP 请求/响应处理（可复用二次）。

## 三、参考来源
- Oracle javadoc: javax.xml.ws.spi.http.HttpHandler
- JSR 224 §13.4
- OpenJDK 8 内置 RI: com.sun.xml.internal.ws.transport.http.HttpAdapter 的 WSHttpHandler
- 本仓库测试：JDK-8/API/javax/xml/ws/spi/http/HttpHandler/HttpHandlerTest.java

## 四、测试用例设计 + 易错规范细节
- 类面：恰 1 声明方法；abstract public void handle(HttpExchange) throws IOException（throws 恰 1 类）；无父接口/公共字段；单公共无参构造器。
- 自造派发：handle 收到同一 exchange 实例（身份）；handler 写状态/关交换对容器可见；重复 handle 计数递增；处理器可入集合按链派发；匿名子类可用。
- 进程内闭环：publish 捕获的 RI 处理器 handle(自造 exchange) → 200/关闭/SOAP 信封含载荷/Content-Type；同一处理器可服务第二个 exchange（无一次性状态）。
- 易错点：自造 handle 覆写须声明 throws IOException（父类有）；断言 RI 处理器用 instanceof 而非类名。
