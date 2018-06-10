/*-
 * #%L
 * com.sun.tools.attach
 * %%
 * Copyright (C) 2017 earcam
 * %%
 * SPDX-License-Identifier: (BSD-3-Clause OR EPL-1.0 OR Apache-2.0 OR MIT)
 *
 * You <b>must</b> choose to accept, in full - any individual or combination of
 * the following licenses:
 * <ul>
 * 	<li><a href="https://opensource.org/licenses/BSD-3-Clause">BSD-3-Clause</a></li>
 * 	<li><a href="https://www.eclipse.org/legal/epl-v10.html">EPL-1.0</a></li>
 * 	<li><a href="https://www.apache.org/licenses/LICENSE-2.0">Apache-2.0</a></li>
 * 	<li><a href="https://opensource.org/licenses/MIT">MIT</a></li>
 * </ul>
 * #L%
 */
package io.earcam.wrapped.com.sun.tools.attach;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.UUID;

import javax.annotation.concurrent.NotThreadSafe;

import org.junit.Assume;
import org.junit.Test;

import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.spi.AttachProvider;

@NotThreadSafe
public class ProviderTest {


	@Test
	public void listVMs()
	{
		assertThat(new Provider().listVirtualMachines(), is(not(empty())));
	}


	@Test
	public void attachToCurrentVm() throws AttachNotSupportedException, IOException
	{
		Assume.assumeThat(System.getProperty("jdk.attach.allowAttachSelf"), is(equalTo("true")));
		
		String key = ProviderTest.class.getCanonicalName() + UUID.randomUUID();
		String value = UUID.randomUUID().toString();
		System.setProperty(key, value);

		VirtualMachine vm = new Provider().attachVirtualMachine(currentPid());

		assertThat(vm.getSystemProperties().getProperty(key), is(equalTo(value)));
		vm.detach();
	}


	// Waiting on http://openjdk.java.net/jeps/102 or consider http://stackoverflow.com/a/7303433/573057
	private static String currentPid()
	{
		String name = ManagementFactory.getRuntimeMXBean().getName();
		return name.substring(0, name.indexOf('@'));
	}


	@Test
	public void spi()
	{
		List<AttachProvider> providers = AttachProvider.providers();

		assertThat(providers, hasItem(instanceOf(Provider.class)));
	}


	@Test
	public void name()
	{
		Provider provider = new Provider();

		assertThat(provider.name(), is(not(emptyString())));
	}


	@Test
	public void type()
	{
		Provider provider = new Provider();

		assertThat(provider.type(), is(not(emptyString())));
	}


	@Test
	public void wrappedProviderIsCached()
	{
		Provider provider = new Provider();

		assertThat(provider.provider(), is(sameInstance(provider.provider())));
	}
}
