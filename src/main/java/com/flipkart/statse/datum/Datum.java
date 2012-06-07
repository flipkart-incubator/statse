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
 * A Datum represents a single point of data within a message.
 *
 * There are two types of Datums:
 *      Metric: A metric represents a numberic data point whose statistics we want to track
 *      Tag: A tag represents various axes along which we want to track statistics of metrics
 */
public interface Datum {
}
