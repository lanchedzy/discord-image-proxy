package com.github.novicezk.midjourney.result;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
@AllArgsConstructor
public class OpenAiErrorResultVO {

    @ApiModelProperty("错误信息")
    private String message;
    @ApiModelProperty("错误类型")
    private String type;
    @ApiModelProperty("响应信息")
    private String param;
    @ApiModelProperty("错误码")
    private String code;
}
