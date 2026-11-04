# CookiePolicy 设计文档（JDK-8 侧）

## 一、API 功能

Cookie 准入策略接口：shouldAccept(URI, HttpCookie) 决定是否接受；内建 ACCEPT_ALL/ACCEPT_NONE/ACCEPT_ORIGINAL_SERVER 三策略。

## 二、Java 用户如何使用

传给 CookieManager 构造器或 setCookiePolicy；或实现单方法接口定制。

## 三、开源代码实际用法调研

浏览器隐私策略（拦截第三方 cookie）即同型抽象。

## 四、测试用例设计

三常量行为矩阵（含 null 参数）、自定义实现、与 CookieManager 集成的调用计数。

## 五、契约深化补充（批次 D，探针验证于 JDK-8）

**已深化断言（53 条）覆盖：** 白盒（三常量 static final、单抽象方法）；ACCEPT_ALL/ACCEPT_NONE 全 null 矩阵；ACCEPT_ORIGINAL_SERVER 20 组矩阵（大小写不敏感、单级子域匹配、深层子域拒绝、cookie domain=null 视为原服务器）；自定义实现 + CookieManager 集成（每条 put cookie 恰调用一次策略，2 次手动 + N 次 put）。匿名内部类（Java-8 语法）。

**易错细节：** ACCEPT_ORIGINAL_SERVER 深层子域拒绝与 HttpCookie.domainMatches 同源；policy==null 时 CookieManager 保持原策略。
