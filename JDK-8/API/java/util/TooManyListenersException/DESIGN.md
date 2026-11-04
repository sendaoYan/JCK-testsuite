# java.util.TooManyListenersException 测试设计案（JDK-8）

> 覆盖目标：完整契约：checked 异常、两构造器、单播语义场景。

## 1. API 功能

- 构造器：无参 / (String)；继承 Exception（checked）

## 2. 使用方式

模拟单播注册第二监听器抛出；编译期强制捕获断言。

## 3. 参考来源

- Oracle Java SE 8 javadoc `java/util/TooManyListenersException`（单播事件源约定）

## 4. 测试用例设计

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | checked 编译强制捕获 | testTooManyContracts |
| P0 | 构造器消息读回 | testTooManyListenersException |
| P0 | 契约深化：受检异常语义/cause 缺省 | testTooManyContracts |
