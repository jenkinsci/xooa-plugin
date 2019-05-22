package io.jenkins.plugins.xooa;

import hudson.model.Action;
import hudson.model.Run;
import jenkins.model.RunAction2;

public class AppUpgradeAction implements RunAction2 {
	
	private String appId, id, name, description, appVersion, network, language, createdAt, updatedAt;
	
	private transient Run run; 

    @Override
    public void onAttached(Run<?, ?> run) {
        this.run = run; 
    }

    @Override
    public void onLoad(Run<?, ?> run) {
        this.run = run; 
    }

    public Run getRun() { 
        return run;
    }
    
    public AppUpgradeAction(String appId, String id, String name, String description, String appVersion, String network, String language, String createdAt,String updatedAt) {
        this.appId = appId;
        this.id = id;
        this.name= name;
        this.description =description;
        this.appVersion = appVersion;
        this.network= network;
        this.language=  language;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    

//    public AppUpgradeAction(String appId2, String id2, String name2, String description2, String appVersion2,
//			String network2, String language2, String createdAt2, String updatedAt2) {
//		// TODO Auto-generated constructor stub
//	}

	public String getAppId() {
        return appId;
    }	
    
    public String getId() {
        return id;
    }	
    
    public String getName() {
        return name;
    }	
    
    public String getDescription() {
        return description;
    }	
    
    public String getAppVersion() {
        return appVersion;
    }	
    
    public String getNetwork() {
        return network;
    }	    
    
    public String getLanguage() {
        return language;
    }	
    
    public String getCreatedAt() {
        return createdAt;
    }	
    
    public String getUpdatedAt() {
        return updatedAt;
    }	

    @Override
    public String getIconFileName() {
        return "document.png"; 
    }

    @Override
    public String getDisplayName() {
        return "App Upgrade Info"; 
    }

    @Override
    public String getUrlName() {
        return "upgradeinfo"; 
    }
}