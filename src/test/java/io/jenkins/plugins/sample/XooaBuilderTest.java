package io.jenkins.plugins.sample;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Label;
//import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
//import org.jenkinsci.plugins.workflow.job.WorkflowJob;
//import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class XooaBuilderTest {

//    @Rule
//    public JenkinsRule jenkins = new JenkinsRule();

    final String apiToken = "API Token";
    final String appId = "XLDB App";
    final String appName = "XLDB";
    final String appDesc = "Xooa XLDB";
    final String language = "Golang";
    final String path = "/smartcontract/";

   /* @Test
    public void testConfigRoundtrip() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        project.getBuildersList().add(new XooaBuilder(apiToken, appId, appName, appDesc, language, path));
        project = jenkins.configRoundtrip(project);
        jenkins.assertEqualDataBoundBeans(new XooaBuilder(apiToken, appId, appName, appDesc, language, path), project.getBuildersList().get(0));
    }

    

    @Test
    public void testBuild() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        XooaBuilder builder = new XooaBuilder(apiToken, appId, appName, appDesc, language, path);
        project.getBuildersList().add(builder);

        FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
        jenkins.assertLogContains("Hello, " + apiToken, build);
    }

    

    @Test
    public void testScriptedPipeline() throws Exception {
        String agentLabel = "my-agent";
        jenkins.createOnlineSlave(Label.get(agentLabel));
        WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-scripted-pipeline");
        String pipelineScript
                = "node {\n"
                + "  greet '" + apiToken + "'\n"
                + "}";
        job.setDefinition(new CpsFlowDefinition(pipelineScript, true));
        WorkflowRun completedBuild = jenkins.assertBuildStatusSuccess(job.scheduleBuild2(0));
        String expectedString = "Hello, " + apiToken + "!";
        jenkins.assertLogContains(expectedString, completedBuild);
    }*/

}