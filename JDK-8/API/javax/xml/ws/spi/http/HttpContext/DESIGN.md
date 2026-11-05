# javax.xml.ws.spi.http.HttpContext — 设计文档

## 一、API 功能
`HttpContext`（public abstract，公共无参构造器）是 JAX-WS 与任意 HTTP 容器的桥：4 个声明方法中 `setHandler(HttpHandler)` 是唯一具体方法——Endpoint.publish(HttpContext) 时运行时把真实处理器装进容器；另 3 个抽象方法 `getPath()`、`getAttribute(String)`、`getAttributeNames()` 由容器实现暴露路径与属性。

## 二、使用方式
容器侧：实现抽象三方法（属性以 Map 支撑），Endpoint.create(impl).publish(myContext) 后 setHandler 回调送达 RI 处理器，容器在其 HTTP 引擎上把请求路由给它。测试验证类面（4 方法、setHandler 具体、3 抽象）、属性契约语义、真实 publish 的 setHandler 回调。

## 三、参考来源
- Oracle javadoc: javax.xml.ws.spi.http.HttpContext
- JSR 224 §13.2 Portable HttpContext
- OpenJDK 8 内置 RI: com.sun.xml.internal.ws.transport.http.HttpAdapter 安装端
- 本仓库测试：JDK-8/API/javax/xml/ws/spi/http/HttpContext/HttpContextTest.java

## 四、测试用例设计 + 易错规范细节
- 类面：恰 4 个声明方法；setHandler void/单参/具体；getPath 返回 String、getAttribute 返回 Object、getAttributeNames 返回 Set 且均抽象；公共无参构造器。
- 属性契约：缺键 null；null 值键仍列入 attributeNames；双上下文属性存储隔离；setHandler 可重复调用并替换先前处理器（自造实现语义）。
- 真实通道：Endpoint.publish(ctx) → setHandler 被调（>=1）且装入实现相关处理器（断言只要求是 HttpHandler，不绑死 com.sun 类名，保持实现无关）；stop 后 isPublished false；换新端点/新上下文可重发布。
- 易错点：发布到 spi HttpContext 不会开监听端口，但 stop 仍必须调用；断言处理器类名会绑死 RI，须用 instanceof 形态。
