package com.cte4.mac.agent.bundle;

import freemarker.template.Configuration;

public interface MacBundle {
    public String buildBundle(Configuration cfg) throws BundleProcessException;
}
