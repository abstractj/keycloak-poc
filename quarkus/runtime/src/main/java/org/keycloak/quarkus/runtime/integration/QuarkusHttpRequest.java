/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.quarkus.runtime.integration;

import java.security.cert.X509Certificate;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import org.jboss.resteasy.spi.HttpRequest;
import org.keycloak.services.HttpRequestImpl;

import io.vertx.ext.web.RoutingContext;

public class QuarkusHttpRequest extends HttpRequestImpl {

    public <R> QuarkusHttpRequest(HttpRequest delegate) {
        super(delegate);
    }

    @Override
    public X509Certificate[] getClientCertificateChain() {
        Instance<RoutingContext> instances = CDI.current().select(RoutingContext.class);

        if (instances.isResolvable()) {
            RoutingContext context = instances.get();

            try {
                SSLSession sslSession = context.request().sslSession();

                if (sslSession == null) {
                    return null;
                }

                return (X509Certificate[]) sslSession.getPeerCertificates();
            } catch (SSLPeerUnverifiedException ignore) {
                // client not authenticated
            }
        }

        return null;
    }
}
