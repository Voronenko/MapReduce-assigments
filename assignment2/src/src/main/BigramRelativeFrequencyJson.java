/*
 * Cloud9: A Hadoop toolkit for working with big data
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

import java.io.IOException;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;

import edu.umd.cloud9.io.JsonWritable;

public class BigramRelativeFrequencyJson extends Configured implements Tool {

  private static final Logger LOG = Logger.getLogger(BigramRelativeFrequencyJson.class);

  // Define custom intermediate key; must specify sort order.
  public static class MyTuple extends JsonWritable implements WritableComparable<MyTuple> {
    public int compareTo(MyTuple that) {
      String thisLeft = this.getJsonObject().get("Left").getAsString();
      String thatLeft = that.getJsonObject().get("Left").getAsString();

      if (thisLeft.equals(thatLeft)) {
        String thisRight = this.getJsonObject().get("Right").getAsString();
        String thatRight = that.getJsonObject().get("Right").getAsString();

        return thisRight.compareTo(thatRight);
      }
      return thisLeft.compareTo(thatLeft);
    }
  }

  protected static class MyMapper extends Mapper<LongWritable, Text, MyTuple, FloatWritable> {
    private static final FloatWritable ONE = new FloatWritable(1);
    private static final MyTuple BIGRAM = new MyTuple();

    @Override
    public void map(LongWritable key, Text value, Context context)
        throws IOException, InterruptedException {
      String line = value.toString();

      String prev = null;
      StringTokenizer itr = new StringTokenizer(line);
      while (itr.hasMoreTokens()) {
        String cur = itr.nextToken();

        // Emit only if we have an actual bigram.
        if (prev != null) {

          // Simple way to truncate tokens that are too long.
          if (cur.length() > 100) {
            cur = cur.substring(0, 100);
          }

          if (prev.length() > 100) {
            prev = prev.substring(0, 100);
          }

          BIGRAM.getJsonObject().addProperty("Left", prev);
          BIGRAM.getJsonObject().addProperty("Right", cur);
          context.write(BIGRAM, ONE);

          BIGRAM.getJsonObject().addProperty("Left", prev);
          BIGRAM.getJsonObject().addProperty("Right", "*");
          context.write(BIGRAM, ONE);
        }
        prev = cur;
      }
    }
  }

  protected static class MyCombiner extends Reducer<MyTuple, FloatWritable, MyTuple, FloatWritable> {
    private final static FloatWritable SUM = new FloatWritable();

    @Override
    public void reduce(MyTuple key, Iterable<FloatWritable> values, Context context)
        throws IOException, InterruptedException {
      int sum = 0;
      Iterator<FloatWritable> iter = values.iterator();
      while (iter.hasNext()) {
        sum += iter.next().get();
      }
      SUM.set(sum);
      context.write(key, SUM);
    }
  }

  protected static class MyReducer extends Reducer<MyTuple, FloatWritable, MyTuple, FloatWritable> {
    private static final FloatWritable VALUE = new FloatWritable();
    private float marginal = 0.0f;

    @Override
    public void reduce(MyTuple key, Iterable<FloatWritable> values, Context context)
        throws IOException, InterruptedException {
      float sum = 0.0f;
      Iterator<FloatWritable> iter = values.iterator();
      while (iter.hasNext()) {
        sum += iter.next().get();
      }

      if (key.getJsonObject().get("Right").getAsString().equals("*")) {
        VALUE.set(sum);
        context.write(key, VALUE);
        marginal = sum;
      } else {
        VALUE.set(sum / marginal);
        context.write(key, VALUE);
      }
    }
  }

  protected static class MyPartitioner extends Partitioner<MyTuple, FloatWritable> {
    @Override
    public int getPartition(MyTuple key, FloatWritable value, int numReduceTasks) {
      return (key.getJsonObject().get("Left").getAsString().hashCode() & Integer.MAX_VALUE)
          % numReduceTasks;
    }
  }

  private BigramRelativeFrequencyJson() {
  }

  private static int printUsage() {
    System.out.println("usage: [input-path] [output-path] [num-reducers]");
    ToolRunner.printGenericCommandUsage(System.out);
    return -1;
  }

  /**
   * Runs this tool.
   */
  public int run(String[] args) throws Exception {
    if (args.length != 3) {
      printUsage();
      return -1;
    }

    String inputPath = args[0];
    String outputPath = args[1];
    int reduceTasks = Integer.parseInt(args[2]);

    LOG.info("Tool name: " + BigramRelativeFrequencyJson.class.getSimpleName());
    LOG.info(" - input path: " + inputPath);
    LOG.info(" - output path: " + outputPath);
    LOG.info(" - num reducers: " + reduceTasks);

    Job job = Job.getInstance(getConf());
    job.setJobName(BigramRelativeFrequencyJson.class.getSimpleName());
    job.setJarByClass(BigramRelativeFrequencyJson.class);

    job.setNumReduceTasks(reduceTasks);

    FileInputFormat.setInputPaths(job, new Path(inputPath));
    FileOutputFormat.setOutputPath(job, new Path(outputPath));

    job.setMapOutputKeyClass(MyTuple.class);
    job.setMapOutputValueClass(FloatWritable.class);
    job.setOutputKeyClass(MyTuple.class);
    job.setOutputValueClass(FloatWritable.class);
    job.setOutputFormatClass(SequenceFileOutputFormat.class);

    job.setMapperClass(MyMapper.class);
    job.setCombinerClass(MyCombiner.class);
    job.setReducerClass(MyReducer.class);
    job.setPartitionerClass(MyPartitioner.class);

    // Delete the output directory if it exists already.
    Path outputDir = new Path(outputPath);
    FileSystem.get(getConf()).delete(outputDir, true);

    long startTime = System.currentTimeMillis();
    job.waitForCompletion(true);
    System.out.println("Job Finished in " + (System.currentTimeMillis() - startTime) / 1000.0
        + " seconds");

    return 0;
  }

  /**
   * Dispatches command-line arguments to the tool via the {@code ToolRunner}.
   */
  public static void main(String[] args) throws Exception {
    ToolRunner.run(new BigramRelativeFrequencyJson(), args);
  }
}
