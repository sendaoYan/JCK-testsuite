# java.util.ServiceConfigurationError 测试设计案（JDK-8）

> 覆盖目标：完整契约：继承 Error、两构造器、ServiceLoader 失败路径关联。

## 1. API 功能

- 构造器：无参 / (String) / (String, Throwable)（JDK-9+）

## 2. 使用方式

ServiceLoader 迭代指向缺失类 → ServiceConfigurationError（关联断言在 ServiceLoaderTest）。

## 3. 参考来源

- Oracle javadoc `java/util/ServiceConfigurationError`

## 4. 测试用例设计

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 继承 Error（unchecked Error 层次） | testServiceConfigurationError |
| P0 | 构造器消息/cause 读回 | testSceContracts |
| P0 | 契约深化：Error 层次/cause 保持/字符串形式 | testSceContracts |
