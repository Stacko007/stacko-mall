# 库存管理

## 数据关系

- 每个 `mall_product` 商品对应一条 `catalog_stock` 库存记录。
- 创建商品时在同一事务内初始化数量为 0 的库存记录。
- 商品必须由管理员在库存管理中设置为大于 0 的数量后，C 端才能下单。
- 库存列表接口同时返回 `productName`，管理端以商品名称为主、商品 ID 为辅助进行展示。

## 历史数据兼容

旧版本创建商品时没有初始化库存，因此可能存在只有 `mall_product`、没有 `catalog_stock` 的商品。管理端查询库存列表时会检查当前租户的商品，并为缺失的商品补建数量为 0 的库存记录。

修复后首次进入库存管理即可看到历史商品。此时库存为 0，需要点击“设置库存”录入实际数量；否则下单会返回库存不足。

如需直接通过 SQL 检查缺失记录：

```sql
SELECT p.id, p.tenant_id, p.name
FROM mall_product p
LEFT JOIN catalog_stock s
  ON s.product_id = p.id AND s.tenant_id = p.tenant_id
WHERE s.product_id IS NULL;
```
