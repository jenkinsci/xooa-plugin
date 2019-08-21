package io.jenkins.plugins.xooa;

import hudson.Launcher;
import hudson.Extension;
import hudson.FilePath;
import hudson.util.FormValidation;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import org.zeroturnaround.zip.ZipUtil;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.servlet.ServletException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;

public class AppUpgradeBuilder extends Builder implements SimpleBuildStep {

	private final String name;
	private final String appId;
	private String deploymentToken;
	private String endPoint = "https://api.xooa.com/deployment/v1/apps/";

	@DataBoundConstructor
	public AppUpgradeBuilder(String name, String appId) {
		this.name = name;
		this.appId = appId;
	}

	public String getName() {
		return name;
	}

	public String getAppId() {
		return appId;
	}

	@Override
	public void perform(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener)
			throws InterruptedException, IOException {
		GlobConfig config = new GlobConfig();
		deploymentToken = config.getDeploymentToken();

		CloseableHttpClient httpClient = HttpClients.createDefault();

		HttpGet validateAppIdRequest = new HttpGet(endPoint + appId);
		validateAppIdRequest.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + deploymentToken);
		HttpResponse appDetailsResp = httpClient.execute(validateAppIdRequest);
		int appDetailsStatusCode = appDetailsResp.getStatusLine().getStatusCode();
		System.out.println("App details status code: " + appDetailsStatusCode);
		listener.getLogger().println("App details status code: " + appDetailsStatusCode);
		if (appDetailsStatusCode != 200) {
			if (appDetailsStatusCode == 401) {
				throw new RuntimeException("Unauthorized. Please provide valid deployment token.");
			}
			throw new RuntimeException("Invalid app. Please provide valid App Id.");
		}

		if (new FilePath(new File(workspace.toString() + "/tmp.zip")).exists()) {
			new FilePath(new File(workspace.toString() + "/tmp.zip")).delete();
			System.out.println("Deleted zip from previous build.");
			listener.getLogger().println("Deleted zip from previous build.");
		}
		if (new FilePath(new File(workspace.toString() + "/" + name)).exists()) {
			ZipUtil.pack(new File(workspace.toString() + "/" + name), new File(workspace.toString() + "/tmp.zip"));
		} else {
			throw new RuntimeException("Smart contract directory doesn't exists. Please provide valid directory.");
		}

		System.out.println("Created new temporary zip file.");
		listener.getLogger().println("Created new temporary zip file.");

		HttpPost uploadFile = new HttpPost(endPoint + appId);
		uploadFile.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + deploymentToken);
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		FileInputStream fin = null;
		try {
			File f = new File(workspace.toString() + "/tmp.zip");
			fin = new FileInputStream(f);
			builder.addBinaryBody("smart-contract", fin, ContentType.APPLICATION_OCTET_STREAM, f.getName());
			HttpEntity multipart = builder.build();
			uploadFile.setEntity(multipart);
			CloseableHttpResponse response = httpClient.execute(uploadFile);
			HttpEntity entity = response.getEntity();
			String json = EntityUtils.toString(entity, StandardCharsets.UTF_8);

			JSONObject o = new JSONObject(json);
			listener.getLogger().println("Upgrade api result object: " + json);
			System.out.println("Upgrade api result object: " + json);
			fin.close();
			if (response.getStatusLine().getStatusCode() == 202) {
				String queryUrl = o.get("resultURL").toString();
				int statusCode = 202;
				int i = 0;
				while (i < 100 && statusCode == 202) {
					System.out.println("Polled times: " + i);
					i++;
					HttpGet getRequest = new HttpGet(queryUrl);
					getRequest.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + deploymentToken);
					HttpResponse resp = httpClient.execute(getRequest);
					statusCode = resp.getStatusLine().getStatusCode();
					System.out.println("Polling API status code: " + statusCode);
					listener.getLogger().println("Polling API status code: " + statusCode);
					HttpEntity httpEntity = resp.getEntity();
					String apiOutput = EntityUtils.toString(httpEntity);
					System.out.println(apiOutput);
					listener.getLogger().println("Upgrade step details: " + apiOutput);
					if (statusCode == 200) {
						JSONObject resultRespJson = new JSONObject(apiOutput);
						if (resultRespJson.has("error")) {
							throw new RuntimeException(
									"Deployment failed due to: " + resultRespJson.get("error").toString());
						} else {
							listener.getLogger().println("App upgraded successfully: " + apiOutput);
							Object result = resultRespJson.get("result");
							JSONObject r = new JSONObject(result.toString());
							System.out.println(result);
							String Id = r.get("Id").toString();
							String Name = r.get("Name").toString();
							String Description = r.get("Description").toString();
							String AppVersion = r.get("AppVersion").toString();
							String Network = r.get("Network").toString();
							String Language = r.get("Language").toString();
							String CreatedAt = r.get("CreatedAt").toString();
							String UpdatedAt = r.get("UpdatedAt").toString();
							run.addAction(new AppUpgradeAction(appId, Id, Name, Description, AppVersion, Network,
									Language, CreatedAt, UpdatedAt));
							break;
						}
					}
					Thread.sleep(2500);
				}

				if (statusCode != 200 && statusCode != 202) {
					if (fin != null) {
						fin.close();
					}
					throw new RuntimeException("Failed with HTTP error code : " + statusCode);
				}

			} else {
				if (fin != null) {
					fin.close();
				}
				throw new RuntimeException("Failed with HTTP error code : " + response.getStatusLine().getStatusCode());
			}
		} catch (Throwable th) {
			if (fin != null) {
				fin.close();
			}
			throw th;
		}

	}

	@Symbol("xooa")
	@Extension
	public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

		public FormValidation doCheckName(@QueryParameter String value) throws IOException, ServletException {
			return FormValidation.ok();
		}

		public FormValidation doCheckAppId(@QueryParameter String value) throws IOException, ServletException {
			return FormValidation.ok();
		}

		@Override
		public boolean isApplicable(Class<? extends AbstractProject> aClass) {
			return true;
		}

		@Override
		public String getDisplayName() {
			return "Upgrade Xooa app";
		}
	}
}
