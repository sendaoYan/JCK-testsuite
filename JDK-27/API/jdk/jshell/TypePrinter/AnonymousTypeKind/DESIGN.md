# AnonymousTypeKind 设计案（内部）

## 1. API 功能
{SUPER, DECLARE, DISPLAY}：匿名类作超类/声明处/展示处的三种处理。

## 2-3. 略

## 4. 测试设计 + 易错
枚举顺序与 valueOf 语义（经反射调用需包 InvocationTargetException）。
