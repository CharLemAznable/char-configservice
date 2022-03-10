package com.github.charlemaznable.configservice.test.common;

import lombok.Data;
import org.n3r.diamond.client.cache.ParamsAppliable;

@Data
public class TestDefaultContentBean implements ParamsAppliable {

    private String name;

    @Override
    public void applyParams(String[] strings) {
        if (strings.length > 0) this.name = strings[0];
    }
}
