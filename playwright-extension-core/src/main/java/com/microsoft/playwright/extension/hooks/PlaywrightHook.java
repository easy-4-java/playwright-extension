/*
 * Copyright (c) 2018, Loong Wan (https://github.com/loong10k).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.microsoft.playwright.extension.hooks;

import com.microsoft.playwright.extension.pool.BrowserContextPooledObjectFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * JVM shutdown hook that ensures the Playwright browser context pool is properly
 * closed when the application terminates. This hook is registered as a daemon thread
 * named {@code "playwright-shutdown-hook"} to perform graceful cleanup of browser
 * resources during JVM shutdown.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see BrowserContextPooledObjectFactory
 */
@Slf4j
public class PlaywrightHook extends Thread{

	private BrowserContextPooledObjectFactory factory;
	private long awaitTerminateMillis;

	/**
	 * Constructs a new Playwright shutdown hook.
	 *
	 * @param factory             the pooled object factory to close on shutdown, may be {@code null}
	 * @param awaitTerminateMillis the maximum time in milliseconds to wait for termination
	 */
	public PlaywrightHook(BrowserContextPooledObjectFactory factory, long awaitTerminateMillis) {
		this.setName("playwright-shutdown-hook");
		this.factory = factory;
		this.awaitTerminateMillis = awaitTerminateMillis;
	}

	/**
	 * Executes the shutdown hook by closing the browser context pool factory.
	 * If the factory is {@code null}, this method does nothing. Any exceptions
	 * thrown during factory closure are logged and swallowed.
	 */
	@Override
	public void run() {
		if(Objects.nonNull(factory)){
			try {
				factory.close();
			} catch (Exception e) {
				log.error("Error occurred while closing Playwright browser context pool", e);
			}
		}
	}

}
