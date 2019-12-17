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

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ClientInterceptors;
import io.grpc.Grpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.MethodDescriptor;
import io.grpc.examples.helloworld.GreeterGrpc;
import io.grpc.examples.helloworld.HelloReply;
import io.grpc.examples.helloworld.HelloRequest;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

/** An example gRPC client for use in demonstrating xDS functionality. */
public final class HelloWorldXdsClient {
  private static final Logger logger = Logger.getLogger(HelloWorldXdsClient.class.getName());

  private String address = "localhost:50051";
  private int time = 1;

  public static void main(String[] args) throws InterruptedException {
    new HelloWorldXdsClient().run(args);
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
      if ("address".equals(key)) {
        address = value;
      } else if ("time".equals(key)) {
        time = Integer.parseInt(value);
      } else {
        System.err.println("Unknown argument: " + key);
        usage = true;
        break;
      }
    }
    if (usage) {
      HelloWorldXdsClient c = new HelloWorldXdsClient();
      System.out.println(
          "Usage: [ARGS...]"
              + "\n"
              + "\n  --address=SERVER_ADDRESS        Server address to connect to. Default "
              + c.address
              + "\n  --time=SECONDS_TO_RUN           Duration to run in seconds. Default "
              + c.time);
      System.exit(1);
    }
  }

  private void run(String[] args) throws InterruptedException {
try{ 
      String loggingConfig =
	  "handlers=java.util.logging.ConsoleHandler\n"
	      + "io.grpc.xds.level=FINEST\n"
	      + "io.grpc.ChannelLogger.level=FINEST\n"
	      + "java.util.logging.ConsoleHandler.level=FINEST\n"
	      + "java.util.logging.ConsoleHandler.formatter=java.util.logging.SimpleFormatter";
      java.util.logging.LogManager.getLogManager()
	  .readConfiguration(
	      new java.io.ByteArrayInputStream(
		  loggingConfig.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
} catch (Throwable t) {
}
    parseArgs(args);
    ManagedChannel managedChannel = ManagedChannelBuilder.forTarget(address).usePlaintext().build();
    RemoteAddressInterceptor interceptor = new RemoteAddressInterceptor();
    Channel channel = ClientInterceptors.intercept(managedChannel, interceptor);
    GreeterGrpc.GreeterBlockingStub stub = GreeterGrpc.newBlockingStub(channel).withWaitForReady();
    try {
      while (time-- > 0) {
        HelloReply response = stub.sayHello(HelloRequest.newBuilder().setName("world").build());
        System.out.println(
            "Greeting: "
                + response.getMessage()
                + ", from "
                + interceptor.callRef.get().getAttributes().get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR));
        TimeUnit.SECONDS.sleep(1);
      }
    } finally {
      managedChannel.shutdown();
      managedChannel.awaitTermination(1, TimeUnit.SECONDS);
    }
  }

  private static class RemoteAddressInterceptor implements ClientInterceptor {
    private final AtomicReference<ClientCall<?, ?>> callRef =
        new AtomicReference<ClientCall<?, ?>>();

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
        MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
      ClientCall<ReqT, RespT> call = next.newCall(method, callOptions);
      callRef.set(call);
      return call;
    }
  }
}
