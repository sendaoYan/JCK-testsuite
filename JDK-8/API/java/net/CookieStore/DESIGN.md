# CookieStore 设计文档（JDK-8 侧）

## 一、API 功能

Cookie 存储抽象：add/remove/removeAll/getCookies/getURIs/get(uri)；JDK-8 唯一实现为包私有 InMemoryCookieStore。

## 二、Java 用户如何使用

经 CookieManager.getCookieStore() 取用；或实现接口接持久化后端。

## 三、开源代码实际用法调研

浏览器 cookie 数据库、Retrofit/OkHttp CookieJar 持久化同型。

## 四、测试用例设计

add/get 往返、null 矩阵、域可见性、去重替换、remove/removeAll 语义、过期过滤、返回集合可变性。

## 五、契约深化补充（批次 D，探针验证于 JDK-8）

**已深化断言（74 条）覆盖：** 签名表；null 矩阵（add(uri,null)→NPE "cookie is null"、get(null)→NPE "uri is null"、add(null,cookie) 合法孤儿）；域可见性；remove 的 URI 参数被忽略；removeAll 空集 false；(name,domain,path) 去重覆盖；过期 cookie 被 getCookies/get 过滤、add 静默拒绝；store 不做 path 过滤（分层契约）。

**易错细节（探针实证，JDK-8 与 JDK-27 存在差异）：**
- JDK-8：getURIs()/get(uri) 返回**可变快照**，修改不影响 store；JDK-27 对这两个视图的 add 抛 UOE（getCookies 两侧均不可修改）。
- store 的 URI 键规范化为 `http://host`（无端口无路径）。
