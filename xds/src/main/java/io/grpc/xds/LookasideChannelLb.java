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

package io.grpc.xds;

import static java.util.logging.Level.FINEST;

import io.grpc.LoadBalancer;
import io.grpc.LoadBalancer.Helper;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.xds.XdsComms.AdsStreamCallback;
import java.util.logging.Logger;

/** A load balancer that has a lookaside channel. */
final class LookasideChannelLb extends LoadBalancer {

  LookasideChannelLb(Helper helper, AdsStreamCallback adsCallback, String balancerName) {

  }

  private static ManagedChannel initLbChannel(Helper helper, String balancerName) {
    ManagedChannel channel;
    try {
      channel = helper.createResolvingOobChannel(balancerName);
    } catch (UnsupportedOperationException uoe) {
      // Temporary solution until createResolvingOobChannel is implemented
      // FIXME (https://github.com/grpc/grpc-java/issues/5495)
      Logger logger = Logger.getLogger(XdsLoadBalancer.class.getName());
      if (logger.isLoggable(FINEST)) {
        logger.log(
            FINEST,
            "createResolvingOobChannel() not supported by the helper: " + helper,
            uoe);
        logger.log(
            FINEST,
            "creating oob channel for target {0} using default ManagedChannelBuilder",
            balancerName);
      }
      channel = ManagedChannelBuilder.forTarget(balancerName).build();
    }
    return channel;
  }

  @Override
  public void handleNameResolutionError(Status error) {

  }

  @Override
  public void shutdown() {

  }
}
