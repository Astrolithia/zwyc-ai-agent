package com.zwyc.zwycaiagent;

import com.alibaba.cloud.ai.autoconfigure.dashscope.DashScopeAudioSpeechAutoConfiguration;
import com.alibaba.cloud.ai.autoconfigure.dashscope.DashScopeAudioTranscriptionAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ZwycAiAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZwycAiAgentApplication.class, args);
    }

}
