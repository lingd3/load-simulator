package cn.edu.sysu.workflow.cloud.load.engine;

import java.io.File;

public interface ProcessEngine {
    String startProcess(String definitionId, Object data);

    String claimTask(String processId, String taskName);

    String deployProcessDefinition(String name, File file);

    void executeTrace(String processId, TraceNode root);
}