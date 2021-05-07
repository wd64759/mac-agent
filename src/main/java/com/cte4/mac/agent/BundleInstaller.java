package com.cte4.mac.agent;

import java.io.File;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.List;
import java.util.Random;

import javax.net.ServerSocketFactory;

import org.jboss.byteman.agent.install.Install;
import org.jboss.byteman.agent.submit.Submit;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class BundleInstaller {

    private final Random random = new Random(System.nanoTime());
    private int port;
    private String processId;
    private String host;
    private boolean addtoBoot;

    private BundleInstaller(String pid) {
        this.processId = pid;
    }

    private BundleInstaller init() {
        try {
            if (!Install.isAgentAttached(this.processId)) {
                this.findAvaliablePort();
                Install.install(this.processId, this.addtoBoot, this.host, this.port, this.props());
            }
        } catch (Exception e) {
            log.error("fail to attach agent", e);
        }
        return this;
    }

    public static BundleInstaller buildInstaller(String pid) {
        return new BundleInstaller(pid).init();
    }

    public void applyBundles(List<File> bundles) {
        
    }

    public void detachBundle(List<File> bundles) {

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
