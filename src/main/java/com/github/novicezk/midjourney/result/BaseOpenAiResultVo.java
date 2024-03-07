package com.github.novicezk.midjourney.result;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class BaseOpenAiResultVo {

    @ApiModelProperty("错误信息")
    private OpenAiErrorResultVO error;




}
