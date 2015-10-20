package com.bwssystems.restfulharmony;

import static java.lang.String.format;
import static spark.SparkBase.port;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.restfulharmony.api.DevModeResponse;
import com.bwssystems.restfulharmony.api.HarmonyRest;
import com.google.inject.Guice;
import com.google.inject.Injector;

import net.whistlingfish.harmony.ActivityChangeListener;
import net.whistlingfish.harmony.HarmonyClient;
import net.whistlingfish.harmony.HarmonyClientModule;
import net.whistlingfish.harmony.config.Activity;

public class HarmonyRestServer {
    @Inject
    private HarmonyClient harmonyClient;
    
    private HarmonyRest harmonyApi;
    
    private DevModeResponse devResponse;
    
    private static Boolean devMode;

    public static void main(String[] args) throws Exception {
    	devMode = Boolean.parseBoolean(System.getProperty("dev.mode", "false"));
    	Injector injector = null;
    	if(!devMode)
            injector = Guice.createInjector(new HarmonyClientModule());
        HarmonyRestServer mainObject = new HarmonyRestServer();
    	if(!devMode)
    		injector.injectMembers(mainObject);
        System.exit(mainObject.execute(args));
    }

    public int execute(String[] args) throws Exception {
        Logger log = LoggerFactory.getLogger(HarmonyRestServer.class);
        if(devMode)
        {
        	harmonyClient = null;
        	devResponse = new DevModeResponse();
        }
        else {
        	devResponse = null;
	        harmonyClient.addListener(new ActivityChangeListener() {
	            @Override
	            public void activityStarted(Activity activity) {
	                log.info(format("activity changed: [%d] %s", activity.getId(), activity.getLabel()));
	            }
	        });
	        harmonyClient.connect(args[0], args[1], args[2]);
        }
        port(Integer.valueOf(System.getProperty("server.port", "8080")));
        Boolean noopCalls = Boolean.parseBoolean(System.getProperty("noop.calls", "false"));
        harmonyApi = new HarmonyRest(harmonyClient, noopCalls, devResponse);
        harmonyApi.setupServer();
        log.info("Harmony v0.1.3 rest server running....");
        while(true)
        {
        	//no op
        	Thread.sleep(100000);
        }
    }
}
