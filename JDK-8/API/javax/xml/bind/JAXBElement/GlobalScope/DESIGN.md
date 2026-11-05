# JAXBElement.GlobalScope 设计案（JDK-8 契约测试）

## 1. API 功能
全局作用域哨兵类：final、无方法无字段无接口，作为 JAXBElement 三参构造省略 scope 时的默认 scope 类对象。

## 2. 使用方式
JAXBElement.getScope()==GlobalScope.class ⇔ isGlobalScope()。用户不实例化它，仅以类对象作哨兵比较。

## 3. 参考来源
Oracle javadoc javax.xml.bind.JAXBElement.GlobalScope、JSR 222、本仓库 GlobalScopeTest/JAXBElementTest。

## 4. 测试用例设计 + 易错规范细节
- 结构：final、extends Object、纯标记、单 public 构造、嵌套类名 JAXBElement$GlobalScope。
- 语义：Object 身份 equals（两实例不等）；**不可序列化**（类本身）；JAXBElement 序列化后 scope 仍指向同一类对象（类对象是每 loader 单例）。
- 角色：默认构造的 JAXBElement scope==GlobalScope.class 且 isGlobalScope true；显式 scope 类则否。
- 易错点：比较用类对象（==），从不需要 new GlobalScope()。
