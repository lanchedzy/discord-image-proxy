package com.github.novicezk.midjourney.wss.handle;


import cn.hutool.core.text.CharSequenceUtil;
import com.github.novicezk.midjourney.Constants;
import com.github.novicezk.midjourney.enums.MessageType;
import com.github.novicezk.midjourney.enums.TaskAction;
import com.github.novicezk.midjourney.loadbalancer.DiscordInstance;
import com.github.novicezk.midjourney.support.Task;
import com.github.novicezk.midjourney.support.TaskCondition;
import com.github.novicezk.midjourney.util.ContentParseData;
import com.github.novicezk.midjourney.util.ConvertUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * imagine消息处理.
 * 完成(create): **cat** - <@1012983546824114217> (relaxed)
 */
@Slf4j
@Component
public class ImageGenerationsSuccessHandler extends MessageHandler {
	private static final String CONTENT_REGEX = "\\*\\*(.*?)\\*\\* - <@\\d+> \\((.*?)\\)";

	@Override
	public void handle(DiscordInstance instance, MessageType messageType, DataObject message) {
		String messageId = getReferenceMessageId(message);
		if (CharSequenceUtil.isEmpty(messageId)) {
			return;
		}
		if (MessageType.CREATE.equals(messageType) && hasCozeImage(message)) {
			Task task = instance.getTaskByMessageId(messageId);
			String imageUrl = getEmbedsImageUrl(message);
			task.setImageUrl(imageUrl);
			finishTask(task, message);
			log.info("messageId:{}, 任务完成: {}", messageId, task);
		}
	}
}
