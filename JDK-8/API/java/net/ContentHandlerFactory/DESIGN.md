# java.net.ContentHandlerFactory 设计文档（批次 C 深化）

> 原测试 ~5 断言。本批补齐：单方法接口结构、记录式工厂（mime 顺序/逐 mime 映射/未知与空与
> null 参数）、lambda 可用性、**URLConnection.setContentHandlerFactory 一次安装 + 二次
> Error("factory already defined")**、注册后 file: URL getContent() 全链路走工厂。

## 1. API 功能

单方法工厂接口：ContentHandler createContentHandler(String mimeType)。经
URLConnection.setContentHandlerFactory 安装（JVM 全局、仅一次，二次 → Error
"factory already defined"）。getContent() 查处理器顺序：静态缓存 → 工厂 → 类路径
（sun.net.www.content.*）——**已缓存过的 mime 不再询问工厂**。

## 2. 使用方式

安装后所有 URLConnection.getContent() 都经工厂；返回 null 交还默认查找。

## 3. 参考来源

- Oracle javadoc；OpenJDK URLConnection.getContentHandler。
- 探针 ProbeC.java H 节 + ProbeC2（工厂首装与缓存遮蔽问题）。

## 4. 测试用例设计（映射到 ContentHandlerFactoryTest.java）

| 测试方法 | 覆盖点 |
|---|---|
| testWhiteBox | 单方法/返回类型/参数/无受检异常、无 @FunctionalInterface 注解但可 lambda |
| testRecordingFactory | 逐 mime 映射、未知/空 mime → null、null mime 记录且返回 null、调用顺序、处理器可用 |
| testRegisterOnceAndError（最后跑） | 首装成功 → file .txt getContent 返回工厂哨兵且 mime=text/plain 恰好查询一次；二次安装 → Error 精确消息 |

易错规范细节：工厂集成测试必须用**未被缓存**的 mime（集成用 text/plain 且本 JVM 内首次 getContent），
且注册测试放最后（污染 JVM 全局）。
（双侧行为一致。）