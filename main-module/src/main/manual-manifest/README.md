# problems with manifests
when running against OpenNMS 27, the manifest is being generated with incorrect versions. They should all be [27.1,28)

```
org.opennms.netmgt.dao.api;version="[26.1,27)",org.opennms.ne
 tmgt.events.api;version="[27.1,28)"
 ```