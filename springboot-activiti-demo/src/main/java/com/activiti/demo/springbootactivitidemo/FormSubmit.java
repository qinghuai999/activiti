package all.com.activiti.demo.springbootactivitidemo;

import org.activiti.engine.*;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.impl.form.BooleanFormType;
import org.activiti.engine.impl.form.DateFormType;
import org.activiti.engine.impl.form.StringFormType;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class FormSubmit {
    public static void main(String[] args) throws Exception {
        // 暴露了所有activiti的服务接口,使用main方法想要找到以下服务需要配置activiti.cfg.xml
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        RepositoryService repositoryService = processEngine.getRepositoryService();
        RuntimeService runtimeService = processEngine.getRuntimeService();
        TaskService taskService = processEngine.getTaskService();
        FormService formService = processEngine.getFormService();
        HistoryService historyService = processEngine.getHistoryService();


        //查询当前任务
        Task task = taskService.createTaskQuery().processInstanceId("87505").singleResult();
        System.out.println("查询当前任务ID: " + task.getId() + ", 节点名称: " + task.getName());

        Scanner scanner = new Scanner(System.in);
        // 获取表单信息
        TaskFormData taskFormData = formService.getTaskFormData(task.getId());
        // 获取表单属性
        List<FormProperty> formProperties = taskFormData.getFormProperties();
        Map<String, Object> map = new HashMap<>();
        for (FormProperty formProperty : formProperties){

            if (StringFormType.class.isInstance(formProperty.getType())){
                String line = null;
                System.out.println("请输入" + formProperty.getName());
                line = scanner.nextLine();
                map.put(formProperty.getId(),line);
            } else if (BooleanFormType.class.isInstance(formProperty.getType())){
                boolean line = false;
                System.out.println("请输入" + formProperty.getName());
                line = scanner.nextBoolean();
                map.put(formProperty.getId(),line);
            } else if (DateFormType.class.isInstance(formProperty.getType())){
                String line = null;
                System.out.println("请输入" + formProperty.getName());
                line = scanner.nextLine();
                // 格式化时间
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date date = dateFormat.parse(line);
                map.put(formProperty.getId(),date);
            }

        }
        // 获取当前用户信息
        taskService.setAssignee(task.getId(),"CEO");
        // 结束该节点
        taskService.complete(task.getId(),map);

        //获取下一个任务信息
        Task nextTask = taskService.createTaskQuery().processInstanceId("87505").singleResult();
        System.out.println(nextTask);


    }
}
