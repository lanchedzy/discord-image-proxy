package com.github.novicezk.midjourney.loadbalancer;


import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.RandomUtil;
import com.github.novicezk.midjourney.domain.CozeBotConfig;
import com.github.novicezk.midjourney.loadbalancer.rule.IRule;
import com.github.novicezk.midjourney.support.Task;
import com.github.novicezk.midjourney.support.TaskCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class DiscordLoadBalancer {
	private final IRule rule;

	private final List<DiscordInstance> instances = Collections.synchronizedList(new ArrayList<>());

	public List<DiscordInstance> getAllInstances() {
		return this.instances;
	}

	public List<DiscordInstance> getAliveInstances() {
		return this.instances.stream().filter(DiscordInstance::isAlive).toList();
	}

	public DiscordInstance chooseInstance() {
		return this.rule.choose(getAliveInstances());
	}

	public CozeBotConfig chooseCozeBot(DiscordInstance instance) {
		return RandomUtil.randomEle(instance.account().getCozes());
	}

	public DiscordInstance getDiscordInstance(String instanceId) {
		if (CharSequenceUtil.isBlank(instanceId)) {
			return null;
		}
		return this.instances.stream()
				.filter(instance -> CharSequenceUtil.equals(instanceId, instance.getInstanceId()))
				.findFirst().orElse(null);
	}

	public Set<String> getQueueTaskIds() {
		Set<String> taskIds = Collections.synchronizedSet(new HashSet<>());
		for (DiscordInstance instance : getAliveInstances()) {
			taskIds.addAll(instance.getRunningFutures().keySet());
		}
		return taskIds;
	}

	public List<Task> getQueueTasks() {
		List<Task> tasks = new ArrayList<>();
		for (DiscordInstance instance : getAliveInstances()) {
			tasks.addAll(instance.getQueueTasks());
		}
		return tasks;
	}

    public Task getMappingTask(String messageId) {
        for (DiscordInstance instance : getAliveInstances()) {
            Task task = instance.getTaskByMessageId(messageId);
            if (task != null) {
                return task;
            }
        }
        return null;
    }

    public Task getRunningTaskByNonce(String nonce) {
        if (CharSequenceUtil.isBlank(nonce)) {
            return null;
        }
        TaskCondition condition = new TaskCondition().setNonce(nonce);
        for (DiscordInstance instance : getAliveInstances()) {
            Optional<Task> optional = instance.getRunningTasks().stream().filter(condition).findFirst();
            if (optional.isPresent()) {
                return optional.get();
            }
        }
        return null;
    }

}
