/*******************************************************************************
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt mountpoint-registrar
 * =================================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property. All rights reserved.
 * =================================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 * ============LICENSE_END==========================================================================
 ******************************************************************************/

package org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.impl;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.onap.dmaap.mr.client.MRClientFactory;
import org.onap.dmaap.mr.client.MRConsumer;
import org.onap.dmaap.mr.client.response.MRConsumerResponse;

public abstract class DMaaPVESMsgConsumerImpl implements DMaaPVESMsgConsumer {

	private static final Logger LOG = LoggerFactory.getLogger(DMaaPVESMsgConsumerImpl.class);

	private final String name = this.getClass().getSimpleName();
	private Properties properties = null;
	private MRConsumer consumer = null;
	private MRConsumerResponse consumerResponse = null;
	private boolean running = false;
	private boolean ready = false;
	private int fetchPause = 5000; // Default pause between fetch - 5 seconds
	private int timeout = 15000; // Default timeout - 15 seconds

	protected DMaaPVESMsgConsumerImpl() {

	}

	/*
	 * Thread to fetch messages from the DMaaP topic. Waits for the messages to arrive on the topic until a certain timeout and returns. 
	 * If no data arrives on the topic, sleeps for a certain time period before checking again
	 */
	@Override
	public void run() {

		if (ready) {
			running = true;
			while (running) {
				try {
					boolean noData = true;
					consumerResponse = consumer.fetchWithReturnConsumerResponse(timeout, -1);
					for (String msg : consumerResponse.getActualMessages()) {
						noData = false;
						LOG.debug(name + " received ActualMessage from DMaaP VES Message topic:\n"+msg);
						processMsg(msg);
					}

					if (noData) {
						LOG.debug(name + " received ResponseCode: " + consumerResponse.getResponseCode());
						LOG.debug(name + " received ResponseMessage: " + consumerResponse.getResponseMessage());
						if ((consumerResponse.getResponseCode() == null) && (consumerResponse.getResponseMessage().contains("SocketTimeoutException"))) {
							LOG.warn("Client timeout while waiting for response from Server {}", consumerResponse.getResponseMessage());
						}
						pauseThread();
					}
				} catch (Exception e) {
					LOG.error("Caught exception reading from DMaaP VES Message Topic", e);
					running = false;
				}
			}
		}
	}

	/*
	 * Create a consumer by specifying  properties containing information such as topic name, timeout, URL etc 
	 */
	@Override
	public void init(Properties properties) {

		try {
			
			String timeoutStr = properties.getProperty("timeout");
			LOG.debug("timeoutStr: " + timeoutStr);

			if ((timeoutStr != null) && (timeoutStr.length() > 0)) {
				timeout = parseTimeOutValue(timeoutStr);
			}

			String fetchPauseStr = properties.getProperty("fetchPause");
			LOG.debug("fetchPause(Str): " + fetchPauseStr);
			if ((fetchPauseStr != null) && (fetchPauseStr.length() > 0)) {
				fetchPause = parseFetchPause(fetchPauseStr);
			}
			LOG.debug("fetchPause: " + fetchPause);

			this.consumer = MRClientFactory.createConsumer(properties);
			ready = true;
		} catch (Exception e) {
			LOG.error("Error initializing DMaaP VES Message consumer from file " + properties, e);
		}
	}

	private int parseTimeOutValue(String timeoutStr) {
		try {
			return Integer.parseInt(timeoutStr);
		} catch (NumberFormatException e) {
			LOG.error("Non-numeric value specified for timeout (" + timeoutStr + ")");
		}
		return timeout;
	}

	private int parseFetchPause(String fetchPauseStr) {
		try {
			return Integer.parseInt(fetchPauseStr);
		} catch (NumberFormatException e) {
			LOG.error("Non-numeric value specified for fetchPause (" + fetchPauseStr + ")");
		}
		return fetchPause;
	}

	private void pauseThread() throws InterruptedException {
		if (fetchPause > 0) {
			LOG.debug(String.format("No data received from fetch.  Pausing %d ms before retry", fetchPause));
			Thread.sleep(fetchPause);
		} else {
			LOG.debug("No data received from fetch.  No fetch pause specified - retrying immediately");
		}
	}

	@Override
	public boolean isReady() {
		return ready;
	}

	@Override
	public boolean isRunning() {
		return running;
	}

	public String getProperty(String name) {
		return properties.getProperty(name, "");
	}

	@Override
	public void stopConsumer() {
		running = false;
	}

	public abstract void processMsg(String msg) throws Exception;

}
