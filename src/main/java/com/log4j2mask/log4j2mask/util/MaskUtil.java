package com.log4j2mask.log4j2mask.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MaskUtil {

    @Value("log.mask.configs")
    private String patternString;

    public String jsonMask(String json) {
        System.out.println("patternString: " +  patternString);
        return json;
    }
}
