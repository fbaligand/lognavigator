package org.lognavigator.util;


import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import net.schmizz.sshj.sftp.FileAttributes;
import net.schmizz.sshj.sftp.PathComponents;
import net.schmizz.sshj.sftp.RemoteResourceInfo;

import org.junit.Assert;
import org.junit.Test;

public class LastUpdatedRemoteResourceFilterTest {

	@Test
	public void testAcceptWithLimitNotReached() throws Exception {
		
		// given
		RemoteResourceInfo resource1 = createRemoteResourceInfo(2L);
		RemoteResourceInfo resource2 = createRemoteResourceInfo(1L);
		int resourceMaxCount = 2;
		
		// when
		LastUpdatedRemoteResourceFilter resourceFilter = new LastUpdatedRemoteResourceFilter(resourceMaxCount);
		resourceFilter.accept(resource1);
		resourceFilter.accept(resource2);
		Collection<RemoteResourceInfo> remoteResourceInfos = resourceFilter.getRemoteResourceInfos();
		
		// then
		Assert.assertEquals(2, remoteResourceInfos.size());
		Iterator<RemoteResourceInfo> resourceIterator = remoteResourceInfos.iterator();
		Assert.assertTrue(resource2 == resourceIterator.next());
		Assert.assertTrue(resource1 == resourceIterator.next());
	}

	@Test
	public void testAcceptWithLimitReached() throws Exception {
		
		// given
		RemoteResourceInfo resource1 = createRemoteResourceInfo(1L);
		RemoteResourceInfo resource2 = createRemoteResourceInfo(2L);
		RemoteResourceInfo resource3 = createRemoteResourceInfo(3L);
		int resourceMaxCount = 2;
		
		// when
		LastUpdatedRemoteResourceFilter resourceFilter = new LastUpdatedRemoteResourceFilter(resourceMaxCount);
		resourceFilter.accept(resource1);
		resourceFilter.accept(resource2);
		resourceFilter.accept(resource3);
		Collection<RemoteResourceInfo> remoteResourceInfos = resourceFilter.getRemoteResourceInfos();
		
		// then
		Assert.assertEquals(2, remoteResourceInfos.size());
		Iterator<RemoteResourceInfo> resourceIterator = remoteResourceInfos.iterator();
		Assert.assertTrue(resource2 == resourceIterator.next());
		Assert.assertTrue(resource3 == resourceIterator.next());
	}
	
	@Test
	public void testCompare() throws Exception {
		
		// given
		RemoteResourceInfo resource1 = createRemoteResourceInfo(2L);
		RemoteResourceInfo resource2 = createRemoteResourceInfo(1L);
		int resourceMaxCount = 2;
		
		// when
		LastUpdatedRemoteResourceFilter resourceFilter = new LastUpdatedRemoteResourceFilter(resourceMaxCount);
		int result = resourceFilter.compare(resource1, resource2);
		
		// then
		final int greaterThanReturn = 1;
		Assert.assertEquals(greaterThanReturn, result);
	}

	private RemoteResourceInfo createRemoteResourceInfo(long mtime) {
		PathComponents comps = new PathComponents("parent", "name" + mtime, "/");
		FileAttributes attrs = new FileAttributes(0, 0, 0, 0, null, 0L, mtime, new HashMap<String, String>());
		return new RemoteResourceInfo(comps, attrs);
	}

}
