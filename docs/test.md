# stacko-mall项目测试说明文档
  整个项目的X-Tenant-ID为 stacko-mall
## 1.stacko-mall 管理后台
租户管理员账号:stacko-mall-admin 密码:123456
订单管理员账号:stacko-mall-order 密码:123456

## 2.stacko-mall 用户端(C端)
注册用户:stacko001,密码123456

## 3.管理后台和用户端登录接口
/api/auth/login
接口返回数据格式示例如下:
```
{
    "success": true,
    "message": "ok",
    "data": {
        "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOjksInVzZXJuYW1lIjoic3RhY2tvLW1hbGwtb3JkZXItbWFuYWdlIiwidGVuYW50SWQiOiJzdGFja28tbWFsbCIsInBob25lIjoiMTM2MDM0NTU1NTUiLCJlbWFpbCI6InRlc3RAZ21haWwuY29tIiwicm9sZXMiOlsic3RhY2tvLW1hbGwtb3JkZXIiXSwicGVybWlzc2lvbnMiOlsibWFsbDpvcmRlcjpyZWFkIiwibWFsbDpvcmRlcjpjcmVhdGUiLCJtYWxsOm9yZGVyOnNoaXAiLCJtYWxsOnN0b2NrOnJlYWQiLCJtYWxsOnByb2R1Y3Q6dXBkYXRlIiwibWFsbDpzdG9jazpsaXN0IiwibWFsbDpwcm9kdWN0OmNyZWF0ZSIsIm1hbGw6Y2FydDphZGRwcm9kdWN0IiwibWFsbDpzdG9jazpzZXQiLCJ1c2VyOnJlYWQiLCJtYWxsOm9yZGVyOmxpc3QiLCJhY2w6Y2hlY2siLCJtYWxsOm9yZGVyOmVkaXQiLCJtYWxsOmNhcnQ6ZGVscHJvZHVjdCIsIm1hbGw6cHJvZHVjdDpsaXN0IiwibWFsbDpjYXJ0OnVzZSIsIm1hbGw6c3RvY2s6YWRqdXN0IiwibWFsbDpwcm9kdWN0OnJlYWQiXSwiZXhwIjoxNzY5OTU1NjQwfQ.tj390Qu1PDXquxe-u0CiXMJ8WpgaAESLS-CaQeKJUdQ",
        "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOjksInVzZXJuYW1lIjoic3RhY2tvLW1hbGwtb3JkZXItbWFuYWdlIiwidGVuYW50SWQiOiJzdGFja28tbWFsbCIsInBob25lIjoiMTM2MDM0NTU1NTUiLCJlbWFpbCI6InRlc3RAZ21haWwuY29tIiwicm9sZXMiOlsic3RhY2tvLW1hbGwtb3JkZXIiXSwicGVybWlzc2lvbnMiOlsibWFsbDpvcmRlcjpyZWFkIiwibWFsbDpvcmRlcjpjcmVhdGUiLCJtYWxsOm9yZGVyOnNoaXAiLCJtYWxsOnN0b2NrOnJlYWQiLCJtYWxsOnByb2R1Y3Q6dXBkYXRlIiwibWFsbDpzdG9jazpsaXN0IiwibWFsbDpwcm9kdWN0OmNyZWF0ZSIsIm1hbGw6Y2FydDphZGRwcm9kdWN0IiwibWFsbDpzdG9jazpzZXQiLCJ1c2VyOnJlYWQiLCJtYWxsOm9yZGVyOmxpc3QiLCJhY2w6Y2hlY2siLCJtYWxsOm9yZGVyOmVkaXQiLCJtYWxsOmNhcnQ6ZGVscHJvZHVjdCIsIm1hbGw6cHJvZHVjdDpsaXN0IiwibWFsbDpjYXJ0OnVzZSIsIm1hbGw6c3RvY2s6YWRqdXN0IiwibWFsbDpwcm9kdWN0OnJlYWQiXSwiZXhwIjoxNzY5OTU1NjQwfQ.tj390Qu1PDXquxe-u0CiXMJ8WpgaAESLS-CaQeKJUdQ",
        "expiresAt": "2026-02-01T14:20:40.852284Z",
        "refreshExpiresAt": "2026-02-01T18:20:40.852284Z",
        "userId": 9,
        "tenantId": "stacko-mall"
    }
}
```
使用token作为后续接口请求凭证,接口鉴权格式是JWT Bearer。

## 4.用户权限查看接口
/api/users/me
返回数据格式包含用户权限、角色信息,示例如下:
```
{
    "success": true,
    "message": null,
    "data": {
        "id": 9,
        "username": "stacko-mall-order-manage",
        "phone": "13603455555",
        "email": "test@gmail.com",
        "status": "ACTIVE",
        "statusLabel": null,
        "tenantId": "stacko-mall",
        "createdAt": null,
        "createdAtLabel": null,
        "roles": [
            "stacko-mall-order"
        ],
        "permissions": [
            "mall:order:create",
            "mall:product:create",
            "mall:stock:list",
            "mall:product:list",
            "mall:stock:set",
            "mall:order:read",
            "mall:stock:adjust",
            "mall:cart:delproduct",
            "mall:cart:use",
            "mall:order:ship",
            "user:read",
            "mall:order:edit",
            "mall:stock:read",
            "mall:product:read",
            "mall:order:list",
            "mall:cart:addproduct",
            "mall:product:update",
            "acl:check"
        ]
    }
}
```