# CookieHandler 设计文档（JDK-8 侧）

## 一、API 功能

Cookie 管理的抽象基类与全局注册点：get 构造请求 Cookie 头、put 消费响应 Set-Cookie，getDefault/setDefault 管理全局处理器。

## 二、Java 用户如何使用

`CookieHandler.setDefault(new CookieManager())`；协议栈自动回调 get/put。

## 三、开源代码实际用法调研

HttpURLConnection 会话保持的标准挂点。

## 四、测试用例设计

白盒方法集、抽象性、IOException 声明、子类收发语义、默认处理器生命周期、与 CookieManager 层级。

## 五、契约深化补充（批次 D，探针验证于 JDK-8）

**已深化断言（38 条）覆盖：** 精确 4 方法集（Object 排除）；get/put 抽象且声明 IOException；getDefault/setDefault static；InstantiationException；子类 get/put 原样收发 URI+映射、IOException 精确传播；新 JVM getDefault()==null、替换/置 null/恢复；CookieManager 直接子类关系。原始断言保留于 testLegacy。
