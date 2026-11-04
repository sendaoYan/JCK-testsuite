# java.net.http.HttpClient.Version 设计文档

## 1. API 功能

HTTP 协议版本枚举：`HTTP_1_1`、`HTTP_2`、`HTTP_3`（JDK-27 新增第三个值）。

## 2. 使用方式

`HttpClient.newBuilder().version(Version.HTTP_3).build()`、`client.version()`、
`HttpRequest.Builder.version(...)`、`HttpResponse.version()`。

## 3. 参考来源

- JDK-27 `javap`；`java.net.http` 包规范；JDK-27 HTTP/3 支持说明。

## 4. 测试用例设计 + 易错规范细节

文件：`VersionTest.java`。

- 枚举形状：`values()` 恰 3 个，顺序 HTTP_1_1/HTTP_2/HTTP_3，ordinal 0/1/2（probed）。
- `valueOf` 全部命中 + bogus/小写 → IAE、null → NPE；`values()` 每次返回新数组。
- compareTo 矩阵（<、=、>）、排序稳定性、equals/hashCode/toString（toString 即 name）。
- EnumSet 全集/range 互通；`valueOf(name())` round-trip。
- 通过 HttpClient builder round-trip（含 HTTP_3 可 build），客户端默认 HTTP_2（probed）。

## 5. 版本差异

- JDK-27 有 `HTTP_3`；JDK-8 无此包；JDK 11–24 只有 HTTP_1_1/HTTP_2。
