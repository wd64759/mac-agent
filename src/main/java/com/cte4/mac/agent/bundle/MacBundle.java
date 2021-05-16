package com.cte4.mac.agent.bundle;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import freemarker.template.Configuration;

public abstract class MacBundle {
    public abstract String buildBundle(Configuration cfg) throws BundleProcessException;
    public abstract Optional<String> getBundleHelper();

    /**
     * The method can be moved up
     * @param fullPath
     * @return
     */
    protected String getSignedMethodName(String fullPath) {
        Pattern p = Pattern.compile(".*\\$(.*\\(.*\\))");
        Matcher m = p.matcher(fullPath);
        if(m.find()) {
            return m.group(1);
        }
        return fullPath;
    }
}
