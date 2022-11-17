/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.causeway.extensions.executionoutbox.restclient.integtests;

import jakarta.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.causeway.core.config.RestEasyConfiguration;
import org.apache.causeway.core.config.viewer.web.WebAppContextPath;
import org.apache.causeway.extensions.executionoutbox.restclient.api.OutboxClient;

import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class RestEndpointService {

    private final RestEasyConfiguration restEasyConfiguration;
    private final WebAppContextPath webAppContextPath;

    public OutboxClient newClient(final int port, final String username, final String password) {

        val restRootPath =
                String.format("http://localhost:%d%s/",
                        port,
                        webAppContextPath
                                .prependContextPath(this.restEasyConfiguration.getJaxrs().getDefaultPath())
                );

        return new OutboxClient(restRootPath, username, password);
    }

}
