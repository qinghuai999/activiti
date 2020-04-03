package com.activiti.demo.springbootactivitidemo;

import org.activiti.engine.*;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricIdentityLink;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

// 让测试在Spring容器下执行,否则会导致自动注入失败
@RunWith(SpringRunner.class)
// 单元测试注解
@SpringBootTest
public class activitiTest {
    // 提供流程初始存储的服务
    @Autowired
    private RepositoryService  repositoryService;

    // 流程在运行时对流程实例进行管理和控制
    @Autowired
    private RuntimeService runtimeService;

    // 提供任务管理服务
    @Autowired
    private TaskService taskService;

    @Autowired
    private FormService formService;

    // 提供用户管理服务
    @Autowired
    private IdentityService identityService;

    // 对流程的历史数据进行管理的维护的服务
    @Autowired
    private HistoryService historyService;




    /**
     * 部署流程 + 流程定义 + 开启流程
     */
    @Test
    public void startWorkFlow(){
        // 部署流程
        Deployment deploy = repositoryService.createDeployment().addClasspathResource("processes/activitiDemo2.bpmn").deploy();
        System.out.println("部署流程ID: " + deploy.getId());
        // 查询流程定义
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery().deploymentId(deploy.getId()).singleResult();
        System.out.println("查询流程定义ID: " + definition.getId() + "," + definition.getName() + "," + definition.getKey());

        // 开启流程 --> 流程开启后会生成一条流程实例
        ProcessInstance processInstance = runtimeService.startProcessInstanceById(definition.getId());
        System.out.println("流程实例ID:" + processInstance.getId() + ",流程实例ID: " + processInstance.getProcessInstanceId());


        // 查询主流程实例
        Execution singleResult = runtimeService.createExecutionQuery().executionId(processInstance.getId()).singleResult();// 查询子流程实例
        System.out.println("主执行流ID: " + singleResult.getId());

        // 查询当前任务
        List<Task> list = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
        for (Task task : list) {
            // 查询子执行流 --> 查询流程实例的当前状态
            List<Execution> executionList = runtimeService.createExecutionQuery().executionId(task.getExecutionId()).list();// 查询子流程实例
            for (Execution execution : executionList){
                System.out.println(execution.getId() + ", " + execution.getActivityId());
            }
            System.out.println("当前任务ID: " + task.getId() + ", " + task.getName() + "," + task.getProcessInstanceId());
        }
    }

    /**
     * 完成当前任务节点
     */
    @Test
    public void taskNow(){
        // 完成当前任务节点 (并行节点需可同时完成)
        taskService.complete("65011");
        taskService.complete("65013");

        // 查询完成后的任务节点信息
        Task task = taskService.createTaskQuery().processInstanceId("65005").singleResult();
        System.out.println(task.getId() + ", " + task.getName());
    }

    /**
     * 中止流程定义
     */
    @Test
    public void stopWorkFlow(){
        // 中止的流程定义不能被再次开启,进行中的任务也不能继续进行
//        repositoryService.suspendProcessDefinitionByKey("activitiDemo2");
//        runtimeService.startProcessInstanceById("activitiDemo2:3:52504");

        // 挂起流程实例
//        runtimeService.suspendProcessInstanceById("65005");
        // 激活流程实例
        runtimeService.activateProcessInstanceById("65005");
    }

    /**
     * 删除流程部署
     *      联级删除: 将存在的运行时任务一并删除 true
     *      不连级删除: 若有进行中的任务会删除失败 false(默认)
     */
    @Test
    public void delWorkFlow(){
        repositoryService.deleteDeployment("65001",true);
    }

    /**
     * 部署压缩文件
     */
    @Test
    public void zipDeployment() throws Exception {
        // 创建一个文件
        FileInputStream fileInputStream = new FileInputStream(new File("/Users/q/springboot-activiti-demo/src/main/resources/ceshi.zip"));
        // 转换为压缩文件
        ZipInputStream zipInputStream = new ZipInputStream(fileInputStream);
        // 部署流程
        repositoryService.createDeployment().addClasspathResource("processes/activitiDemo1.bpmn").addZipInputStream(zipInputStream).deploy();
    }

    /**
     * 查询流程图片
     */
    @Test
    public void picQuery() throws Exception {
        // 查询流程定义
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery().deploymentId("47501").singleResult();
        // 获取流程图片输入流数据
        InputStream processDiagram = repositoryService.getProcessDiagram(definition.getId());
        // 读取图片
        BufferedImage read = ImageIO.read(processDiagram);
        // 保存图片文件
        File file = new File("/Users/q/springboot-activiti-demo/src/main/resources/" + definition.getDiagramResourceName());
        // 将图片转为输出流
        FileOutputStream outputStream = new FileOutputStream(file);
        // 输出图片
        ImageIO.write(read,"png",outputStream);
    }

    /**
     * 查询该用户下的所有任务
     */
    @Test
    public void TaskByUser(){
        // 该用户下有多少可审批任务
        List<Task> manager = taskService.createTaskQuery().taskCandidateGroup("manager").list();
        for (Task task : manager){
            System.out.println(task.getId() + "," + task.getName());
        }

        // 通过任务id查询可参与者(参与组)
        List<IdentityLink> identityLinksForTask = taskService.getIdentityLinksForTask("80019");
        for (IdentityLink identityLink : identityLinksForTask){
            System.out.println(identityLink.getUserId() + ", " + identityLink.getGroupId());
        }
    }

    @Test
    public void deployWorkFlow(){
        // 部署工作流
        Deployment deploy = repositoryService.createDeployment().addClasspathResource("processes/activitiApproval.bpmn").deploy();
        System.out.println("查询部署ID: " + deploy.getId());
        // 查询流程定义ID
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery().deploymentId(deploy.getId()).singleResult();
        System.out.println("流程定义ID: " + definition.getId() + ", 流程名称: " + definition.getName() + ", 流程Key: " + definition.getKey());
        // 开启流程
        ProcessInstance startProcessInstanceById = runtimeService.startProcessInstanceById(definition.getId());
        // 查询流程实例 --> 主执行流
        Execution execution = runtimeService.createExecutionQuery().executionId(startProcessInstanceById.getId()).singleResult();
        System.out.println("查询流程实例ID:" + execution.getId());
    }

    /**
     * 查询当前任务表单数据
     */
    @Test
    public void formData(){
        List<Task> list = taskService.createTaskQuery().processInstanceId("87505").list();

        for (Task task : list){
            // 获取流程实例ID
            String processInstanceId = task.getProcessInstanceId();
            Map<String, Object> map = runtimeService.getVariables(processInstanceId);
            for (Map.Entry<String, Object> entry : map.entrySet()){
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }
            System.out.println("任务列表: " + task.getName());
        }
    }

    /**
     * 查询历史任务
     */
    @Test
    public void historyTask(){
        List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery().processInstanceId("80005").list();
        for (HistoricTaskInstance taskInstance : list){
            System.out.println(taskInstance.getId() + ", " + taskInstance.getName());
        }
    }

    /**
     * 查询工作流全部节点
     */
    @Test
    public void historyActiviti(){
        List<HistoricActivityInstance> list = historyService.createHistoricActivityInstanceQuery().processInstanceId("80005").list();
        for (HistoricActivityInstance activityInstance : list){
            System.out.println(activityInstance.getId() + ", " + activityInstance.getActivityId() + ", " + activityInstance.getActivityName());
        }
    }

    /**
     * 查询历史审批人
     */
    @Test
    public void historyUser(){
        // 通过实例查询审批人
        List<HistoricIdentityLink> identityLinks = historyService.getHistoricIdentityLinksForProcessInstance("80005");
        for (HistoricIdentityLink identityLink : identityLinks){
            System.out.println("通过实例查询审批人: " + identityLink.getGroupId() + ", " + identityLink.getUserId() + ", " +identityLink.getType());
        }

        // 通过任务查询历史参与人/组
        List<HistoricIdentityLink> linksForTask = historyService.getHistoricIdentityLinksForTask("82506");
        for (HistoricIdentityLink identityLink : linksForTask){
            System.out.println("通过任务查询候选组: " + identityLink.getGroupId() + ", " + identityLink.getUserId() + ", " +identityLink.getType());
        }
    }

    /**
     * 查询历史表单数据
     */
    @Test
    public void historyForm(){
        List<HistoricVariableInstance> list = historyService.createHistoricVariableInstanceQuery().processInstanceId("87505").list();
        for (HistoricVariableInstance variableInstance : list) {
            System.out.println(variableInstance.getId() + ", " + variableInstance.getValue());
        }
    }


}
