# java.util.MissingResourceException 测试设计案（JDK-8）

> 覆盖目标：完整契约：三参公共构造 + 四参包内构造（反射）、getClassName/getKey/getArguments、ResourceBundle 真实触发。

## 1. API 功能

- 公共构造器：(String, String className, String key)；包内 (…, Object[] args)（反射探测）
- 读回：getClassName()/getKey()；getArguments()（JDK-9+ 存在侧反射）

## 2. 使用方式

ResourceBundle.getBundle("不存在") → MRE（className/key 精确）；直接构造断言读回。

## 3. 参考来源

- Oracle Java SE 8 javadoc `java/util/MissingResourceException`
- JDK 源码 ResourceBundle 抛出点

## 4. 测试用例设计

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | getBundle 未知基名触发 + className/key 读回 | testMissingResourceException |
| P0 | 三参构造器读回 | testMissingResourceContracts |
| P1 | 包内四参构造（反射；27 侧模块封装断言） | testMissingResourceContracts |
| P0 | 契约深化：层次/构造器矩阵/getter 保持 | testMissingResourceContracts |
