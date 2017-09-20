package cn.edu.sysu.workflow.cloud.load.process;

import cn.edu.sysu.workflow.cloud.load.simulator.ProcessInstance;

import java.util.List;
import java.util.Map;

public class TraceNode {

    private List<TraceNode> nextNodes;
    private ProcessInstance.Task task;
    private Map<String, Object> variables;

    public List<TraceNode> getNextNodes() {
        return nextNodes;
    }

    public void setNextNodes(List<TraceNode> nextNodes) {
        this.nextNodes = nextNodes;
    }


    public ProcessInstance.Task getTask() {
        return task;
    }

    public void setTask(ProcessInstance.Task task) {
        this.task = task;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }
}
