# java.util.Locale.Category 测试设计案（JDK-8）

> 覆盖目标：枚举完整覆盖 + setDefault(Category)/getDefault(Category) 分类独立。

## 1. API 功能

- 枚举常量：DISPLAY / FORMAT
- Locale.setDefault(Locale.Category, Locale) 与 getDefault(Locale.Category)

## 2. 使用方式

getDefault(Category.DISPLAY)/(FORMAT) 各自独立 setDefault（finally 还原默认，防污染）。

## 3. 参考来源

- Oracle Java SE 8 javadoc `java/util/Locale.Category`

## 4. 测试用例设计

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 枚举常量集 values()/valueOf 精确 | testCategory |
| P0 | DISPLAY/FORMAT 默认可读且分类设置互不影响 | testCategoryContracts |
| P0 | 契约深化：完整常量表/类别默认区独立生效 | testCategoryContracts |
