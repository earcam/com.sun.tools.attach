package io.earcam.wrapped.com.sun.tools.attach;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeThat;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.UUID;

import javax.annotation.concurrent.NotThreadSafe;

import org.junit.Assume;
import org.junit.Test;

import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.AttachOperationFailedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.spi.AttachProvider;


@NotThreadSafe
public class ProviderTest {


	@Test
	public void listVMs()
	{
		assertThat(new Provider().listVirtualMachines(), is( not( empty() )));
	}


	@Test
	public void attachToCurrentVm() throws AttachNotSupportedException, IOException
	{
		String key = ProviderTest.class.getCanonicalName() + UUID.randomUUID();
		String value = UUID.randomUUID().toString();
		System.setProperty(key, value);
		
		VirtualMachine vm = new Provider().attachVirtualMachine(currentPid());
		
		assertThat(vm.getSystemProperties().getProperty(key), is( equalTo( value ) ));
		vm.detach();
	}


    //Waiting on http://openjdk.java.net/jeps/102 or consider http://stackoverflow.com/a/7303433/573057
	private static String currentPid()
	{
		String name = ManagementFactory.getRuntimeMXBean().getName();
        return name.substring(0, name.indexOf('@'));
	}
	
	
	@Test
	public void spi()
	{
		List<AttachProvider> providers = AttachProvider.providers();
		
		assertThat(providers, hasItem( instanceOf( Provider.class )));
	}
	
	
	@Test
	public void name()
	{
		Provider provider = new Provider();
		
		
		assertThat(provider.name(), is( not( emptyString() )));
	}
	
	
	@Test
	public void type()
	{
		Provider provider = new Provider();
		
		
		assertThat(provider.type(), is( not( emptyString() )));
	}


	@Test
	public void providerNotFound()
	{
		String os = System.getProperty("os.name");
		try {
			System.setProperty("os.name", "chicken");
			Provider provider = new Provider();
			System.out.println(provider.type());
			fail();
		} catch(UncheckedIOException e) {
			assertThat(e.getCause(), is( instanceOf( AttachOperationFailedException.class )));
		} finally {
			System.setProperty("os.name", os);
		}
	}
	
	
	@Test
	public void wrappedProviderIsCached()
	{
		Provider provider = new Provider();
		
		
		assertThat(provider.provider(), is( sameInstance( provider.provider() )));
	}
}
