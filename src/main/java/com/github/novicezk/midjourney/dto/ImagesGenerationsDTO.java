package com.github.novicezk.midjourney.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel("images/generations 提交参数")
@EqualsAndHashCode(callSuper = true)
public class ImagesGenerationsDTO extends BaseOpenAiDTO{
    @ApiModelProperty(value = "生成数量", required = true, example = "1")
    private Long n;
    @ApiModelProperty(value = "图像质量", required = false, example = "1024*1024")
    private String size;
}
