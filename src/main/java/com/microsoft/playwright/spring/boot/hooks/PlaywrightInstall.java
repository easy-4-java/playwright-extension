/*
 * Copyright (c) 2018, hiwepy (https://github.com/hiwepy).
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
package com.microsoft.playwright.spring.boot.hooks;

import com.microsoft.playwright.spring.boot.PlaywrightBrowserType;
import com.microsoft.playwright.spring.boot.pool.BrowserContextPool;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
public class PlaywrightInstall implements Runnable {
	private volatile boolean isInstalled = false;
	private BrowserContextPool browserContextPool;
	private String downloadHost;
	public PlaywrightInstall(BrowserContextPool browserContextPool, String downloadHost) {
		this.browserContextPool = browserContextPool;
		this.downloadHost = downloadHost;
	}

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

	public boolean isInstalled() {
		return isInstalled;
	}

}
