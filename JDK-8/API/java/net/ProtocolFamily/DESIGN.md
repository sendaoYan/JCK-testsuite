# java.net.ProtocolFamily 测试设计案（JDK-8）

> 批次 F 深化：单方法接口结构、自定义实现、平台实现多态。

## 1. API 功能

- 接口 `ProtocolFamily`：单方法 `name()`（String）。
- 平台实现：`StandardProtocolFamily`（JDK-8 恰好 INET/INET6 两常量）。

## 2. 测试用例设计（ProtocolFamilyTest.java，30 断言）

- 白盒：public interface、无字段、恰好 1 个声明方法 name()（abstract、返回 String、无参）。
- 自定义实现：任意名称（含 null/空串）透传；同名的两个实现是不同对象。
- 平台实现：INET/INET6 经接口引用调用 name() 精确串；`values().length==2`（JDK-8）。
- 多态混排：标准 + 自定义统一按接口迭代。

## 易错规范细节（已锁入断言）

- 接口不校验 name 非空/非空串；自定义实现可与标准常量同名（字符串复用合法）。

## JDK-8 vs JDK-27 差异

- JDK-27（32 断言）：三常量（JDK 16 增 UNIX）。
