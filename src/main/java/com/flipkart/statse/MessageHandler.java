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

import com.flipkart.statse.datum.MetricDatum;
import com.flipkart.statse.metric.StatseMetrics;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class MessageHandler extends SimpleChannelUpstreamHandler {
    private static final Logger LOG = LoggerFactory.getLogger(MessageHandler.class);

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
            throws Exception {

        Message msg = (Message) e.getMessage();
        LOG.trace("Processing message: {}", msg);
        List<String> paths = msg.getExplodedPaths();

        for (String path : paths) {
            for (MetricDatum metricDatum : msg.getMetricData()) {
                String name = metricDatum.getName();
                int value = metricDatum.getValue();
                MetricDatum.Type type = metricDatum.getType();

                StatseMetrics.add(type, path, name, value);
            }
        }
    }

    @SuppressWarnings({"ThrowableResultOfMethodCallIgnored"})
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
            throws Exception {

        LOG.error("Error when receiving message", e.getCause());
        // We don't close the channel because we can keep serving requests.
    }
}