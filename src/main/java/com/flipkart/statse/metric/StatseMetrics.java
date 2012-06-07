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

package com.flipkart.statse.metric;

import com.flipkart.statse.datum.MetricDatum;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class StatseMetrics {
    private static final Logger LOG = LoggerFactory.getLogger(StatseMetrics.class);

    private static final Map<MetricName, Integer> gaugeValues = new ConcurrentHashMap<MetricName, Integer>();

    private StatseMetrics () { /* unused */ }

    public static void add(MetricDatum.Type type, String path, String name, int value) {
        MetricName metricName = getMetricName(type, path, name);
        switch (type) {
            case GAUGE:
                addGauge(metricName, value);
                break;
            case METER:
                addMeter(metricName, value);
                break;
            case HISTOGRAM:
                addHistogram(metricName, value);
                break;
            case TIMER:
                addTimer(metricName, value);
                break;

        }
    }

    /**
     * Given a path and a name, returns an instance of {@link MetricName}.
     * "statse" is used as the group.
     *
     * @param type The type of metric datum this to get the name for
     * @param path The path is used as the "type" of metric
     * @param name The name is used as the "name" of metric
     * @return An isntance of {@link MetricName}
     */
    private static MetricName getMetricName (MetricDatum.Type type, String path, String name) {
        // TODO: we should not be creating multiple instances of the same MetricName
        return new MetricName("statse", path, name + "." + type.name().toLowerCase());
    }

    private static void addGauge (final MetricName metricName, int value) {
        Integer oldValue = gaugeValues.get(metricName);
        gaugeValues.put(metricName, value);

        if (oldValue == null) {
            Metrics.newGauge(metricName, new Gauge<Integer>() {
                @Override
                public Integer value () {
                    return gaugeValues.get(metricName);
                }
            });
        }
    }

    private static void addMeter (final MetricName metricName, int value) {
        Meter meter = Metrics.newMeter(metricName, metricName.getName(), TimeUnit.SECONDS);
        meter.mark(value);
    }

    private static void addHistogram (final MetricName metricName, int value) {
        Histogram histogram = Metrics.newHistogram(metricName, true);
        histogram.update(value);
    }

    private static void addTimer (final MetricName metricName, int value) {
        Timer timer = Metrics.newTimer(metricName, TimeUnit.MILLISECONDS, TimeUnit.SECONDS);
        timer.update(value, TimeUnit.MILLISECONDS);
    }
}