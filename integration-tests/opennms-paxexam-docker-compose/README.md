# useful notes

```
ssh admin@localhost -p 8101  -o StrictHostKeyChecking=no
(password admin)

kar:uninstall MqttClient.kar-package
kar:list

kar:install mvn:org.opennms.plugins/MqttClient.kar-package/0.0.8-SNAPSHOT/kar


opennms:list-collectors

log:set DEBUG
log:tail

```

 http://restsimulator:8080/nokia/data
 
 cassandra-01
 
 ```
 cqlsh

##Verify keyspace initialization:

use newts;
describe table terms;
describe table samples;


 select * from samples where resource = 'snmp:fs:nokia:equipment.SystemStatsHolder:mqtt:sniffy' ALLOW FILTERING;
 ```
 
 
 
 
 in command shell opennms
 
 ```
 opennms:show-measurement-resources 
 
 
 opennms:show-measurements -a systemCpuUsage --interval 6000 "node[nokia:equipment.SystemStatsHolder].interfaceSnmp[mqtt]"
 
  opennms:show-measurements -a systemCpuUsage --start 1692004860000    --raw-timestamps --interval 6000 "node[nokia:equipment.SystemStatsHolder].interfaceSnmp[mqtt]"
 
 ```
 
 ## Cassandra data extraction
to extract data directly from cassandra, install the following user defined procedures

```
CREATE OR REPLACE FUNCTION newts.type (value blob)  CALLED ON NULL INPUT  RETURNS text 
LANGUAGE java AS ' 
if (value == null) {     return null;   }
Byte b = value.get();   
if(b.intValue()==1) {
  return "COUNTER";
} else if (b.intValue()==2) {
   return "ABSOLUTE";
} else if (b.intValue()==3) {
  return "DERIVE";
} else if (b.intValue()==4) {
  return "GAUGE";
} 
return "unknown type:"+b.intValue(); ' ;

CREATE OR REPLACE FUNCTION newts.valueNumber (value blob)  CALLED ON NULL INPUT  RETURNS double 
LANGUAGE java AS ' 
if (value == null) { return null; }  
Byte b = value.get();  
if(b.intValue()==1 || b.intValue()==2 || b.intValue()==3 ) {
    return Double.longBitsToDouble( value.getLong());
  } else if (b.intValue()==4) {
    return value.getDouble();
  } 
throw new IllegalArgumentException(" newts.valueNumberfunction  unknown type:"+b.intValue()); '  ;

```
This allows a cql query to access newts data in cassandra

```
select  collected_at, resource, metric_name, newts.type(value), newts.valueNumber(value) from newts.samples;

```