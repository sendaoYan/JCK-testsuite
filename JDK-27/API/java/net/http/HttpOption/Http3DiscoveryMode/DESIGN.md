# java.net.http.HttpOption.Http3DiscoveryMode 设计文档

## 1. API 功能

HTTP/3 发现方式枚举：`ANY`、`ALT_SVC`、`HTTP_3_URI_ONLY`。

## 2. 使用方式

`requestBuilder.setOption(HttpOption.H3_DISCOVERY, Http3DiscoveryMode.ALT_SVC)`；
读取 `request.getOption(HttpOption.H3_DISCOVERY)`。

## 3. 参考来源

- JDK-27 `javap`；`HttpOption.java`。

## 4. 测试用例设计 + 易错规范细节

文件：`Http3DiscoveryModeTest.java`。

- 枚举形状：3 值、顺序 ANY/ALT_SVC/HTTP_3_URI_ONLY、ordinal、name/toString；
  `values()` 每次新数组（probed）；valueOf 全命中 + bogus/小写/空 IAE + null NPE。
- compareTo 矩阵、EnumSet allOf/range、排序与 name round-trip。
- setOption 三值逐一 round-trip（probed）；默认无值；switch 穷尽三分支。

## 5. 版本差异

- JDK-27 新增枚举；早期版本不存在。
