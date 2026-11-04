# CookieHandler 设计文档

## 一、API 功能

抽象类，处理 HTTP Cookie 的存取(get/put)与策略。

## 二、Java 用户如何使用

用户继承实现 get/put 或使用 CookieManager，通过 CookieHandler.setDefault 注册。

## 三、开源代码实际用法调研

JDK 网络栈在收到/发送请求时回调默认 CookieHandler。

## 四、测试用例设计

getDefault/setDefault 往返；匿名子类实现 get/put 并断言被回调。

## 五、契约深化补充（批次 D，探针验证于 JDK-27）

**已深化断言（39 条）覆盖：**
- 精确公开方法集 = {get, put, getDefault, setDefault}（Object 方法排除后恰 4 个）。
- get/put 抽象、声明 IOException；getDefault/setDefault static；setDefault 返回 void。
- InstantiationException（含反射包装形态）；子类语义：get/put 原样收发 URI 与映射、IOException 精确传播（消息保真）。
- 默认处理器生命周期：新 JVM getDefault()==null、setDefault 可替换/置 null 恢复、finally 恢复原值。
- 层级：CookieManager 直接子类、isAssignableFrom/instanceof 成立。
