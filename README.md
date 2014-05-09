StatsE is a centralised multi-dimensional metrics aggregator.

This idea was (AFAIK) first [described by Flickr](http://code.flickr.com/blog/2008/10/27/counting-timing/) as StatsD. It
was later on [popularised by Etsy](http://codeascraft.etsy.com/2011/02/15/measure-anything-measure-everything/). Reading
their descriptions would be a good way to understand the idea and motivation behind StatsE. See below for the motivation
behind creating this alternative system instead of just using one of these (open source) StatsD implementations.

StasE is <i>Stats</i>D, <i>E</i>nhanced. The primary enhancement is the notion of multi-dimensionality.


Why StatsE?
----------

Both Flickr and Etsy have open-sourced their implementations of statsd:

* https://github.com/etsy/statsd (implemented using NodeJS)
* https://github.com/iamcal/Flickr-StatsD (implemented in Perl)

There are a few other implementations available as well. Given this, why did we choose to write yet another
implementation instead of using one of the existing implementations? There are a few reasons:

* As well written as they are, the existing statsd implementations get overwhelmed by a large amount of incoming stats
sooner than we would like them to. Etsy's statsd does allow the client to send only a sample of data points to work
around this issue. But we believe that this kind of simplistic sampling does not retain correct distribution of the data
points (hosts could be receiving disproportional amount of data, for example); sampling techniques such as Reservoir
sampling done on the aggregator based on the data received from across different servers would be more representative.
Sampling on the aggregator also leads to less jerky, smoother graphs which are more useful for looking at trends in behaviour.

* We wanted to pack and send more data within each UDP packet. There are many dimensions to every data-point and
including this information without having to send multiple UDP packets makes life simpler.

* The existing solutions don't utilize all the CPU cores on the hosts they are run on. This is mostly an artifact of the
platform they are built on (NodeJS, Perl).

* We wanted a solution that fits into our deployment platform of choice (JVM).

* It was a lot of fun writing this :)


Usage
-----

### Build & Run

    mvn package
    java -jar target/statse-*.jar

### Protocol

StatsE is a UDP server which accepts a set of (related) metrics in each UDP messages. A message is comprised of the
following parts:

* A namespace, to group related metrics together.
  * For example, "com.flipkart.website.search"
* One or more metric name + metric type + metric value.
  * For example, [requests, meter, 1], [response-time, timer, 154], [number-of-results, histogram, 3425]
* One of more dimensions to slice & dice the metrics by.
  * For example, host:search1, index:books

### Message Format

The general format of a message is this:

    <period>.<separated>.<namespace> <metrics-go-here> [tags-go-here]

The format of a metric:

    <metric-name>:<metric-value>|<metric-type>

    <metric-name>: a descriptive name for the metric; can contain alphanumerics and some special chars (_, -, .)
    <metric-value>: a positive integer
    <metric-type>: one of g, m, h, t, ms
                     g => Gauge
                     m => Meter
                     h => Histogram
                     t => Timer (milliseconds)

The format of a tag:

    <tag-name>:<tag-value>

    <tag-name>: a descriptive name for the metric; can contain alphanumerics and some special chars (_, -, .)
    <tag-value>: the tag value; can contain alphanumerics and some special chars (_, -, .)

Each message must contain at least one metric and can contain any number of tags. Times are assumed to be in milliseconds.

#### StatsD Compatibility

StatsE is backward compatible with StatsD. So any valid StatsD message is also a valid StatsE message. This allows you
to use any of the existing StatsD clients to send data to StatsE.

### Example

Install `netcat` and you'll be able to send messages to StatsE from command-line:

    echo "com.flipkart.website.search requests:1|m response-time:154|t host:search1 index:books" | nc -u -w0 127.0.0.1 2345

This assumes that StatsE is running on localhost.

Future Work
-----------

* Externalise the config that is currently hard-coded.
* Allow picking and choosing reporters to use.
* Enable creation of a "data cube".
* TCP daemon in addition to the UDP daemon.
* Clients for various platforms (Java, PHP, JRuby etc.)
* A better Graphite reporter.
* Add multiple ports for UDP server.


Contribution, Bugs and Feedback
-------------------------------

For bugs, questions and discussions please use the [Github Issues](https://github.com/flipkart-incubator/Iris-BufferQueue/issues).

Please follow the [contribution guidelines](https://github.com/flipkart-incubator/Iris-BufferQueue/blob/master/CONTRIBUTING.md) when submitting pull requests.


LICENSE
-------

Copyright 2014 Flipkart Internet Pvt. Ltd.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
