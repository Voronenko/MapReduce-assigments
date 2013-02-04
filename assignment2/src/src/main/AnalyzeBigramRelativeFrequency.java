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

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.FloatWritable;

import com.google.common.collect.Lists;

import edu.umd.cloud9.io.SequenceFileUtils;
import edu.umd.cloud9.io.pair.PairOfStrings;
import edu.umd.cloud9.io.pair.PairOfWritables;

public class AnalyzeBigramRelativeFrequency {
  public static void main(String[] args) {
    if (args.length != 1) {
      System.out.println("usage: [input-path]");
      System.exit(-1);
    }

    System.out.println("input path: " + args[0]);

    List<PairOfWritables<PairOfStrings, FloatWritable>> pairs =
        SequenceFileUtils.readDirectory(new Path(args[0]));

    List<PairOfWritables<PairOfStrings, FloatWritable>> list1 = Lists.newArrayList();
    List<PairOfWritables<PairOfStrings, FloatWritable>> list2 = Lists.newArrayList();

    for (PairOfWritables<PairOfStrings, FloatWritable> p : pairs) {
      PairOfStrings bigram = p.getLeftElement();

      if (bigram.getLeftElement().equals("light")) {
        list1.add(p);
      }
      if (bigram.getLeftElement().equals("contain")) {
        list2.add(p);
      }
    }

    Collections.sort(list1, new Comparator<PairOfWritables<PairOfStrings, FloatWritable>>() {
      public int compare(PairOfWritables<PairOfStrings, FloatWritable> e1,
          PairOfWritables<PairOfStrings, FloatWritable> e2) {
        if (e1.getRightElement().compareTo(e2.getRightElement()) == 0) {
          return e1.getLeftElement().compareTo(e2.getLeftElement());
        }

        return e2.getRightElement().compareTo(e1.getRightElement());
      }
    });

    int i = 0;
    for (PairOfWritables<PairOfStrings, FloatWritable> p : list1) {
      PairOfStrings bigram = p.getLeftElement();
      System.out.println(bigram + "\t" + p.getRightElement());
      i++;

      if (i > 10) {
        break;
      }
    }

    Collections.sort(list2, new Comparator<PairOfWritables<PairOfStrings, FloatWritable>>() {
      public int compare(PairOfWritables<PairOfStrings, FloatWritable> e1,
          PairOfWritables<PairOfStrings, FloatWritable> e2) {
        if (e1.getRightElement().compareTo(e2.getRightElement()) == 0) {
          return e1.getLeftElement().compareTo(e2.getLeftElement());
        }

        return e2.getRightElement().compareTo(e1.getRightElement());
      }
    });

    i = 0;
    for (PairOfWritables<PairOfStrings, FloatWritable> p : list2) {
      PairOfStrings bigram = p.getLeftElement();
      System.out.println(bigram + "\t" + p.getRightElement());
      i++;

      if (i > 10) {
        break;
      }
    }
  }
}
