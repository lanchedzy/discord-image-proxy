package com.github.novicezk.midjourney.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseOpenAiDTO {

    @ApiModelProperty(value = "模型名称", required = true, example = "dall-e-3")
    private String model;
    @ApiModelProperty(value = "prompt", required = true, example = "Cat")
    private String prompt;
}
