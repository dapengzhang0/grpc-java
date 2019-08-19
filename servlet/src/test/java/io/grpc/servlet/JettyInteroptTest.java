/*
 * Copyright 2019 The gRPC Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.grpc.servlet;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.ServerBuilder;
import io.grpc.internal.AbstractManagedChannelImplBuilder;
import io.grpc.testing.integration.AbstractInteropTest;
import org.eclipse.jetty.http2.server.HTTP2CServerConnectionFactory;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

public class JettyInteroptTest extends AbstractInteropTest {

  private static final String HOST = "localhost";
  private static final String MYAPP = "/grpc.testing.TestService";
  private int port;
  private Server server;

  @After
  @Override
  public void tearDown() {
    super.tearDown();
    try {
      server.stop();
    } catch (Exception e) {
      throw new AssertionError(e);
    }
  }

  @Override
  protected ServerBuilder<?> getServerBuilder() {
    return new ServletServerBuilder().maxInboundMessageSize(AbstractInteropTest.MAX_MESSAGE_SIZE);
  }

  @Override
  protected void startServer(ServerBuilder<?> builer) {
    GrpcServlet grpcServlet =
        new GrpcServlet(((ServletServerBuilder) builer).buildServletAdapter());
    server = new Server(0);
    ServerConnector sc = (ServerConnector)server.getConnectors()[0];
    sc.addConnectionFactory(new HTTP2CServerConnectionFactory(new HttpConfiguration()));
    ServletContextHandler context =
        new ServletContextHandler(ServletContextHandler.SESSIONS);
    context.setContextPath(MYAPP);
    context.addServlet(new ServletHolder(grpcServlet), "/*");
    server.setHandler(context);

    try {
      server.start();
    } catch (Exception e) {
      throw new AssertionError(e);
    }

    port = sc.getLocalPort();
  }


  @Override
  protected ManagedChannel createChannel() {
    AbstractManagedChannelImplBuilder<?> builder =
        (AbstractManagedChannelImplBuilder<?>) ManagedChannelBuilder.forAddress(HOST, port)
            .usePlaintext()
            .maxInboundMessageSize(AbstractInteropTest.MAX_MESSAGE_SIZE);
    io.grpc.internal.TestingAccessor.setStatsImplementation(
        builder, createClientCensusStatsModule());
    return builder.build();
  }

  // FIXME
  @Override
  @Ignore("Jetty is broken on client GOAWAY")
  @Test
  public void gracefulShutdown() {}
}
