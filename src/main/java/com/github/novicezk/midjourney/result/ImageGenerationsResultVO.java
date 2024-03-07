package com.github.novicezk.midjourney.result;

import com.github.novicezk.midjourney.dto.BaseOpenAiDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@ApiModel("image/generations接口返回结果")
@Getter
@Setter
public class ImageGenerationsResultVO extends BaseOpenAiResultVo {
	@ApiModelProperty(value = "创建时间")
	private Long created;
	@ApiModelProperty(value = "是否触发限制")
	private boolean dailyLimit;
	@ApiModelProperty(value = "返回的图片列表")
	private List<ImageItemDTO> data;

	public static ImageGenerationsResultVO fail(String message, String code) {
		return ImageGenerationsResultVO.fail(message, "request_error", code);
	}

	public static ImageGenerationsResultVO fail(String message, String type, String code) {
		ImageGenerationsResultVO vo = new ImageGenerationsResultVO();
		OpenAiErrorResultVO requestError = new OpenAiErrorResultVO(message, type, null, code);
		vo.setError(requestError);
		return vo;
	}

}
