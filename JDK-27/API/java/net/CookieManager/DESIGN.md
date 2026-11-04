# CookieManager 设计文档

## 一、API 功能

默认 CookieHandler 实现，内置内存 CookieStore 与 CookiePolicy 管理会话/持久 Cookie。

## 二、Java 用户如何使用

用户 new CookieManager() 或指定 store/policy，用 CookieManager.setDefault 注册后由 HttpURLConnection 自动管理 Cookie。

## 三、开源代码实际用法调研

Web 客户端(爬虫、测试框架)用 CookieManager 维持会话。

## 四、测试用例设计

get/setCookiePolicy、getCookieStore 非空；put 后 get 返回对应 Cookie；默认策略 ACCEPT_ORIGINAL_SERVER。

## 五、契约深化补充（批次 D，探针验证于 JDK-27）

**已深化断言（103 条）覆盖：**
- 行为化默认策略：`new CookieManager()` 实际生效 ACCEPT_ORIGINAL_SERVER（put 两域后仅原服务器域可 get）。
- setCookiePolicy(null) 被静默忽略。
- put 解析：Set-Cookie 与 Set-Cookie2、小写头键、多值列表、双键并存、host-only cookie 的 domain=""、同名替换（域+名）、坏值跳过、无点主机加 `.local` 后缀、默认 path=URI 目录规则（无 path→"/"，"/a/b"→"/a/"）。
- put/get(null,·) 与 put/get(·,null) 均抛 `IllegalArgumentException("Argument is null")`（非 NPE）。
- get 返回值：必含 "Cookie" 键；请求头映射被忽略且不被修改；返回映射不可修改（put 抛 UOE）。
- get 过滤：path 匹配（sort-by-path 长者优先）、secure cookie 仅 https、portlist 限制、过期即地删除。
- get(null) 抛 IAE；自定义 CookieStore+CookiePolicy 组装生效。

**易错细节（探针实证）：**
- 多路径 cookie 返回顺序：JDK-27 窄路径在前（JDK-8 宽路径在前）——测试须用 containsAll 而非顺序断言。
- store.add 的 URI 键被规范化为 `scheme://host`（去端口、去路径）；store 层不做 path 过滤（过滤是 CookieManager.get 的职责分层）。
