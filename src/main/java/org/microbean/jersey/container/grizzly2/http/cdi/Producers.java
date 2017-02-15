/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2017 MicroBean.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.microbean.jersey.container.grizzly2.http.cdi;

import java.net.URI;
import java.net.URISyntaxException;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;

import javax.enterprise.inject.CreationException;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;

import javax.ws.rs.core.Application;

import org.microbean.configuration.cdi.annotation.ConfigurationValue;

import org.glassfish.grizzly.http.server.HttpServer;

import org.glassfish.grizzly.ssl.SSLEngineConfigurator;

import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpContainer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;

import org.glassfish.jersey.server.ContainerFactory;

/**
 * A class housing <a
 * href="http://docs.jboss.org/cdi/spec/2.0.EDR2/cdi-spec.html#producer_method">producer
 * methods</a> that manufacture certain Grizzly- and Jersey-related
 * objects.
 *
 * @author <a href="http://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 */
@ApplicationScoped
class Producers {

  private Producers() {
    super();
  }
  
  @Produces
  @Dependent
  private static final GrizzlyHttpContainer produceGrizzlyHttpContainer(final Instance<Application> applicationInstance) {
    final GrizzlyHttpContainer returnValue;
    if (applicationInstance == null || applicationInstance.isUnsatisfied()) {
      returnValue = null;
    } else {
      returnValue = ContainerFactory.createContainer(GrizzlyHttpContainer.class, applicationInstance.get());
    }
    return returnValue;
  }

  @Produces
  @Dependent
  private static final HttpServer produceHttpServer(@ConfigurationValue("host") final String host,
                                                    @ConfigurationValue("port") final int port,
                                                    @ConfigurationValue("contextPath") final String contextPath,
                                                    final Instance<GrizzlyHttpContainer> handlerInstance,
                                                    @ConfigurationValue("secure") final boolean secure,
                                                    final Instance<SSLEngineConfigurator> sslEngineConfiguratorInstance) {
    final HttpServer returnValue;
    if (handlerInstance == null || handlerInstance.isUnsatisfied()) {
      returnValue = null;
    } else {
      URI uri = null;
      try {
        uri = new URI("ignored", null /* no userInfo */, host, port, contextPath, null /* no query */, null /* no fragment */);
      } catch (final URISyntaxException uriSyntaxException) {
        throw new CreationException(uriSyntaxException);
      }
      final SSLEngineConfigurator sslEngineConfigurator;
      if (!secure || sslEngineConfiguratorInstance == null || sslEngineConfiguratorInstance.isUnsatisfied()) {
        sslEngineConfigurator = null;
      } else {
        sslEngineConfigurator = sslEngineConfiguratorInstance.get();
      }
      returnValue = GrizzlyHttpServerFactory.createHttpServer(uri, handlerInstance.get(), secure, sslEngineConfigurator, false);
    }
    return returnValue;
  }
  
}
