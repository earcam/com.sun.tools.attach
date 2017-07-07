package io.earcam.wrapped.com.sun.tools.attach;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import com.sun.tools.attach.AttachNotSupportedException;       //NOSONAR false positive - @jdk.Exported
import com.sun.tools.attach.AttachOperationFailedException;    //NOSONAR false positive - @jdk.Exported
import com.sun.tools.attach.VirtualMachine;                    //NOSONAR false positive - @jdk.Exported
import com.sun.tools.attach.VirtualMachineDescriptor;          //NOSONAR false positive - @jdk.Exported
import com.sun.tools.attach.spi.AttachProvider;                //NOSONAR false positive - @jdk.Exported

import sun.tools.attach.BsdAttachProvider;
import sun.tools.attach.LinuxAttachProvider;
import sun.tools.attach.SolarisAttachProvider;
import sun.tools.attach.WindowsAttachProvider;

public class Provider extends AttachProvider {
	
	private static final String SYSTEM_PROPERTY_OS_NAME = "os.name";
	private Optional<Supplier<AttachProvider>> PROVIDER_BY_OS = findProvider();
	
	private volatile AttachProvider provider;


	private static Optional<Supplier<AttachProvider>> findProvider()
	{
        Map<String, Supplier<AttachProvider>> map = new HashMap<>();
        map.put("windows", WindowsAttachProvider::new);
        map.put("mac os x", BsdAttachProvider::new);
        map.put("bsd", BsdAttachProvider::new);
        map.put("sunos", SolarisAttachProvider::new);
        map.put("solaris", SolarisAttachProvider::new);
        map.put("linux", LinuxAttachProvider::new);
        return map.entrySet().stream()
        	.filter(e -> os().startsWith(e.getKey()))
        	.map(Map.Entry::getValue)
        	.findFirst();
	}


	private static String os()
	{
		return System.getProperty(SYSTEM_PROPERTY_OS_NAME, "").toLowerCase();
	}
	
	
	public AttachProvider provider()
	{
		if(provider == null) {
			synchronized(Provider.class) {
				if(provider == null) {
					provider = PROVIDER_BY_OS.orElseThrow(Provider::providerNotFound).get();
				}
			}
		}
		return provider;
	}


	private static UncheckedIOException providerNotFound()
	{
		return new UncheckedIOException(new AttachOperationFailedException("Failed to locate AttachProvider for this OS: " + os()));
	}


	@Override
	public VirtualMachine attachVirtualMachine(String arg0) throws AttachNotSupportedException, IOException
	{
		return provider().attachVirtualMachine(arg0);
	}


	@Override
	public List<VirtualMachineDescriptor> listVirtualMachines()
	{
		return provider().listVirtualMachines();
	}


	@Override
	public String name()
	{
		return provider().name();
	}


	@Override
	public String type()
	{
		return provider().type();
	}
}