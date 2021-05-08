package com.cte4.mac.agent;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AgentApp {

    static Logger log = LogManager.getLogger(AgentApp.class);

    static final String BUNDLES_LOC = System.getProperty("bundles.dir", "/mnt/d/code/e4/bundles");
    static final String BUNDLE_POSTFIX = ".bnl";
    static final String ACTION_ENABLE = "enable";
    static final String ACTION_DISABLE = "disable";

    public static void main(String[] args) {
        if (args.length < 2) {
            log.info("prompt: pid action [file name]");
            log.info("e.g. 1298 enable");
            log.info("     1298 disable");
            log.info("     1298 disable common.bnl");
            return;
        }
        List<String> argsList = Arrays.asList(args);
        String processID = args[0];
        String action = args[1];
        List<String> bundles = argsList.size() > 2 ? argsList.subList(3, argsList.size()) : new ArrayList<>();

        // do some validation
        File bundleFolder = new File(BUNDLES_LOC);
        List<File> bundleFiles = Arrays.asList(bundleFolder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(BUNDLE_POSTFIX);
            }
        }));
        if (bundles.size() > 0) {
            bundleFiles = bundleFiles.stream().filter((f) -> bundles.contains(f.getName()))
                    .collect(Collectors.toList());
        }
        if (bundleFiles.size() == 0) {
            log.warn("not found any proper bundle files");
            return;
        }

        // apply changes
        BundleInstaller installer = BundleInstaller.buildInstaller(processID);
        switch (action.toLowerCase()) {
            case ACTION_ENABLE:
                installer.applyBundles(bundleFiles);
                break;
            case ACTION_DISABLE:
                installer.detachBundle(bundleFiles);
                break;
            default:
                log.error(String.format("invalud action, only accept %s and %s", ACTION_ENABLE, ACTION_DISABLE));
                break;
        }
    }
}
