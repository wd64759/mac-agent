package com.cte4.mac.agent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.cte4.mac.agent.bundle.BundleProcessException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AgentApp {

    static Logger log = LogManager.getLogger(AgentApp.class);

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

        // apply changes
        try {
            BundleInstaller installer = BundleInstaller.buildInstaller(processID);
            BundleGen bundleRepo = BundleGen.getInstance();
            bundleRepo.process();
            List<String> bundleCntList = bundleRepo.getBundleContents();

            // reserved for handle specific bundle to apply
            List<String> specBundles = argsList.size() > 2 ? argsList.subList(3, argsList.size()) : new ArrayList<>();
            if (specBundles.size() > 0) {
                // TODO: rule name design example - SLI name + Class name + method with script
                // .substring(0, 50)
                // bundles = specBundles.stream().filter((f) -> bundles.contains(f.getName()))
                // .collect(Collectors.toList());
            }
            if (bundleCntList.size() == 0) {
                log.warn("not found any proper bundle files");
                return;
            }
            bundleRepo.getBundleHelpers().stream().forEach(helper -> {
                log.info("install helper - " + helper);
                installer.applyBundleHelper(helper);
            });
            switch (action.toLowerCase()) {
                case ACTION_ENABLE:
                    installer.applyBundleCnt(bundleCntList);
                    break;
                case ACTION_DISABLE:
                    installer.detachBundle(bundleCntList);
                    break;
                default:
                    log.error(String.format("invalud action, only accept %s and %s", ACTION_ENABLE, ACTION_DISABLE));
                    break;
            }

        } catch (BundleProcessException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
