package com.github.novicezk.midjourney.result;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@AllArgsConstructor
@Data
public class ImageItemDTO implements Serializable {

    @ApiModelProperty(value = "生成的图片URL地址")
    private String url;
}
