# org.omg.CosNaming.NamingContextExt

## API 功能
NamingContext 的字符串/URL 扩展：to_string/to_name/to_url/resolve_str。
## 使用方式
接口（继承全部 NamingContext 操作）。
## 参考来源
JDK-8 rt.jar；实测反射。
## 测试用例设计 + 易错细节
- extends NamingContext（继承层级）；4 个扩展方法存在；to_name 返回类型与 InvalidName checked 异常精确。
