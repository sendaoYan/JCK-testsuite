# javax.xml.ws.http.HTTPException — 设计文档

## 一、API 功能
`HTTPException` 扩展 `ProtocolException`，携带 HTTP 状态码：唯一公共构造器 `HTTPException(int statusCode)` + 自有唯一方法 `getStatusCode()`。用于 XML/HTTP 绑定上把非 2xx 响应映射为异常（无 fault 载荷的纯传输层失败）。

## 二、使用方式
处理器或客户端运行时在收到 HTTP 错误状态时抛出/传播；业务代码 catch 后调 getStatusCode() 分支处理。测试覆盖层级、单构造器面、状态码矩阵（0/200/302/404/500/MAX_VALUE/-1 原样回读）、消息/cause 语义、equals 身份。

## 三、参考来源
- Oracle javadoc: javax.xml.ws.http.HTTPException
- JSR 224 §11.2
- OpenJDK 8 内置 RI（薄异常类，无独立实现）
- 本仓库测试：JDK-8/API/javax/xml/ws/http/HTTPException/HTTPExceptionTest.java

## 四、测试用例设计 + 易错规范细节
- 层级：HTTPException → ProtocolException → WebServiceException → RuntimeException。
- 恰 1 个公共构造器 HTTPException(int)；getStatusCode() 返回 int 且稳定。
- 状态码不做合法性校验：-1、0、Integer.MAX_VALUE 均原样回读（记录性事实）。
- getMessage() 默认 null（构造器只收码不收消息）；initCause 可执行一次（二致抛 ISE）；equals/hashCode 为 Object 身份语义。
- 易错点：无 (String) 构造器，断言构造器面时不要误加；statusCode 无 setter。
