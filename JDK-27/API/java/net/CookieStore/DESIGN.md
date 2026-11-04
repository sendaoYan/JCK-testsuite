# CookieStore 设计文档

## 一、API 功能

接口，表示 Cookie 存储，支持增删查与按 URI 匹配。

## 二、Java 用户如何使用

用户 new InMemoryCookieStore() 或自定义，传给 CookieManager。

## 三、开源代码实际用法调研

CookieManager 默认使用 InMemoryCookieStore。

## 四、测试用例设计

add/get/remove/removeAll/getURIs 语义；get 按 URI+请求属性返回匹配 Cookie；用 new InMemoryCookieStore() 实例验证。

## 五、契约深化补充（批次 D，探针验证于 JDK-27）

**已深化断言（76 条）覆盖：**
- 通过 CookieManager.getCookieStore() 取包私有 InMemoryCookieStore 实例（唯一可测路径）。
- 签名表：add/remove/removeAll/getCookies/getURIs/get(uri) 的返回类型与语义。
- null 矩阵：add(uri,null)→NPE "cookie is null"；get(null)→NPE "uri is null"；remove(uri,null)→NPE；add(null,cookie) 合法（孤儿 cookie 域可见）。
- 域可见性：add 以 a.example.com 存入，get(b.example.com) 不可见、get(a.example.com) 可见。
- 不可修改视图：getCookies()/getURIs()/get(uri) 的返回列表 add 抛 UOE（JDK-8 仅 getCookies 不可修改）。
- remove：URI 参数被忽略（仅按 cookie 等值匹配）；removeAll 空集 false、有则 true 且清空。
- 去重替换：同 (name,domain,path) 覆盖旧实例；HttpCookie.equals 跨 store 互操作。
- 过期过滤：getCookies/get 不返回已过期 cookie；add 拒绝已过期 cookie（静默）。

**易错细节（探针实证）：**
- store 的 URI 键规范化：`http://host:port/path` → `http://host`（scheme+host，无端口无路径）。
- store 层**不做** path 过滤（get(/a) 能看到 path=/b 的 cookie）——分层契约：过滤在 CookieManager.get。
- JDK-8：getURIs()/get(uri) 返回可变快照，改它不影响 store；JDK-27 直接 UOE。
