# overlay cassandra configuration

cassandra.yaml is copied from https://raw.githubusercontent.com/apache/cassandra/cassandra-3.11.15/conf/cassandra.yaml

most of the settings are modified using the dockker-entrypoint https://github.com/docker-library/cassandra/blob/master/3.11/docker-entrypoint.sh

however we need to enable user defined functions 

https://docs.datastax.com/en/dse/5.1/cql/cql/cql_using/useCreateUDF.html

enable_user_defined_functions: true
