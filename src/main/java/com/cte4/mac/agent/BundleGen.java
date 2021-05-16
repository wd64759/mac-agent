package com.cte4.mac.agent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import com.cte4.mac.agent.bundle.AvailabilityBundle;
import com.cte4.mac.agent.bundle.BundleProcessException;
import com.cte4.mac.agent.bundle.LatencyBundle;
import com.cte4.mac.agent.bundle.MacBundle;
import com.e4.mac.apt.processor.RuleCfgGenerator;
import com.e4.mac.apt.processor.model.ElementDescriptor;
import com.e4.mac.apt.processor.model.ModuleDescriptor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;

public class BundleGen {
    static BundleGen instance;
    static Logger log = LogManager.getLogger(BundleGen.class);
    static final String MAC_CFG_DIR = System.getProperty("mac.files.dir", System.getProperty("java.io.tmpdir", "/tmp"));
    static final String BUNDLE_HELPER_DIR = System.getProperty("mac.helper.dir", "/mnt/d/code/e4/mac-exposer/bundle-latency/build/libs");
    
    private Configuration templateCfg;
    private List<Function<ElementDescriptor, Optional<MacBundle>>> bundles = new ArrayList<>();
    private List<String> bundleContents = new ArrayList<>();
    private Set<String> bundleHelpers = new HashSet<>();

    private BundleGen() {
    }

    public static BundleGen getInstance() throws BundleProcessException {
        if (instance == null) {
            synchronized (BundleGen.class) {
                instance = new BundleGen();
                instance.init();
            }
        }
        return instance;
    }

    public void init() throws BundleProcessException {
        templateCfg = new Configuration(Configuration.VERSION_2_3_22);
        try {
            // cfg.setDirectoryForTemplateLoading(getTemplateDir());
            templateCfg.setClassForTemplateLoading(BundleGen.class, "/bundles");
            templateCfg.setDefaultEncoding("UTF-8");
            templateCfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
            // add all bundes
            registBundles();
        } catch (Exception e) {
            throw new BundleProcessException("fail to build latency bundler", e);
        }
    }

    /**
     * regist all specific bundle generators
     */
    protected void registBundles() {
        bundles.add(LatencyBundle::accept);
        bundles.add(AvailabilityBundle::accept);
    }

    /**
     * To run all bundle processor against the annotation tree
     * @param annotationElem
     */
    public void buildBundles(ElementDescriptor annotationElem) {
        for(Function<ElementDescriptor, Optional<MacBundle>> bundleHandler: this.bundles) {
            bundleHandler.apply(annotationElem).ifPresent(t->{
                try {
                    String bundleCnt = t.buildBundle(this.templateCfg);
                    bundleContents.add(bundleCnt);
                    t.getBundleHelper().ifPresent(helper->this.bundleHelpers.add(BUNDLE_HELPER_DIR + File.separator + helper));
                } catch (BundleProcessException e) {
                    log.error("fail to run build bundle", e);
                }
            });
        }
        // recursion for all leaves
        annotationElem.getChildren().forEach(t->this.buildBundles(t));
    }

    /**
     * main entry
     * @throws IOException
     */
    public void process() throws IOException {
        String cfgFile = Files.readString(Paths.get(MAC_CFG_DIR + File.separator + "mac/rule_cfg.json"));
        ModuleDescriptor md = RuleCfgGenerator.fromCfg(cfgFile);
        this.buildBundles(md);
    }

    public List<String> getBundleContents() {
        return bundleContents;
    }

    public Set<String> getBundleHelpers() {
        return bundleHelpers;
    }

    public static void main(String[] args) throws Exception {
        BundleGen bg = BundleGen.getInstance();
        bg.process();
        bg.bundleContents.forEach(System.out::println);
        log.info(bg.bundleHelpers);
    }
}
