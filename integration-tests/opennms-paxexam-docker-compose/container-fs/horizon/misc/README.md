

To get into the horizon container and access the karaf shell using ssh use

```
docker-compose exec horizon bash

ssh admin@horizon -p 8101  -o StrictHostKeyChecking=no
(password: admin)

```

to collect

```

 opennms:collect -n 1 org.opennms.protocols.xml.collector.XmlCollector 127.0.0.1 collection=xmlfile1
 
 ```