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

package com.flipkart.statse.datum;

/**
 * A MetricDatum represents a numeric data point we want to track statistics for.
 *
 * There are four types of metrics:
 *      GAUGE: A gauge measures the instantaneous measurement of a value over time
 *      METER: A meter measures the rate of events over time (e.g., "requests per second")
 *      HISTOGRAM: A histogram measures the statistical distribution of values in a stream of data
 *      TIMER: A timer measures both the rate that a particular piece of code is called and the distribution of its duration
 * @see <a href="http://metrics.codahale.com/getting-started/">Documentation for Coda Hale's Metrics package</a>
 */
public class MetricDatum implements Datum {

    public enum Type {GAUGE, METER, HISTOGRAM, TIMER}

    private final String name;
    private final int value;
    private Type type;

    public MetricDatum (String name, int value, Type type) {
        this.name = name;
        this.value = value;
        this.type = type;
    }

    public String getName () {
        return name;
    }

    public int getValue () {
        return value;
    }

    public Type getType () {
        return type;
    }

    @Override
    public String toString () {
        return "MetricDatum{" +
                "name='" + name + '\'' +
                ", value=" + value +
                ", type=" + type +
                '}';
    }
}
