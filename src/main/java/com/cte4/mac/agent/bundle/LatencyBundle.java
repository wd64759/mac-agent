package com.cte4.mac.agent.bundle;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.e4.mac.annotation.LatencySPI;
import com.e4.mac.apt.processor.model.AnnotationType;
import com.e4.mac.apt.processor.model.ElementDescriptor;

import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * Annotation parser for latency SLI generation
 */
public class LatencyBundle extends MacBundle {
    final static String theAnnotation = LatencySPI.class.getName();
    final String bundleTpl = "Latency.bnl";
    final String bundleHelper = "bundle-latency-1.0.jar";
    private Map<String, Object> dataModel;
    
    private ElementDescriptor element;
    public LatencyBundle(ElementDescriptor element) {
        this.element = element;
        this.dataModel = new HashMap<>();
    }
    public static Optional<MacBundle> accept(ElementDescriptor element) {
        // only accept annotation on method level
        LatencyBundle latencyBundle = null;
        if (element.getAnnotationType().equals(AnnotationType.METHOD)) {
            if(element.getAnnotations().stream().filter(t->t.getName().equals(theAnnotation)).findAny().isPresent()){
                latencyBundle = new LatencyBundle(element);
                latencyBundle.buildDataModel();
            }
        }
        return Optional.ofNullable(latencyBundle);
    }

    /**
     * combine the annotation attributes with bundle template
     * @return
     */
    public String buildBundle(Configuration cfg) throws BundleProcessException {
        try (StringWriter bytebuf = new StringWriter()) {
            Template template = cfg.getTemplate(this.bundleTpl);
            Map<String, Object> data = new HashMap<>();
            data.put("data", this.dataModel);
            template.process(data, bytebuf);
            return bytebuf.toString();
        } catch (Exception e) {
            throw new BundleProcessException("fail to generate latency bundle", e);
        }
    }

    /**
     * To convert the general annotation configuration as specific model for template operation
     */
    protected void buildDataModel() {
        String methodWithSign = getSignedMethodName(this.element.getName());
        dataModel.put("mtdName", methodWithSign);
        dataModel.put("clzName", this.element.getParent().getName());
    }

    @Override
    public Optional<String> getBundleHelper() {
        return Optional.of(this.bundleHelper);
    }

}
