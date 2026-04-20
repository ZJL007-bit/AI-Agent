package com.lenyan.lenaiagent.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 终端命令执行工具类
 */
public class TerminalOperationTool {

    /**
     * 执行终端命令并返回结果
     */
    @Tool(description = "在终端中执行命令并返回结果")
    public String executeTerminalCommand(@ToolParam(description = "要执行的终端命令") String command) {
        StringBuilder output = new StringBuilder();
        try {
            // 使用ProcessBuilder执行命令
            ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", command);
            Process process = builder.start();
            
            // 读取命令输出
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            
            // 检查命令执行结果
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                output.append("Command execution failed with exit code: ").append(exitCode);
            }
        } catch (IOException | InterruptedException e) {
            output.append("Error executing command: ").append(e.getMessage());
        }
        return output.toString();
    }
}
