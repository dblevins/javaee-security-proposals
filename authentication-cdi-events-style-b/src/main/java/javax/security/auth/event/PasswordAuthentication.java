/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package javax.security.auth.event;

/**
 * Event to support Basic and FORM authentication methods
 * <p/>
 * Identified in IETF spec with recent update:
 * <p/>
 * - http://tools.ietf.org/html/draft-ietf-httpauth-basicauth-update
 * <p/>
 * HTTP Authorization header example:
 * <p/>
 * Authorization: Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==
 */
public class PasswordAuthentication extends Authentication {

    private final String username;
    private final String credential;


    public PasswordAuthentication(final String username, final String credential) {
        this.username = username;
        this.credential = credential;
    }

    public String getUsername() {
        return username;
    }

    public String getCredential() {
        return credential;
    }
}
