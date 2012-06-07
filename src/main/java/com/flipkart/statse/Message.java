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

import com.flipkart.statse.datum.Datum;
import com.flipkart.statse.datum.MetricDatum;
import com.flipkart.statse.datum.TagDatum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Message {
    private String path;
    private List<MetricDatum> metricData;
    private List<TagDatum> tagData;

    public Message () {
        metricData = new ArrayList<MetricDatum>();
        tagData = new ArrayList<TagDatum>();
    }

    public String getPath () {
        return path;
    }

    public void setPath (String path) {
        this.path = path;
    }

    public void addData (Datum datum) {
        if (datum == null) {
            return;
        }

        if (datum instanceof MetricDatum) {
            addMetric((MetricDatum) datum);
        }
        else if (datum instanceof TagDatum) {
            addTag((TagDatum) datum);
        }
    }

    public void addMetric (MetricDatum metricDatum) {
        metricData.add(metricDatum);
    }

    public List<MetricDatum> getMetricData () {
        return metricData;
    }

    public void addTag (TagDatum tagDatum) {
        tagData.add(tagDatum);
    }

    public List<TagDatum> getTagData () {
        return tagData;
    }

    public List<String> getExplodedPaths () {
        if (path == null) {
            return Arrays.asList(new String[] {""});
        }

        List<String> paths = new ArrayList<String>(1 + tagData.size());
        paths.add(path + "._all");

        for (TagDatum tagDatum : getTagData()) {
            paths.add(path + "._by_" + tagDatum.getName() + "." + tagDatum.getValue());
        }

        return paths;
    }

    @Override
    public String toString () {
        return "Message{" +
                "path='" + path + '\'' +
                ", metricData=" + metricData +
                ", tagData=" + tagData +
                '}';
    }
}
