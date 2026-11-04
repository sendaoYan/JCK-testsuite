# CookieManager 设计文档（JDK-8 侧）

## 一、API 功能

CookieHandler 的具体实现：以 CookieStore 为存储、CookiePolicy 为准入策略，put/get 完成 Set-Cookie 解析与请求 Cookie 头构造。

## 二、Java 用户如何使用

`CookieHandler.setDefault(new CookieManager(store, policy))`；或子类覆写 put/get 定制。

## 三、开源代码实际用法调研

HttpClient/OkHttp/Jsoup 的 cookie jar 是同型实现；Applet/WebStart 会话保持。

## 四、测试用例设计

put/get 往返、策略开关、store 可见性、过期剔除、默认策略行为、null 参数负例、头解析（Set-Cookie/Set-Cookie2/大小写/多值）。

## 五、契约深化补充（批次 D，探针验证于 JDK-8）

**已深化断言（100 条）覆盖：** 行为化默认策略 ACCEPT_ORIGINAL_SERVER；setCookiePolicy(null) 静默忽略；put 解析矩阵（双键、小写键、多值、host→domain、同名替换、坏值跳过、.local 后缀、默认 path=URI 目录）；put/get(null) 抛 `IllegalArgumentException("Argument is null")`（非 NPE）；get 必含 "Cookie" 键、请求映射不被修改、返回映射不可修改；path/secure/portlist/过期过滤；自定义 store+policy 组装；CookieHandler 集成生命周期（finally 恢复）。

**易错细节（探针实证，JDK-8 与 JDK-27 存在差异）：**
- 多路径 cookie 的 get 返回顺序：JDK-8 宽路径在前（JDK-27 窄路径在前）——测试用 containsAll。
- store 的 URI 键规范化为 `scheme://host`（无端口无路径）；store 层不做 path 过滤。
