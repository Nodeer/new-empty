package org.camunda.bpm.integrationtest.jobexecutor;

import java.util.List;
import java.util.Set;

import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.ProcessEngineService;
import org.camunda.bpm.application.ProcessApplicationDeploymentInfo;
import org.camunda.bpm.application.ProcessApplicationInfo;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cmd.AcquireJobsCmd;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.AcquiredJobs;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class IndependentJobExecutionTest extends AbstractFoxPlatformIntegrationTest {
  
  private ProcessEngine engine1;
  private ProcessEngineConfigurationImpl engine1Configuration;
  
  @Before
  public void setEngines() {
    ProcessEngineService engineService = BpmPlatform.getProcessEngineService();
    engine1 = engineService.getProcessEngine("engine1");
    engine1Configuration = ((ProcessEngineImpl) engine1).getProcessEngineConfiguration();
  }

  @Deployment(order = 0, name="pa1")
  public static WebArchive processArchive1() {    
    
    return initWebArchiveDeployment("pa1.war", "org/camunda/bpm/integrationtest/jobexecutor/pa1.xml")
        .addAsResource("org/camunda/bpm/integrationtest/jobexecutor/archive1process.bpmn20.xml");
            
  }
  
  @Deployment(order = 1, name="pa2")
  public static WebArchive processArchive2() {    
    
    return initWebArchiveDeployment("pa2.war", "org/camunda/bpm/integrationtest/jobexecutor/pa2.xml")
        .addAsResource("org/camunda/bpm/integrationtest/jobexecutor/archive2process.bpmn20.xml");
  }
  
  @OperateOnDeployment("pa1")
  @Test
  public void testDeploymentRegistration() {
    Set<String> registeredDeploymentsForEngine1 = engine1.getManagementService().getRegisteredDeployments();
    Set<String> registeredDeploymentsForDefaultEngine = processEngine.getManagementService().getRegisteredDeployments();
    
    ProcessApplicationInfo pa1Info = getProcessApplicationDeploymentInfo("pa1");
    
    List<ProcessApplicationDeploymentInfo> pa1DeploymentInfo = pa1Info.getDeploymentInfo();
    
    Assert.assertEquals(1, pa1DeploymentInfo.size());
    Assert.assertTrue(registeredDeploymentsForEngine1.contains(pa1DeploymentInfo.get(0).getDeploymentId()));
    
    ProcessApplicationInfo pa2Info = getProcessApplicationDeploymentInfo("pa2");
    
    List<ProcessApplicationDeploymentInfo> pa2DeploymentInfo = pa2Info.getDeploymentInfo();
    Assert.assertEquals(1, pa2DeploymentInfo.size());
    Assert.assertTrue(registeredDeploymentsForDefaultEngine.contains(pa2DeploymentInfo.get(0).getDeploymentId()));
  }
  
  private ProcessApplicationInfo getProcessApplicationDeploymentInfo(String applicationName) {
    ProcessApplicationInfo processApplicationInfo = BpmPlatform.getProcessApplicationService().getProcessApplicationInfo(applicationName);
    if (processApplicationInfo == null) {
      processApplicationInfo = BpmPlatform.getProcessApplicationService().getProcessApplicationInfo("/" + applicationName);
    }
    return processApplicationInfo;
    
  }
  
  @OperateOnDeployment("pa1")
  @Test
  public void testDeploymentAwareJobAcquisition() {
    JobExecutor jobExecutor1 = engine1Configuration.getJobExecutor();
    
    ProcessInstance instance1 = engine1.getRuntimeService().startProcessInstanceByKey("archive1Process");
    ProcessInstance instance2 = processEngine.getRuntimeService().startProcessInstanceByKey("archive2Process");
    
    Job job1 = managementService.createJobQuery().processInstanceId(instance1.getId()).singleResult();
    Job job2 = managementService.createJobQuery().processInstanceId(instance2.getId()).singleResult();
    
    
    // the deployment aware configuration should only return the jobs of the registered deployments
    CommandExecutor commandExecutor = engine1Configuration.getCommandExecutorTxRequired();
    AcquiredJobs acquiredJobs = commandExecutor.execute(new AcquireJobsCmd(jobExecutor1));
    
    Assert.assertEquals(1, acquiredJobs.size());
    Assert.assertTrue(acquiredJobs.contains(job1.getId()));
    Assert.assertFalse(acquiredJobs.contains(job2.getId()));
  }
  
  @OperateOnDeployment("pa1")
  @Test
  public void testDeploymentUnawareJobAcquisition() {
    JobExecutor defaultJobExecutor = processEngineConfiguration.getJobExecutor();
    
    ProcessInstance instance1 = engine1.getRuntimeService().startProcessInstanceByKey("archive1Process");
    ProcessInstance instance2 = processEngine.getRuntimeService().startProcessInstanceByKey("archive2Process");
    
    Job job1 = managementService.createJobQuery().processInstanceId(instance1.getId()).singleResult();
    Job job2 = managementService.createJobQuery().processInstanceId(instance2.getId()).singleResult();
    
    // the deployment unaware configuration should return both jobs
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();
    AcquiredJobs acquiredJobs = commandExecutor.execute(new AcquireJobsCmd(defaultJobExecutor));
    
    Assert.assertEquals(2, acquiredJobs.size());
    Assert.assertTrue(acquiredJobs.contains(job1.getId()));
    Assert.assertTrue(acquiredJobs.contains(job2.getId()));
  }
}
