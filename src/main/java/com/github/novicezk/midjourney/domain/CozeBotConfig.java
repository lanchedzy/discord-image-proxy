package com.github.novicezk.midjourney.domain;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode()
@ApiModel("Coze discord bot账号")
public class CozeBotConfig implements Serializable {

    /**
     * coze 机器人ID
     */
    private String botId;
    /**
     * 机器人名称
     */
    private String botName;
    /**
     * 回复的频道
     */
    private String channelId;
}
