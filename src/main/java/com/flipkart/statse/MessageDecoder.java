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

import com.google.common.collect.ImmutableMap;
import com.flipkart.statse.datum.Datum;
import com.flipkart.statse.datum.MetricDatum;
import com.flipkart.statse.datum.TagDatum;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.jboss.netty.util.CharsetUtil;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class embodies the logic for decoding a message.
 *
 * The general format of a message is this:
 * <period>.<separated>.<namespace> <metrics-go-here> [tags-go-here]
 *
 * The format of a metric:
 * <metric-name>:<metric-value>|<metric-type>
 *     <metric-name>: a descriptive name for the metric; can contain alphanumerics and some special chars (_, -, .)
 *     <metric-value>: a positive integer
 *     <metric-type>: one of g, m, h, t, ms
 *                      g => Gauge
 *                      m => Meter
 *                      c => Meter (StatsD backward compatibility)
 *                      h => Histogram
 *                      t => Timer
 *                      ms => Timer (StatsD backward compatibility)
 *
 * The format of a tag:
 * <tag-name>:<tag-value>
 *     <tag-name>: a descriptive name for the metric; can contain alphanumerics and some special chars (_, -, .)
 *     <tag-value>: the tag value; can contain alphanumerics and some special chars (_, -, .)
 *
 * Each message must contain at least one metric and can contain any number of tags.
 *
 * The namespace itself can be skipped. But this is usually not a good idea.
 * It is allowed primarily so that this message format is compatible with
 * Etsy's statsd which is already in use at a bunch of places (and so has
 * clients deployed).
 */
public class MessageDecoder extends FrameDecoder {

    private static final Pattern splitPattern = Pattern.compile("\\s+");
    private static final Pattern metricPattern = Pattern.compile("(\\w+):(\\d+)\\|(\\w+)");
    private static final Pattern tagPattern = Pattern.compile("(\\w+):(\\w+)");

    private static final Map<String, MetricDatum.Type> typeMap
            = ImmutableMap.<String, MetricDatum.Type>builder()
                .put("g", MetricDatum.Type.GAUGE)
                .put("m", MetricDatum.Type.METER)
                .put("c", MetricDatum.Type.METER)
                .put("h", MetricDatum.Type.HISTOGRAM)
                .put("t", MetricDatum.Type.TIMER)
                .put("ms", MetricDatum.Type.TIMER)
                .build();

    @Override
    protected Message decode (ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) throws Exception {
        String msgString = buffer.readBytes(buffer.readableBytes()).toString(CharsetUtil.US_ASCII);
        Message msg = new Message();

        String[] parts = splitPattern.split(msgString);
        int i = 0;
        
        if (!parts[0].contains(":")) {
            // There is a namespace; we're _not_ in StatsD compatible mode
            msg.setPath(parts[i]);
            i++;
        }
        
        for (; i < parts.length; i++) {
            Datum datum = decodeDatum(parts[i]);
            msg.addData(datum);
        }
        
        return msg;
    }

    /**
     * Decodes a single datum i.e. either a metric or a tag.
     *
     * @param datumStr The metric/tag string to decode
     * @return An object of {@link Datum} representing the parsed metric/tag; null if we're unable to parse
     */
    private Datum decodeDatum (String datumStr) {
        Matcher matcher = metricPattern.matcher(datumStr);
        if (matcher.matches()) {
            MetricDatum.Type type = typeMap.get(matcher.group(3));
            if (type == null) {
                return null;
            }
            
            String name = matcher.group(1);
            int value = Integer.parseInt(matcher.group(2));
            
            return new MetricDatum(name, value, type);
        }
        
        matcher = tagPattern.matcher(datumStr);
        if (matcher.matches()) {
            String name = matcher.group(1);
            String value = matcher.group(2);
            
            return new TagDatum(name, value);
        }
        
        return null;
    }
}
