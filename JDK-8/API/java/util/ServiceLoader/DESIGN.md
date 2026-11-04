# java.util.ServiceLoader 测试设计案（JDK-8）

> 覆盖目标：完整契约（Java 8 面）。用嵌套接口（无 META-INF/services 注册）验证空迭代；
> 不依赖外部提供者。

## 1. API 功能

- **工厂**：`load(Class)`、`load(Class, ClassLoader)`、`loadInstalled(Class)`。
- **迭代**：`iterator()`、`reload()`、`toString()`。

## 2. 使用方式

`ServiceLoader.load(Svc.class)` → `iterator()` 遍历提供者。

## 3. 参考来源

- Oracle Java SE 8 javadoc `java/util/ServiceLoader`。
- `load(null)`/`load(null, cl)` → NPE。

## 4. 测试用例设计

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 无注册服务空迭代 + reload | testServiceLoader |
| P0 | null NPE + load(Class,ClassLoader) + loadInstalled | testNullAndFactories |
| P1 | toString 非空 | testToString |

## 关键规范细节

- `load(null)` → NPE。
- 无提供者时 `iterator().hasNext()` 为 false。

## 注意事项

不依赖外部服务资源；无全局副作用；headless-safe 不加 `@key`。
