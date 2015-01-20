package org.lognavigator.util;

import java.util.Collection;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import net.schmizz.sshj.sftp.RemoteResourceFilter;
import net.schmizz.sshj.sftp.RemoteResourceInfo;

/**
 * RemoteResourceFilter whichs keeps only 'resourceMaxCount' last modified resources
 */
public class LastUpdatedRemoteResourceFilter implements RemoteResourceFilter, Comparator<RemoteResourceInfo> {
	
	private SortedSet<RemoteResourceInfo> remoteResourceInfos;
	private int resourceMaxCount;
	
	public LastUpdatedRemoteResourceFilter(int resourceMaxCount) {
		this.resourceMaxCount = resourceMaxCount;
		this.remoteResourceInfos = new TreeSet<RemoteResourceInfo>(this);
	}

	/**
	 * Stores the resource in remoteResourceInfos set (which is sorted by mtime).
	 * Keeps only the 'resourceMaxCount' last modified resources in remoteResourceInfos.
	 * Always return false, so that no resource is stored in client list (the real filtered result list is stored in this instance, in 'remoteResourceInfos')
	 */
	@Override
	public boolean accept(RemoteResourceInfo resource) {
		remoteResourceInfos.add(resource);
		if (remoteResourceInfos.size() > resourceMaxCount) {
			remoteResourceInfos.remove(remoteResourceInfos.first());
		}
		return false;
	}

	/**
	 * Return the 'resourceMaxCount' last modified resources filtered 
	 */
	public Collection<RemoteResourceInfo> getRemoteResourceInfos() {
		return remoteResourceInfos;
	}

	/**
	 * Comparator method which sorts by mtime ascending
	 */
	@Override
	public int compare(RemoteResourceInfo o1, RemoteResourceInfo o2) {
		if (o1 == o2) {
			return 0;
		}
		else {
			long diff = o1.getAttributes().getMtime() - o2.getAttributes().getMtime();
			return (diff >= 0) ? 1 : -1;
		}
	}
}
