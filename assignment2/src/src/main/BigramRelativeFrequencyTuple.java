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
import org.apache.pig.backend.executionengine.ExecException;
import org.apache.pig.data.BinSedesTuple;
import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;

public class BigramRelativeFrequencyTuple extends Configured implements Tool {
  private static final Logger LOG = Logger.getLogger(BigramRelativeFrequencyTuple.class);
  private static final TupleFactory TUPLE_FACTORY = TupleFactory.getInstance();

  protected static class MyMapper extends Mapper<LongWritable, Text, Tuple, FloatWritable> {
    private static final FloatWritable ONE = new FloatWritable(1);

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

          Tuple tuple1 = TUPLE_FACTORY.newTuple();
          tuple1.append(prev);
          tuple1.append(cur);
          context.write(tuple1, ONE);

          Tuple tuple2 = TUPLE_FACTORY.newTuple();
          tuple2.append(prev);
          tuple2.append("*");
          context.write(tuple2, ONE);
        }
        prev = cur;
      }
    }
  }

  protected static class MyCombiner extends Reducer<Tuple, FloatWritable, Tuple, FloatWritable> {
    private final static FloatWritable SUM = new FloatWritable();

    @Override
    public void reduce(Tuple key, Iterable<FloatWritable> values, Context context)
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

  protected static class MyReducer extends Reducer<Tuple, FloatWritable, Tuple, FloatWritable> {
    private static final FloatWritable VALUE = new FloatWritable();
    private float marginal = 0.0f;

    @Override
    public void reduce(Tuple key, Iterable<FloatWritable> values, Context context)
        throws IOException, InterruptedException {
      float sum = 0.0f;
      Iterator<FloatWritable> iter = values.iterator();
      while (iter.hasNext()) {
        sum += iter.next().get();
      }

      if (key.get(1).equals("*")) {
        VALUE.set(sum);
        context.write(key, VALUE);
        marginal = sum;
      } else {
        VALUE.set(sum / marginal);
        context.write(key, VALUE);
      }
    }
  }

  protected static class MyPartitioner extends Partitioner<Tuple, FloatWritable> {
    @Override
    public int getPartition(Tuple key, FloatWritable value, int numReduceTasks) {
      try {
        return (((String) key.get(0)).hashCode() & Integer.MAX_VALUE) % numReduceTasks;
      } catch (ExecException e) {
        e.printStackTrace();
        return 0;
      }
    }
  }

  private BigramRelativeFrequencyTuple() {}

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

    LOG.info("Tool name: " + BigramRelativeFrequencyTuple.class.getSimpleName());
    LOG.info(" - input path: " + inputPath);
    LOG.info(" - output path: " + outputPath);
    LOG.info(" - num reducers: " + reduceTasks);

    Job job = Job.getInstance(getConf());
    job.setJobName(BigramRelativeFrequencyTuple.class.getSimpleName());
    job.setJarByClass(BigramRelativeFrequencyTuple.class);

    job.setNumReduceTasks(reduceTasks);

    FileInputFormat.setInputPaths(job, new Path(inputPath));
    FileOutputFormat.setOutputPath(job, new Path(outputPath));

    job.setMapOutputKeyClass(BinSedesTuple.class);
    job.setMapOutputValueClass(FloatWritable.class);
    job.setOutputKeyClass(BinSedesTuple.class);
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
    ToolRunner.run(new BigramRelativeFrequencyTuple(), args);
  }
}
