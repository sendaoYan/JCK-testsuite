# java.net.http.HttpResponse.ResponseInfo 设计文档

## 1. API 功能

`BodyHandler.apply` 的入参：`statusCode()`、`headers()`、`version()`（三个抽象方法，无 default）。

## 2. 使用方式

在自定义 BodyHandler 内按 `ri.statusCode()`/`ri.headers()` 分支选择 BodySubscriber。

## 3. 参考来源

- JDK-27 `javap`；`java.net.http` 包规范。

## 4. 测试用例设计 + 易错规范细节

文件：`ResponseInfoTest.java`。

- 形状：3 个抽象方法、0 个 default、返回类型 int/HttpHeaders/Version、非 sealed。
- 匿名实现返回哨兵值（headers identity、HTTP_3）；大小写不敏感头查询；
  驱动按状态码分支的 BodyHandler（2xx 解码空体 / 5xx replacing "error-500"）；
  firstValue 取多值第一个。

## 5. 版本差异

- 自 JDK 11 稳定；JDK-8 无此包。
