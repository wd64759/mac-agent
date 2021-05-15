package com.cte4.mac.agent.bundle;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.e4.mac.annotation.LatencySPI;
import com.e4.mac.apt.processor.model.AnnotationType;
import com.e4.mac.apt.processor.model.ElementDescriptor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * Annotation parser for latency SLI generation
 */
public class LatencyBundle implements MacBundle {
    private static Logger log = LogManager.getLogger(LatencyBundle.class);
    final static String theAnnotation = LatencySPI.class.getName();
    final String bundleTpl = "Latency.bnl";
    
    private ElementDescriptor element;
    public LatencyBundle(ElementDescriptor element) {
        this.element = element;
    }
    public static Optional<MacBundle> accept(ElementDescriptor element) {
        // only accept annotation on method level
        LatencyBundle latencyBundle = null;
        if (element.getAnnotationType().equals(AnnotationType.METHOD)) {
            if(element.getAnnotations().stream().filter(t->t.getName().equals(theAnnotation)).findAny().isPresent()){
                latencyBundle = new LatencyBundle(element);
                latencyBundle.buildModel();
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
            data.put("data", this.buildModel());
            template.process(data, bytebuf);
            return bytebuf.toString();
        } catch (Exception e) {
            throw new BundleProcessException("fail to generate latency bundle", e);
        }
    }

    /**
     * To convert the general annotation configuration as specific model for template operation
     */
    protected Map<String, Object> buildModel() {
        Map<String, Object> model = new HashMap<>();
        model.put("mtdName", this.element.getName());
        model.put("clzName", this.element.getParent().getName());
        return model;
    }

}
