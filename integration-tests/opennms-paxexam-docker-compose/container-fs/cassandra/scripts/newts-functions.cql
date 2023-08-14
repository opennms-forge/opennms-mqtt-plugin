-- newts-functions.cql
-- cql functions to allow reading of newts data using
-- select  collected_at, resource, metric_name, newts.type(value), newts.valueNumber(value) from newts.samples;

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