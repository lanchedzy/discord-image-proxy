package com.github.novicezk.midjourney.service;

import cn.hutool.core.thread.ThreadUtil;
import com.github.novicezk.midjourney.Constants;
import com.github.novicezk.midjourney.ReturnCode;
import com.github.novicezk.midjourney.domain.CozeBotConfig;
import com.github.novicezk.midjourney.enums.BlendDimensions;
import com.github.novicezk.midjourney.enums.TaskStatus;
import com.github.novicezk.midjourney.loadbalancer.DiscordInstance;
import com.github.novicezk.midjourney.loadbalancer.DiscordLoadBalancer;
import com.github.novicezk.midjourney.result.*;
import com.github.novicezk.midjourney.support.Task;
import com.github.novicezk.midjourney.util.MimeTypeUtils;
import eu.maxschuster.dataurl.DataUrl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {
	private final TaskStoreService taskStoreService;
	private final DiscordLoadBalancer discordLoadBalancer;

	@Override
	public SubmitResultVO submitImagine(Task task, List<DataUrl> dataUrls) {
		DiscordInstance instance = this.discordLoadBalancer.chooseInstance();
		if (instance == null) {
			return SubmitResultVO.fail(ReturnCode.NOT_FOUND, "无可用的账号实例");
		}
		task.setProperty(Constants.TASK_PROPERTY_DISCORD_INSTANCE_ID, instance.getInstanceId());
		return instance.submitTask(task, () -> {
			List<String> imageUrls = new ArrayList<>();
			for (DataUrl dataUrl : dataUrls) {
				String taskFileName = task.getId() + "." + MimeTypeUtils.guessFileSuffix(dataUrl.getMimeType());
				Message<String> uploadResult = instance.upload(taskFileName, dataUrl);
				if (uploadResult.getCode() != ReturnCode.SUCCESS) {
					return Message.of(uploadResult.getCode(), uploadResult.getDescription());
				}
				String finalFileName = uploadResult.getResult();
				Message<String> sendImageResult = instance.sendImageMessage("upload image: " + finalFileName, finalFileName);
				if (sendImageResult.getCode() != ReturnCode.SUCCESS) {
					return Message.of(sendImageResult.getCode(), sendImageResult.getDescription());
				}
				imageUrls.add(sendImageResult.getResult());
			}
			if (!imageUrls.isEmpty()) {
				task.setPrompt(String.join(" ", imageUrls) + " " + task.getPrompt());
				task.setPromptEn(String.join(" ", imageUrls) + " " + task.getPromptEn());
				task.setDescription("/imagine " + task.getPrompt());
				this.taskStoreService.save(task);
			}
			return instance.imagine(task.getPromptEn(), task.getPropertyGeneric(Constants.TASK_PROPERTY_NONCE));
		});
	}

	@Override
	public SubmitResultVO submitUpscale(Task task, String targetMessageId, String targetMessageHash, int index, int messageFlags) {
		String instanceId = task.getPropertyGeneric(Constants.TASK_PROPERTY_DISCORD_INSTANCE_ID);
		DiscordInstance discordInstance = this.discordLoadBalancer.getDiscordInstance(instanceId);
		if (discordInstance == null || !discordInstance.isAlive()) {
			return SubmitResultVO.fail(ReturnCode.NOT_FOUND, "账号不可用: " + instanceId);
		}
		return discordInstance.submitTask(task, () -> discordInstance.upscale(targetMessageId, index, targetMessageHash, messageFlags, task.getPropertyGeneric(Constants.TASK_PROPERTY_NONCE)));
	}

	@Override
	public SubmitResultVO submitVariation(Task task, String targetMessageId, String targetMessageHash, int index, int messageFlags) {
		String instanceId = task.getPropertyGeneric(Constants.TASK_PROPERTY_DISCORD_INSTANCE_ID);
		DiscordInstance discordInstance = this.discordLoadBalancer.getDiscordInstance(instanceId);
		if (discordInstance == null || !discordInstance.isAlive()) {
			return SubmitResultVO.fail(ReturnCode.NOT_FOUND, "账号不可用: " + instanceId);
		}
		return discordInstance.submitTask(task, () -> discordInstance.variation(targetMessageId, index, targetMessageHash, messageFlags, task.getPropertyGeneric(Constants.TASK_PROPERTY_NONCE)));
	}

	@Override
	public SubmitResultVO submitReroll(Task task, String targetMessageId, String targetMessageHash, int messageFlags) {
		String instanceId = task.getPropertyGeneric(Constants.TASK_PROPERTY_DISCORD_INSTANCE_ID);
		DiscordInstance discordInstance = this.discordLoadBalancer.getDiscordInstance(instanceId);
		if (discordInstance == null || !discordInstance.isAlive()) {
			return SubmitResultVO.fail(ReturnCode.NOT_FOUND, "账号不可用: " + instanceId);
		}
		return discordInstance.submitTask(task, () -> discordInstance.reroll(targetMessageId, targetMessageHash, messageFlags, task.getPropertyGeneric(Constants.TASK_PROPERTY_NONCE)));
	}

	@Override
	public SubmitResultVO submitDescribe(Task task, DataUrl dataUrl) {
		DiscordInstance discordInstance = this.discordLoadBalancer.chooseInstance();
		if (discordInstance == null) {
			return SubmitResultVO.fail(ReturnCode.NOT_FOUND, "无可用的账号实例");
		}
		task.setProperty(Constants.TASK_PROPERTY_DISCORD_INSTANCE_ID, discordInstance.getInstanceId());
		return discordInstance.submitTask(task, () -> {
			String taskFileName = task.getId() + "." + MimeTypeUtils.guessFileSuffix(dataUrl.getMimeType());
			Message<String> uploadResult = discordInstance.upload(taskFileName, dataUrl);
			if (uploadResult.getCode() != ReturnCode.SUCCESS) {
				return Message.of(uploadResult.getCode(), uploadResult.getDescription());
			}
			String finalFileName = uploadResult.getResult();
			return discordInstance.describe(finalFileName, task.getPropertyGeneric(Constants.TASK_PROPERTY_NONCE));
		});
	}

	@Override
	public SubmitResultVO submitBlend(Task task, List<DataUrl> dataUrls, BlendDimensions dimensions) {
		DiscordInstance discordInstance = this.discordLoadBalancer.chooseInstance();
		if (discordInstance == null) {
			return SubmitResultVO.fail(ReturnCode.NOT_FOUND, "无可用的账号实例");
		}
		task.setProperty(Constants.TASK_PROPERTY_DISCORD_INSTANCE_ID, discordInstance.getInstanceId());
		return discordInstance.submitTask(task, () -> {
			List<String> finalFileNames = new ArrayList<>();
			for (DataUrl dataUrl : dataUrls) {
				String taskFileName = task.getId() + "." + MimeTypeUtils.guessFileSuffix(dataUrl.getMimeType());
				Message<String> uploadResult = discordInstance.upload(taskFileName, dataUrl);
				if (uploadResult.getCode() != ReturnCode.SUCCESS) {
					return Message.of(uploadResult.getCode(), uploadResult.getDescription());
				}
				finalFileNames.add(uploadResult.getResult());
			}
			return discordInstance.blend(finalFileNames, dimensions, task.getPropertyGeneric(Constants.TASK_PROPERTY_NONCE));
		});
	}

	@Override
	public SubmitResultVO chatCompletions(Task task, List<DataUrl> dataUrls) {
		return null;
	}

	@Override
	public ImageGenerationsResultVO imageGenerations(Task task, List<DataUrl> dataUrls) {
		DiscordInstance instance = this.discordLoadBalancer.chooseInstance();
		if (instance == null) {
			return ImageGenerationsResultVO.fail("无可用的账号实例", ReturnCode.NOT_FOUND + "");
		}
		CozeBotConfig cozeBot = this.discordLoadBalancer.chooseCozeBot(instance);
		if (cozeBot == null) {
			return ImageGenerationsResultVO.fail("无可用的CozeBot", ReturnCode.NOT_FOUND + "");
		}
		task.setProperty(Constants.TASK_PROPERTY_DISCORD_INSTANCE_ID, instance.getInstanceId());
		// 发送消息
		task.start();
		Message<MessagesResultVO> message = instance.sendTextMessage(cozeBot.getBotId(), cozeBot.getChannelId(), task.getPrompt(), task.getPropertyGeneric(Constants.TASK_PROPERTY_NONCE));
		if (message.getCode() != ReturnCode.SUCCESS) {
			return ImageGenerationsResultVO.fail(message.getDescription(), message.getCode() + "");
		}
		log.info("prompt:{}, start for messageId:{}", task.getPrompt(), message.getResult().getId());
		task.setStatus(TaskStatus.IN_PROGRESS);
		task.setSubmitTime(System.currentTimeMillis());
		instance.putTask(message.getResult().getId(), task);
		while (true) {
			ThreadUtil.sleep(50);
			task = instance.getTaskByMessageId(message.getResult().getId());
			if (task.getStatus() == TaskStatus.FAILURE) {
				return ImageGenerationsResultVO.fail(task.getFailReason(), ReturnCode.NOT_FOUND + "");
			}
			switch (task.getStatus()) {
				case FAILURE:
					return ImageGenerationsResultVO.fail(task.getFailReason(), ReturnCode.NOT_FOUND + "");
				case SUCCESS:
					ImageGenerationsResultVO resultVO = new ImageGenerationsResultVO();
					resultVO.setCreated(task.getFinishTime());
					resultVO.setDailyLimit(false);
					List<ImageItemDTO> imageItemDTOS = new ArrayList<>();
					imageItemDTOS.add(new ImageItemDTO(task.getImageUrl()));
					resultVO.setData(imageItemDTOS);
					return resultVO;
				default:
					if (System.currentTimeMillis() - task.getSubmitTime() > 60000) {
						task.fail("请求超时");
						return ImageGenerationsResultVO.fail("请求超时", ReturnCode.TIME_OUT + "");
					}

            }
		}
	}

}
