/**
 * Copyright 2014 Flipkart Internet Pvt. Ltd.
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

package com.flipkart.statse;

import com.yammer.metrics.reporting.AdminServlet;
import com.yammer.metrics.reporting.GraphiteReporter;
import com.yammer.metrics.reporting.MetricsServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.jboss.netty.bootstrap.ConnectionlessBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.FixedReceiveBufferSizePredictorFactory;
import org.jboss.netty.channel.socket.DatagramChannelFactory;
import org.jboss.netty.channel.socket.oio.OioDatagramChannelFactory;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.Delimiters;
import org.jboss.netty.handler.execution.ExecutionHandler;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class Run {

    public static void main(String[] args) throws Exception {
        startUDPServer();
//        startGraphiteReporter();
        startHTTPReporter();
    }

    private static void startUDPServer () {
        DatagramChannelFactory f =
                new OioDatagramChannelFactory(Executors.newCachedThreadPool());
                // new NioDatagramChannelFactory(Executors.newCachedThreadPool());

        ConnectionlessBootstrap b = new ConnectionlessBootstrap(f);

        // Configure the pipeline factory
        b.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() throws Exception {
                return Channels.pipeline(
                        // apply a thread model — execute on a fixed thread pool
                        new ExecutionHandler(
                                Executors.newFixedThreadPool(
                                        Runtime.getRuntime().availableProcessors())),
                        // split by lines into separate messages
                        new DelimiterBasedFrameDecoder(1024, Delimiters.lineDelimiter()),
                        new MessageDecoder(),
                        new MessageHandler());
            }
        });

        // Disable broadcast
        b.setOption("broadcast", "false");

        // Allow packets as large as up to 1024 bytes (default is 768).
        // You could increase or decrease this value to avoid truncated packets
        // or to improve memory footprint respectively.
        //
        // Please also note that a large UDP packet might be truncated or
        // dropped by your router no matter how you configured this option.
        // In UDP, a packet is truncated or dropped if it is larger than a
        // certain size, depending on router configuration.  IPv4 routers
        // truncate and IPv6 routers drop a large packet.  That's why it is
        // safe to send small packets in UDP.
        b.setOption(
                "receiveBufferSizePredictorFactory",
                new FixedReceiveBufferSizePredictorFactory(1024));

        // Bump up the kernel UDP socket receive buffer size.
        //
        // 131072 is 128K which is Linux kernel's default max UDP receive buffer
        // size. The following command can bump this up to 32MB
        // sysctl -w net.core.rmem_max=33554432
        //
        // See http://splunk-base.splunk.com/answers/7001/udp-drops-on-linux for
        // more tweaks.
        b.setOption("receiveBufferSize", 131072);

        // Bind to the port and start the service.
        b.bind(new InetSocketAddress(2345));
    }

    private static void startGraphiteReporter () {
        GraphiteReporter.enable(10, TimeUnit.SECONDS,
                "ops-statsd.nm.flipkart.com", 2003);
    }

    private static void startHTTPReporter () throws Exception {
        Server server = new org.eclipse.jetty.server.Server(8080);

        ServletContextHandler context =
                new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        context.addServlet(AdminServlet.class, "");
        context.addServlet(MetricsServlet.class, "/metrics");

        server.start();
        server.join();
    }
}
