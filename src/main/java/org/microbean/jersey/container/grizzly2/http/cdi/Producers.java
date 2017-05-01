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

import javax.ws.rs.ApplicationPath;

import javax.ws.rs.core.Application;

import org.microbean.configuration.cdi.annotation.ConfigurationValue;

import org.glassfish.grizzly.http.server.HttpServer;

import org.glassfish.grizzly.ssl.SSLEngineConfigurator;

import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpContainer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;

import org.glassfish.jersey.server.ContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class housing <a
 * href="http://docs.jboss.org/cdi/spec/2.0.EDR2/cdi-spec.html#producer_method">producer
 * methods</a> that manufacture certain Grizzly- and Jersey-related
 * objects, including {@link HttpServer} instances (by way of {@link
 * GrizzlyHttpServerFactory} instances) and {@link
 * GrizzlyHttpContainer} instances (by way of {@link ContainerFactory}
 * instances).
 *
 * <p>For this class to be useful, a CDI container must be capable of
 * locating or otherwise supplying {@link SSLEngineConfigurator}
 * instances.  See the <a
 * href="https://ljnelson.github.io/microbean-grizzly-http-server-cdi/"
 * target="_parent">MicroBean Grizzly HTTP Server CDI Integration</a>
 * project for an example of a project that supplies such
 * instances.</p>
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 */
@ApplicationScoped
class Producers {


  /*
   * Static fields.
   */
  

  /**
   * A {@link Logger} for use by instances of this class.
   *
   * <p>This field is never {@code null}.</p>
   *
   * @see LoggerFactory#getLogger(Class)
   */
  private static final Logger logger = LoggerFactory.getLogger(Producers.class);


  /*
   * Constructors.
   */

  
  /**
   * Creates a new {@link Producers}.
   */
  private Producers() {
    super();
  }

  /**
   * Creates and returns a new {@link GrizzlyHttpContainer} if the
   * supplied {@link Instance} containing {@link Application}
   * instances is {@link Instance#isUnsatisfied() not unsatisfied}.
   *
   * <p>This method may return {@code null}.</p>
   *
   * @param applicationInstance an {@link Instance} {@linkplain
   * Instance#get() housing} an {@link Application}; may be {@code
   * null}
   *
   * @return a {@link GrizzlyHttpContainer}, or {@code null}
   *
   * @see ContainerFactory#createContainer(Class, Application)
   */
  @Produces
  @Dependent
  private static final GrizzlyHttpContainer produceGrizzlyHttpContainer(final Instance<Application> applicationInstance) {
    if (logger.isTraceEnabled()) {
      logger.trace("ENTRY {} {} {}", Producers.class.getName(), "produceGrizzlyHttpContainer", applicationInstance);
    }
    final GrizzlyHttpContainer returnValue;
    if (applicationInstance == null || applicationInstance.isUnsatisfied()) {
      returnValue = null;
    } else {
      returnValue = ContainerFactory.createContainer(GrizzlyHttpContainer.class, applicationInstance.get());
      if (logger.isInfoEnabled()) {
        logger.info("Created GrizzlyHttpContainer: {}", returnValue);
      }
    }
    if (logger.isTraceEnabled()) {
      logger.trace("EXIT {} {} {}", Producers.class.getName(), "produceGrizzlyHttpContainer", returnValue);
    }
    return returnValue;
  }

  /**
   * Creates and returns a new {@link HttpServer} if the supplied
   * {@link Instance} containing a {@link GrizzlyHttpContainer} is
   * non-{@code null} and {@link Instance#isUnsatisfied() not
   * unsatisfied}.
   *
   * <p>This method may return {@code null}.</p>
   *
   * <p>Note that the the supplied {@link Instance} containing a
   * {@link GrizzlyHttpContainer} may be non-{@code null} and {@link
   * Instance#isUnsatisfied() not unsatisfied} and yet may still
   * produce a {@code null} {@link GrizzlyHttpContainer}.  In this
   * case, a suitable warning will be logged, but {@link HttpServer}
   * creation will proceed anyways.</p>
   *
   * @param host the address of the network interface to listen on;
   * may be {@code null} in which case {@code 0.0.0.0} will be used
   * instead
   *
   * @param port the port to listen on; {@code 8080} by default
   *
   * @param contextPath where the resulting web application should be
   * rooted; may be {@code null} in which case {@code /} will be used
   * instead
   *
   * @param containerInstance an {@link Instance} {@linkplain
   * Instance#get() housing} a {@link GrizzlyHttpContainer} instance
   * (such as might be produced by the {@link
   * #produceGrizzlyHttpContainer(Instance)} method); may be {@code
   * null} or {@linkplain Instance#isUnsatisfied() unsatisfied} in
   * which case {@code null} will be returned
   *
   * @param secure whether the communication with the server will be
   * secure or not
   *
   * @param sslEngineConfiguratorInstance an {@link Instance}
   * {@linkplain Instance#get() housing} an {@link
   * SSLEngineConfigurator}; may be {@code null} or {@linkplain
   * Instance#isUnsatisfied() unsatisfied} in which case an {@link
   * SSLEngineConfigurator} instance will not be passed to the {@link
   * GrizzlyHttpServerFactory#createHttpServer(URI,
   * GrizzlyHttpContainer, boolean, SSLEngineConfigurator, boolean)}
   * method
   *
   * @return an {@link HttpServer}, or {@code null}
   *
   * @see #produceGrizzlyHttpContainer(Instance)
   *
   * @see <a
   * href="https://github.com/ljnelson/microbean-jersey-container-grizzly2-http-cdi/issues/1">Issue
   * #1</a>
   *
   * @see GrizzlyHttpContainer
   *
   * @see GrizzlyHttpServerFactory#createHttpServer(URI,
   * GrizzlyHttpContainer, boolean, SSLEngineConfigurator, boolean)
   */
  @Produces
  @Dependent
  private static final HttpServer produceHttpServer(@ConfigurationValue(value = "host", defaultValue = "0.0.0.0") final String host,
                                                    @ConfigurationValue(value = "port", defaultValue = "8080") final int port,
                                                    @ConfigurationValue(value = "contextPath") final String contextPath,
                                                    final Instance<GrizzlyHttpContainer> containerInstance,
                                                    @ConfigurationValue("secure") final boolean secure,
                                                    final Instance<SSLEngineConfigurator> sslEngineConfiguratorInstance) {
    if (logger.isTraceEnabled()) {
      logger.trace("ENTRY {} {} {}, {}, {}, {}, {}, {}", Producers.class.getName(), "produceHttpServer", host, port, contextPath, containerInstance, secure, sslEngineConfiguratorInstance);
    }
    final HttpServer returnValue;
    if (containerInstance == null || containerInstance.isUnsatisfied()) {
      returnValue = null;
    } else {

      final String applicationContextPath;
      final GrizzlyHttpContainer container = containerInstance.get();
      if (container == null) {
        if (contextPath == null) {
          applicationContextPath = "/";
        } else {
          applicationContextPath = contextPath;
        }
        if (logger.isWarnEnabled()) {
          logger.warn("No GrizzlyHttpContainer present");
        }
      } else if (contextPath == null) {
        Object application = container.getConfiguration();
        if (application == null) {
          applicationContextPath = "/";
        } else {
          ApplicationPath applicationPath = null;
          Class<?> applicationClass = application.getClass();
          while (applicationPath == null && applicationClass != null) {
            if (applicationClass.isSynthetic()) {
              applicationClass = applicationClass.getSuperclass();
            } else {
              applicationPath = applicationClass.getAnnotation(ApplicationPath.class);
              if (applicationPath == null) {
                if (application instanceof ResourceConfig) {
                  application = ((ResourceConfig)application).getApplication();
                  if (application == null) {
                    applicationClass = null;
                  } else {
                    applicationClass = application.getClass();
                  }
                } else if (applicationClass.isSynthetic()) {
                  applicationClass = applicationClass.getSuperclass();
                } else {
                  applicationClass = null;
                }
              }
            }
          }
          if (applicationPath == null) {
            applicationContextPath = "/";
          } else {
            final String applicationPathValue = applicationPath.value();
            if (applicationPathValue == null || applicationPathValue.isEmpty()) {
              applicationContextPath = "/";
            } else {
              applicationContextPath = applicationPathValue;
            }
          }
        }
      } else {
        applicationContextPath = contextPath;
      }
      assert applicationContextPath != null;
      
      URI uri = null;
      try {
        uri = new URI("ignored", null /* no userInfo */, host, port, applicationContextPath, null /* no query */, null /* no fragment */);
      } catch (final URISyntaxException uriSyntaxException) {
        throw new CreationException(uriSyntaxException);
      }

      final SSLEngineConfigurator sslEngineConfigurator;
      if (!secure || sslEngineConfiguratorInstance == null || sslEngineConfiguratorInstance.isUnsatisfied()) {
        sslEngineConfigurator = null;
      } else {
        sslEngineConfigurator = sslEngineConfiguratorInstance.get();
      }

      returnValue = GrizzlyHttpServerFactory.createHttpServer(uri, container, secure, sslEngineConfigurator, false);
      if (logger.isInfoEnabled()) {
        logger.info("Created HttpServer: {}", returnValue);
      }
    }
    if (logger.isTraceEnabled()) {
      logger.trace("EXIT {} {} {}", Producers.class.getName(), "produceHttpServer", returnValue);
    }
    return returnValue;
  }
  
}
