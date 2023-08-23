/*
 *    Copyright 2023 Craig Gallen, OpenNMS Group
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.opennms.integration.example.paxexamtest.manual;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import static org.opennms.paxexam.container.OpenNMSRBCRemoteTargetOptions.location;
import static org.opennms.paxexam.container.OpenNMSRBCRemoteTargetOptions.waitForRBCFor;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.ops4j.pax.exam.spi.reactors.PerSuite;
import org.ops4j.pax.exam.util.PathUtils;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
