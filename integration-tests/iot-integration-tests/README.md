# opennms-paxexam-test-example

This is an example test project which will work with either a simple karaf installation or OpenNMS. 
It does not uses any OpenNMS specific API's.

## Setting up the test environment

To run the tests, you first need to build and install the core classes. 
(In the future , these will probably be committed to the public maven repo but for now you need to build and install them in your local .m2 repository)

```
cd opennms-paxexam-parent
mvn clean install
```
Next, run maven to install the dependencies in which ever project you want to test and then start the container with docker-compose.

To just run a simple karaf use:

```
cd minimal-karaf
mvn clean install
docker-compose up -d
```

To run a full OpenNMS horizon use:

```
cd minimal-horizon
mvn clean install
docker-compose up -d
```
Note that horizon will take a while to start - particularly if this is the first time you have run the container and the database needs built. 


## Running the tests

Once the test environment has started, you can run the test example.
You can run this as a surefire test within the maven build

```
cd opennms-paxexam-test-example
mvn clean install
```

Alternatively you can import the project into an IDE such as Eclipse and run the tests individually.

## Simple test

The simplest example test is [MyTest.java](../opennms-paxexam-test-example/src/test/java/org/opennms/integration/example/paxexamtest/simple/MyTest.java)

This test simply checks that the karaf BundleContext is injected into the test and then prints out four messages to System.out and LOG.error, LOG.warn and LOG.debug respectively. 

Note that the test will be wrapped in a RestProbe and injected into the Karaf container using RMI. 
Any generated log messages will appear in the karaf.log and not the log in the maven build or the IDE.

```
@RunWith(PaxExam.class)
@ExamFactory(org.opennms.paxexam.container.OpenNMSPluginTestContainerFactory.class)

@ExamReactorStrategy(PerClass.class)
//@ExamReactorStrategy(PerSuite.class)
//@ExamReactorStrategy(PerMethod.class)
public class MyTest {
    private static Logger LOG = LoggerFactory.getLogger(MyTest.class);

    @Inject
    private BundleContext bc;
    
    @Configuration
    public Option[] config() {
        return new Option[] {
                location("localhost", 55555),
                waitForRBCFor(10000)
       };
    }

    @Test
    public void printOutData() {
        System.out.println("****  bundle printing test data");
    }
    
    @Test
    public void shouldHaveBundleContext() {
        LOG.error("**** make sure we can log error");
        LOG.info("**** make sure we can log info ");
        LOG.debug("**** make sure we can log debug");
        assertThat(bc, is(notNullValue()));
    }

}
```
### Test Configuration

The pax-exam system is initiated within a junit test through the annotation 

```
@RunWith(PaxExam.class)
```

In a standard configuration, paxexam looks for a test container factory on the class path however in the opennms-paxexam system, the test container factory must be explicitly injected using the annotation 

```
@ExamFactory(org.opennms.paxexam.container.OpenNMSPluginTestContainerFactory.class), 
```

The actual configuration for the test is defined using

```
    @Configuration
    public Option[] config() {
        return new Option[] {
                location("localhost", 55555),
                waitForRBCFor(10000)
       };
    }

```

The location defines the address of the test runner as localhost and the RMI port for the test in this class as 55555.

Note that if you are running the test on the host against a container within docker-compose, the localhost address and port are translated by docker-compose to the address of the container. 
Each test can only be injected into one container and thus will have a unique RMI port. 
However it should be possible to have more than one container exposing their RMI on a different ports and then have separate tests which test each of the container locations.

The waitForRBCFor(10000) configuration tells the system to wait for up to 10 seconds for the remote bundle context to respond. 

No other container provisioning annotations are supported as the configuration of the container should be done through any files injected in the docker-compose project. 

## Karaf command test

A more complex test example is [CommandTest.java](../opennms-paxexam-test-example/src/test/java/org/opennms/integration/example/paxexamtest/complex/CommandTest.java) which extends a base test class  [TestBase.java](../opennms-paxexam-test-example/src/test/java/org/opennms/integration/example/paxexamtest/complex/TestBase.java)

( The idea for the base test was provided by Achim Nierbeck in his [karaf-cassandra project TestBase.java]( https://github.com/ANierbeck/Karaf-Cassandra/blob/master/Karaf-Cassandra-ITest/src/test/java/de/nierbeck/cassandra/itest/TestBase.java) If you look at the tests in that project you will get some good ideas of what can be done here.)

[CommandTest.java](../opennms-paxexam-test-example/src/test/java/org/opennms/integration/example/paxexamtest/complex/CommandTest.java) illustrates how services running in karf can be injected into the paxexam test class and how karaf commands can be executed within the test.
In this example,

```
executeCommand("bundle:list")
```

lists all of the bundles running in the container. 
The string output from the command is logged. (Note this is why CRLF filtering is turned off in the log configuration)

However, any karaf command which could be run from the terminal can be run as part of the test. 
This opens the possibility for running a command to start and stop a feature injected using the .m2 repository before and after the tests are run.





