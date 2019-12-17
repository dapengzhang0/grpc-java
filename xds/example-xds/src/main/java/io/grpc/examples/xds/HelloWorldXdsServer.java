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

package io.grpc.examples.xds;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.examples.helloworld.GreeterGrpc;
import io.grpc.examples.helloworld.HelloReply;
import io.grpc.examples.helloworld.HelloRequest;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Logger;

/** An example gRPC server for use in demonstrating xDS functionality. */
public final class HelloWorldXdsServer {
  private static final Logger logger = Logger.getLogger(HelloWorldXdsServer.class.getName());

  private int port = 50051;

  public static void main(String[] args) throws IOException, InterruptedException {
    new HelloWorldXdsServer().start(args);
  }

  private void parseArgs(String[] args) {
    boolean usage = false;
    for (String arg : args) {
      if (!arg.startsWith("--")) {
        System.err.println("All arguments must start with '--': " + arg);
        usage = true;
        break;
      }
      String[] parts = arg.substring(2).split("=", 2);
      String key = parts[0];
      if ("help".equals(key)) {
        usage = true;
        break;
      }
      if (parts.length != 2) {
        System.err.println("All arguments must be of the form --arg=value");
        usage = true;
        break;
      }
      String value = parts[1];
      if ("port".equals(key)) {
        port = Integer.parseInt(value);
      } else {
        System.err.println("Unknown argument: " + key);
        usage = true;
        break;
      }
    }
    if (usage) {
      HelloWorldXdsServer c = new HelloWorldXdsServer();
      System.out.println(
          "Usage: [ARGS...]"
              + "\n"
              + "\n  --port=PORT        Server port to bind to. Default "
              + c.port);
      System.exit(1);
    }
  }

  private void start(String[] args) throws IOException, InterruptedException {
    parseArgs(args);
    Server server = ServerBuilder.forPort(port).addService(new GreeterImpl()).build();
    server.start();
    logger.info("Started on port " + port);
    server.awaitTermination();
  }

  private static class GreeterImpl extends GreeterGrpc.GreeterImplBase {
    @Override
    public void sayHello(HelloRequest req, StreamObserver<HelloReply> responseObserver) {
      String host;
      try {
        host = InetAddress.getLocalHost().getHostName();
      } catch (UnknownHostException e) {
        host = "failed to get host " + e;
      }
      HelloReply reply =
          HelloReply.newBuilder()
              .setMessage("Hello " + req.getName() + ", this is " + host)
              .build();
      responseObserver.onNext(reply);
      responseObserver.onCompleted();
    }
  }
}
