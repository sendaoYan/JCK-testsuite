# ResponseCache 设计文档（JDK-8 侧）

## 一、API 功能

URL 协议栈的响应缓存挂点：get 查缓存、put 写缓存；getDefault/setDefault 全局注册。

## 二、Java 用户如何使用

`ResponseCache.setDefault(new MyResponseCache())`；HttpURLConnection 自动回调。

## 三、开源代码实际用法调研

Java Web Start 离线缓存；OkHttp Cache 的平台前置形态。

## 四、测试用例设计

白盒方法集、get/put 抽象签名与 IOException、子类收发保真、默认生命周期。

## 五、契约深化补充（批次 D，探针验证于 JDK-8）

**已深化断言（38 条）覆盖：** 精确 4 方法集；get→CacheResponse/put→CacheRequest；static 工厂对；子类 get/put 收到精确 URI/method/headers（日志逐字段断言）且返回缓存实例（==）；put 接收 file: URLConnection（协议无关）；IOException 精确传播；新 JVM null、替换/置 null/重装、finally 恢复。原始断言保留于 testLegacy。
