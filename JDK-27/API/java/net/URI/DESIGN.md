# URI 设计文档

## 一、API 功能

RFC 3986 URI 解析器，表示统一资源标识符，区分透明(opaque)与层级(hierarchical)形式，提供 scheme/authority/path/query/fragment 等组件访问与规范化、相对解析。

## 二、Java 用户如何使用

用户用 new URI(String) 或 new URI(scheme, ssp, fragment) 构造，调用 getScheme/getHost/getPort/getPath/getQuery/getFragment、resolve()/relativize()/normalize() 比较与拼接 URI。

## 三、开源代码实际用法调研

Spring、Apache HttpClient、OkHttp 等普遍用 java.net.URI 表示与校验 HTTP 端点；JAX-RS 用 URI 作为资源定位基础。

## 四、测试用例设计

构造层级与 opaque URI 并断言组件解析；验证 isAbsolute/isOpaque；resolve/relativize 往返；toString 往返；非法 scheme(数字开头)抛 URISyntaxException。
