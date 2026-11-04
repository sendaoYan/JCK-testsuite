# java.net.http.HttpClient.Redirect 设计文档

## 1. API 功能

重定向策略枚举：`NEVER`、`ALWAYS`、`NORMAL`（自动升级 https 不降级）。

## 2. 使用方式

`HttpClient.newBuilder().followRedirects(Redirect.NORMAL).build()`、`client.followRedirects()`。

## 3. 参考来源

- JDK-27 `javap`；`java.net.http` 包规范（重定向策略语义）。

## 4. 测试用例设计 + 易错规范细节

文件：`RedirectTest.java`。

- 枚举形状：3 个值、ordinal、name、toString=name；`values()` 每次新数组。
- `valueOf` 全命中 + bogus/小写 IAE + null NPE；compareTo 矩阵与排序。
- EnumSet.range(NEVER, ALWAYS)、equals/hashCode。
- 每个值经 builder round-trip；客户端默认 NEVER（probed）。

## 5. 版本差异

- 自 JDK 11 起不变；JDK-8 无此包。
