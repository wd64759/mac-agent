package com.cte4.mac.agent;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.net.ServerSocketFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.byteman.agent.install.Install;
import org.jboss.byteman.agent.submit.Submit;

public class BundleInstaller {

    static Logger log = LogManager.getLogger(BundleInstaller.class);
    private final Random random = new Random(System.nanoTime());
    private int port;
    private String processId;
    private String host;
    private boolean addtoBoot;
    private Submit installer;

    private BundleInstaller(String pid) {
        this.processId = pid;
    }

    private BundleInstaller init() {
        try {
            if (!Install.isAgentAttached(this.processId)) {
                this.findAvaliablePort();
                Install.install(this.processId, this.addtoBoot, this.host, this.port, this.props());
                installer = new Submit(this.host, this.port);
            }
        } catch (Exception e) {
            log.error("fail to attach agent", e);
        }
        return this;
    }

    public static BundleInstaller buildInstaller(String pid) {
        return new BundleInstaller(pid).init();
    }

    /**
     * To install bundles 1by1
     * @param bundles
     */
    public void applyBundles(List<File> bundles) {
        if(this.installer == null) {
            log.error("..exiting for bundle installer not initiated");
            return;
        }
        for(File bundleFile: bundles) {
            String bundleName = bundleFile.getName();
            try {
                String bundleScript = this.loadBundleScript(bundleFile);
                ByteArrayInputStream is = new ByteArrayInputStream(bundleScript.getBytes());
                String result = this.installer.addRulesFromResources(Arrays.asList(is));
                log.info(String.format("bundle(%s) is installed successfully, status:%s", bundleName, result));
            } catch (Exception e) {
                log.error(String.format("bundle(%s) failed to install", bundleName), e);
            }
        }
    }

    public void detachBundle(List<File> bundles) {
    }

    private String loadBundleScript(File bundleFile) throws IOException {
        String fLoc = bundleFile.getAbsolutePath();
        try {
            String script = Files.readString(Paths.get(fLoc));
            return script;
        } catch (IOException e) {
            log.error(String.format("fail to load script from bundle file:%s", fLoc));
            throw e;
        }
    }

    /**
     * to find avaiable port
     * 
     * @return
     */
    protected int findAvaliablePort() {
        this.port = Submit.DEFAULT_PORT;
        int retryTime = 10;
        while (retryTime-- > 0 && !isPortAvailable(this.port)) {
            this.port += random.nextInt(10);
        }
        return port;
    }

        /**
     * to check if TCP port is available
     * 
     * @param port
     * @return
     */
    protected boolean isPortAvailable(int port) {
        try {
            ServerSocket serverSocket = ServerSocketFactory.getDefault().createServerSocket(port, 1,
                    InetAddress.getByName("localhost"));
            serverSocket.close();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private String[] props() {
        return new String[] { 
            "org.jboss.byteman.dump.generated.classes=true",
            "org.jboss.byteman.dump.generated.classes.directory=/tmp/dump",
            "org.jboss.byteman.mac.agentport=" + this.port, 
            "org.jboss.byteman.mac.pid=" + this.processId,
            "org.jboss.byteman.debug", 
            "org.jboss.byteman.verbose" };
    }
}
