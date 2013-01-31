MapReduce-assigments
====================

Self studying on Map-Reduce course http://lintool.github.com/MapReduce-course-2013s/assignments.html


Assignment 0: Prelude due 6:00pm January 24
Complete the word count tutorial in Cloud9, which is a Hadoop toolkit we're going to use throughout the course. The tutorial will take you through setting up Hadoop on your local machine and running Hadoop on the virtual machine. It'll also begin familiarizing you with GitHub.

Note: This assignment is not explicitly graded, except as part of Assignment 1.


Assignment 1: Warmup due 6:00pm January 31
Make sure you've completed the word count tutorial in Cloud9.

Sign up for a GitHub account. It is very important that you do so as soon as possible, because GitHub is the mechanism by which you will submit assignments. Once you've signed up for an account, go to this page to request an educational account.

Next, create a private repo called MapReduce-assignments. Here is how you create a repo on GitHub. For "Who has access to this repository?", make sure you click "Only the people I specify". If you've successfully gotten an educational account (per above), you should be able to create private repos for free. Take some time to learn about git if you've never used it before. There are plenty of good tutorials online: do a simple web search and find one you like. If you've used svn before, many of the concepts will be familiar, except that git is far more powerful.

After you've learned about git, set aside the repo for now; you'll come back to it later.

In the single node virtual cluster in the word count tutorial, you should have run the word count demo with five reducers:

etc/hadoop-cluster.sh edu.umd.cloud9.example.simple.DemoWordCount \
  -input bible+shakes.nopunc.gz -output wc -numReducers 5
Answer the following questions:

Question 1. What is the first term in part-r-00000 and how many times does it appear?

Question 2. What is the third to last term in part-r-00004 and how many times does it appear?

Question 3. How many unique terms are there? (Hint: read the counter values)

Let's do a little bit of cleanup of the words. Modify the word count demo so that only words consisting entirely of letters are counted. To be more specific, the word must match the following Java regular expression:

word.matches("[A-Za-z]+")
Now run word count again, also with five reducers. Answer the following questions:

Question 4. What is the first term in part-r-00000 and how many times does it appear?

Question 5. What is the third to last term in part-r-00004 and how many times does it appear?

Question 6. How many unique terms are there?
