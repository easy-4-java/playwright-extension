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

import com.microsoft.playwright.extension.PlaywrightBrowserType;
import com.microsoft.playwright.extension.pool.BrowserContextPool;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * Runnable task that triggers Playwright browser installation by borrowing an object
 * from the browser context pool. This is typically executed at application startup to
 * ensure that the required browser binaries are downloaded and available. The download
 * host can be customized via the {@code PLAYWRIGHT_DOWNLOAD_HOST} system property.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 * @see BrowserContextPool
 * @see PlaywrightBrowserType
 */
@Slf4j
public class PlaywrightInstall implements Runnable {
	private volatile boolean isInstalled = false;
	private BrowserContextPool browserContextPool;
	private String downloadHost;

	/**
	 * Constructs a new Playwright install task.
	 *
	 * @param browserContextPool the pool to borrow a browser context from, triggering installation
	 * @param downloadHost       the host URL to download browser binaries from, may be {@code null}
	 */
	public PlaywrightInstall(BrowserContextPool browserContextPool, String downloadHost) {
		this.browserContextPool = browserContextPool;
		this.downloadHost = downloadHost;
	}

	/**
	 * Executes the Playwright installation process. Sets the download host as a system
	 * property and borrows an object from the pool to trigger browser binary download.
	 * If the pool or download host is {@code null}, this method does nothing.
	 */
	@Override
	public void run() {
		if(Objects.nonNull(browserContextPool) && Objects.nonNull(downloadHost)){
			try {
				// 1、触发浏览器安装
				System.setProperty("PLAYWRIGHT_DOWNLOAD_HOST", downloadHost);
				browserContextPool.borrowObject();
				// 2、安装完成后
				isInstalled = true;
 				if (isInstalled) {
					log.info("Playwright is installed.");
				} else {
					log.warn("Playwright is not installed yet.");
				}
			} catch (Exception e) {
				log.error("Playwright install error", e);
			}
		}
	}

	/**
	 * Returns whether the Playwright browser installation has completed successfully.
	 *
	 * @return {@code true} if installation completed, {@code false} otherwise
	 */
	public boolean isInstalled() {
		return isInstalled;
	}

}
