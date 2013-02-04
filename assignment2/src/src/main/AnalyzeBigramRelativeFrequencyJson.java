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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.common.collect.Lists;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.FloatWritable;

import edu.umd.cloud9.io.SequenceFileUtils;
import edu.umd.cloud9.io.pair.PairOfWritables;

public class AnalyzeBigramRelativeFrequencyJson {
  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      System.out.println("usage: [input-path]");
      System.exit(-1);
    }

    System.out.println("input path: " + args[0]);

    List<PairOfWritables<BigramRelativeFrequencyJson.MyTuple, FloatWritable>> pairs = 
        SequenceFileUtils.readDirectory(new Path(args[0]));

    List<PairOfWritables<BigramRelativeFrequencyJson.MyTuple, FloatWritable>> list1 = Lists.newArrayList();
    List<PairOfWritables<BigramRelativeFrequencyJson.MyTuple, FloatWritable>> list2 = Lists.newArrayList();

    for (PairOfWritables<BigramRelativeFrequencyJson.MyTuple, FloatWritable> p : pairs) {
      BigramRelativeFrequencyJson.MyTuple bigram = p.getLeftElement();

      if (bigram.getJsonObject().get("Left").getAsString().equals("light")) {
        list1.add(p);
      }

      if (bigram.getJsonObject().get("Left").getAsString().equals("contain")) {
        list2.add(p);
      }
    }

    Collections.sort(list1,
        new Comparator<PairOfWritables<BigramRelativeFrequencyJson.MyTuple, FloatWritable>>() {
          public int compare(
              PairOfWritables<BigramRelativeFrequencyJson.MyTuple, FloatWritable> e1,
              PairOfWritables<BigramRelativeFrequencyJson.MyTuple, FloatWritable> e2) {
            if (e1.getRightElement().compareTo(e2.getRightElement()) == 0) {
              return e1.getLeftElement().compareTo(e2.getLeftElement());
            }

            return e2.getRightElement().compareTo(e1.getRightElement());
          }
        });

    int i = 0;
    for (PairOfWritables<BigramRelativeFrequencyJson.MyTuple, FloatWritable> p : list1) {
      BigramRelativeFrequencyJson.MyTuple bigram = p.getLeftElement();
      System.out.println(bigram + "\t" + p.getRightElement());
      i++;

      if (i > 10) {
        break;
      }
    }

    Collections.sort(list2,
        new Comparator<PairOfWritables<BigramRelativeFrequencyJson.MyTuple, FloatWritable>>() {
          public int compare(
              PairOfWritables<BigramRelativeFrequencyJson.MyTuple, FloatWritable> e1,
              PairOfWritables<BigramRelativeFrequencyJson.MyTuple, FloatWritable> e2) {
            if (e1.getRightElement().compareTo(e2.getRightElement()) == 0) {
              return e1.getLeftElement().compareTo(e2.getLeftElement());
            }

            return e2.getRightElement().compareTo(e1.getRightElement());
          }
        });

    i = 0;
    for (PairOfWritables<BigramRelativeFrequencyJson.MyTuple, FloatWritable> p : list2) {
      BigramRelativeFrequencyJson.MyTuple bigram = p.getLeftElement();
      System.out.println(bigram + "\t" + p.getRightElement());
      i++;

      if (i > 10) {
        break;
      }
    }
  }
}
