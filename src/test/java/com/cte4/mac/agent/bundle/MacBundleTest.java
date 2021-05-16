package com.cte4.mac.agent.bundle;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import freemarker.template.Configuration;

public class MacBundleTest {
    MacBundle bundle;

    MacBundleTest() {
        bundle = new MacBundle(){
            @Override
            public String buildBundle(Configuration cfg) throws BundleProcessException {
                return null;
            }
            @Override
            public Optional<String> getBundleHelper() {
                return null;
            }
            
        };
    }

    @Test
    public void getSignedMethodName() {
        Function<String, String> func = bundle::getSignedMethodName;
        assertEquals("T1(String, Integer)", func.apply("Class$T1(String, Integer)"));
        assertEquals("T1(String, Integer, Com.ParamClass)", func.apply("Package.Class$T1(String, Integer, Com.ParamClass)"));
        assertEquals("T1()", func.apply("Package.Class$T1()"));
    }
}
