# CookiePolicy 设计文档

## 一、API 功能

接口，判定是否接受来自某 URI 的 Cookie。

## 二、Java 用户如何使用

用户实现 shouldAccept(uri, cookie) 或使用内建 ACCEPT_ALL/ACCEPT_NONE/ACCEPT_ORIGINAL_SERVER 传给 CookieManager。

## 三、开源代码实际用法调研

浏览器/客户端按隐私策略定制 Cookie 接受规则。

## 四、测试用例设计

三个内建策略常量与 shouldAccept 行为；自定义策略被调用。

## 五、契约深化补充（批次 D，探针验证于 JDK-27）

**已深化断言（60 条）覆盖：**
- 白盒：三个常量 static final 且类型 CookiePolicy；接口仅一个抽象方法 shouldAccept(URI, HttpCookie)。
- ACCEPT_ALL / ACCEPT_NONE 全 null 矩阵（null URI、null cookie、双 null 均 true/false）。
- ACCEPT_ORIGINAL_SERVER 20 组矩阵：域相等（含大小写不敏感）、点前缀域单级子域、深层子域拒绝、cookie domain=null 视为原服务器、host 与带端口 URI、null 参数 false。
- 自定义实现 + CookieManager 集成：策略对每条 put 的 cookie 恰好调用一次（2 次手动 + N 次 put）。

**易错细节（探针实证）：**
- ACCEPT_ORIGINAL_SERVER 的"深层子域拒绝"与 HttpCookie.domainMatches 同源：`a.b.example.com` 不匹配 `.example.com`。
- policy==null 时 CookieManager 保持原策略不抛异常。
