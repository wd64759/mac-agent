package com.cte4.mac.agent.bundle;

import java.util.Optional;

import com.e4.mac.apt.processor.model.ElementDescriptor;

import freemarker.template.Configuration;

public class AvailabilityBundle extends MacBundle {

    public static Optional<MacBundle> accept(ElementDescriptor element) {
        // TODO
        return Optional.ofNullable(null);
    }

    @Override
    public String buildBundle(Configuration cfg) throws BundleProcessException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Optional<String> getBundleHelper() {
        return null;
    }
    
}
