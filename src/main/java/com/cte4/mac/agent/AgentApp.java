package com.cte4.mac.agent;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import lombok.extern.log4j.Log4j2;

@SpringBootApplication
@Log4j2
public class AgentApp {

    static final String BUNDLE_POSTFIX = ".bnl";
    static final String ACTION_ENABLE = "enable";
    static final String ACTION_DISABLE = "disable";

    public static void main(String[] args) {
        SpringApplication.run(AgentApp.class, args);
    }

    @Value("exposer.bundle.loc")
    String bundleFileLoc;

    @Bean
    CommandLineRunner commander() {
        return new CommandLineRunner() {
            @Override
            public void run(String... args) throws Exception {
                if (args.length < 2) {
                    log.info("prompt: pid action [file name]");
                    log.info("e.g. 1298 enable");
                    log.info("     1298 disable");
                    log.info("     1298 disable common.bnl");
                }
                List<String> argsList = Arrays.asList(args);
                String processID = args[0];
                String action = args[1];
                List<String> bundles = argsList.size() > 2 ? argsList.subList(3, argsList.size()) : new ArrayList();

                // do some validation
                File bundleFolder = new File(bundleFileLoc);
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
        };
    }
}