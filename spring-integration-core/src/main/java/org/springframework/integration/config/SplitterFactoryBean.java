/*
 * Copyright 2002-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.integration.config;

import org.springframework.expression.Expression;
import org.springframework.integration.core.MessageHandler;
import org.springframework.integration.splitter.AbstractMessageSplitter;
import org.springframework.integration.splitter.DefaultMessageSplitter;
import org.springframework.integration.splitter.ExpressionEvaluatingSplitter;
import org.springframework.integration.splitter.MethodInvokingSplitter;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Factory bean for creating a Message Splitter.
 * 
 * @author Mark Fisher
 */
public class SplitterFactoryBean extends AbstractMessageHandlerFactoryBean {

	private volatile Long sendTimeout;

	private volatile boolean requiresReply;


	public void setSendTimeout(Long sendTimeout) {
		this.sendTimeout = sendTimeout;
	}

	public boolean isRequiresReply() {
		return requiresReply;
	}

	public void setRequiresReply(boolean requiresReply) {
		this.requiresReply = requiresReply;
	}

	@Override
	MessageHandler createMethodInvokingHandler(Object targetObject, String targetMethodName) {
		Assert.notNull(targetObject, "targetObject must not be null");
		AbstractMessageSplitter splitter = this.extractTypeIfPossible(targetObject, AbstractMessageSplitter.class);
		if (splitter == null) {
			splitter = this.createMethodInvokingSplitter(targetObject, targetMethodName);
			this.configureSplitter(splitter);
		}
		else {
			Assert.isTrue(!StringUtils.hasText(targetMethodName), "target method should not be provided when the target "
					+ "object is an implementation of AbstractMessageSplitter");
			this.configureSplitter(splitter);
			if (targetObject instanceof MessageHandler) {
				return (MessageHandler) targetObject;
			}
		}
		return splitter;
	}

	private AbstractMessageSplitter createMethodInvokingSplitter(Object targetObject, String targetMethodName) {
		return (StringUtils.hasText(targetMethodName))
				? new MethodInvokingSplitter(targetObject, targetMethodName)
				: new MethodInvokingSplitter(targetObject);
	}

	@Override
	MessageHandler createExpressionEvaluatingHandler(Expression expression) {
		return this.configureSplitter(new ExpressionEvaluatingSplitter(expression));
	}

	@Override
	MessageHandler createDefaultHandler() {
		return this.configureSplitter(new DefaultMessageSplitter());
	}

	private AbstractMessageSplitter configureSplitter(AbstractMessageSplitter splitter) {
		if (this.sendTimeout != null) {
			splitter.setSendTimeout(sendTimeout);
		}
		splitter.setRequiresReply(requiresReply);
		return splitter;
	}

}
