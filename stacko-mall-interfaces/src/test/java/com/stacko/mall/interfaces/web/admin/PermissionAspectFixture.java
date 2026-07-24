package com.stacko.mall.interfaces.web.admin;

import com.stacko.mall.interfaces.web.security.RequiresPermission;

public class PermissionAspectFixture {
    @RequiresPermission("mall:product:list")
    public String list() {
        return "ok";
    }

    public String notConfigured() {
        return "not-configured";
    }
}
