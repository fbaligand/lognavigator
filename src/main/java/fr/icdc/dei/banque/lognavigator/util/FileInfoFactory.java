package fr.icdc.dei.banque.lognavigator.util;

import fr.icdc.dei.banque.lognavigator.bean.FileInfo;

/**
 * Factory for creating FileInfo beans 
 * 
 * @author fbaligand
 */
public class FileInfoFactory {
	
	/**
	 * Create and return a parent folder link <code>FileInfo</code> bean
	 * @param currentPath current path used to compute parent path 
	 * @return FileInfo bean containing link to parent folder
	 */
	public static FileInfo createParentFolderLink(String currentPath) {
		FileInfo parentFolderLink = new FileInfo();
		parentFolderLink.setDirectory(true);
		parentFolderLink.setFileName("..");
		parentFolderLink.setFileSize(0L);
		int parentFolderEndIndex = currentPath.lastIndexOf('/');
		if (parentFolderEndIndex != -1) {
			parentFolderLink.setRelativePath(currentPath.substring(0, parentFolderEndIndex));
		}
		return parentFolderLink;
	}
	
}
