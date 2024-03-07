package com.github.novicezk.midjourney.controller;

import cn.hutool.core.text.CharSequenceUtil;
import com.github.novicezk.midjourney.Constants;
import com.github.novicezk.midjourney.ProxyProperties;
import com.github.novicezk.midjourney.ReturnCode;
import com.github.novicezk.midjourney.dto.ImagesGenerationsDTO;
import com.github.novicezk.midjourney.enums.TaskAction;
import com.github.novicezk.midjourney.result.BaseOpenAiResultVo;
import com.github.novicezk.midjourney.result.ImageGenerationsResultVO;
import com.github.novicezk.midjourney.service.TaskService;
import com.github.novicezk.midjourney.support.Task;
import com.github.novicezk.midjourney.util.SnowFlake;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api("openAi接口对齐")
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class OpenAiController {
    private final TaskService taskService;
    private final ProxyProperties properties;

    @ApiOperation(value = "提交OpenAi图片生成")
    @PostMapping("/images/generations")
    public ImageGenerationsResultVO imagesGenerations(@RequestBody ImagesGenerationsDTO imagineDTO) {
        String prompt = imagineDTO.getPrompt();
        if (CharSequenceUtil.isBlank(prompt)) {
            return ImageGenerationsResultVO.fail("prompt不能为空", "param invalid", "" + ReturnCode.VALIDATION_ERROR);
        }
        Task task = newTask(TaskAction.IMAGE_GENERATIONS);
        task.setPrompt(properties.getImagePromptPrefix() +  prompt.trim());
        return (ImageGenerationsResultVO) this.taskService.imageGenerations(task, null);
    }

    private Task newTask(TaskAction taskAction) {
        Task task = new Task();
        task.setId(SnowFlake.INSTANCE.nextId());
        task.setSubmitTime(System.currentTimeMillis());
        task.setProperty(Constants.TASK_PROPERTY_NONCE, SnowFlake.INSTANCE.nextId());
        task.setAction(taskAction);
        return task;
    }
}
