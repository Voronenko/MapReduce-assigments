Notes on environment installation

[https://ccp.cloudera.com/display/CDH4DOC/Installing+CDH4+on+a+Single+Linux+Node+in+Pseudo-distributed+Mode](https://ccp.cloudera.com/display/CDH4DOC/Installing+CDH4+on+a+Single+Linux+Node+in+Pseudo-distributed+Mode)


---------


Items to consider if played with previous version of map reduce (so called MRV1)

1. stop hadoop-0.20-mapreduce-*
stop hadoop-0.20-hdfs-*

2. Remove

sudo apt-get remove hadoop-0.20-conf-pseudo hadoop-0.20-map-reduce-*


Then install deb +

http://archive.cloudera.com/cdh4/one-click-install/precise/amd64/cdh4-repository_1.0_all.deb


key

curl -s http://archive.cloudera.com/cdh4/ubuntu/precise/amd64/cdh/archive.key | sudo apt-key add -

Then proceed with instructions on link above


---------

Useful urls:   http://localhost:50070/  - Name node frontend


-------------------

DFS initialization:
[https://gist.github.com/4699525](https://gist.github.com/4699525)


Also worth checking this github repo for inspiration:

[https://github.com/alrokayan/hadoop-openstack-centos](https://github.com/alrokayan/hadoop-openstack-centos)