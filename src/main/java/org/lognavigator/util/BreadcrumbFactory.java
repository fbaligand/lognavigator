package org.lognavigator.util;

import java.util.ArrayList;
import java.util.List;

import static org.lognavigator.util.Constants.*;

import org.lognavigator.bean.Breadcrumb;

/**
 * Static methods usefull to create a list of Breadcrumbs
 */
public class BreadcrumbFactory {
	
	/**
	 * Create a list of Breadcrumb with one element : link to logs list root
	 * @param logAccessConfigId breadcrumb label
	 * @return list of Breadcrumb with one element 
	 */
	public static List<Breadcrumb> createBreadCrumbs(String logAccessConfigId) {
		List<Breadcrumb> breadcrumbs = new ArrayList<Breadcrumb>();
		breadcrumbs.add(new Breadcrumb(logAccessConfigId, LOGS_LIST_URL));
		return breadcrumbs;
	}

	/**
	 * Split 'path' into path-elements and add each one to breadcrumbs list
	 * @param breadcrumbs
	 * @param path
	 * @param lastElementIsLink
	 */
	public static void addSubPath(List<Breadcrumb> breadcrumbs, String path, boolean lastElementIsLink) {
		path = path.replace("//", "/");
		String[] pathElements = path.split("/");
		int currentSlashIndex = 0;
		
		for (int i=0; i<pathElements.length; i++) {
			String label = pathElements[i];
			if (label.isEmpty()) {
				continue;
			}
			if (!label.equals("*") && (lastElementIsLink || i < pathElements.length - 1)) {
				currentSlashIndex = path.indexOf('/', currentSlashIndex + 1);
				String linkParam = (currentSlashIndex != -1) ? path.substring(0, currentSlashIndex) : path;
				String link = FOLDER_VIEW_URL_PREFIX + UriUtil.encode(linkParam);
				breadcrumbs.add(new Breadcrumb(label, link));
			}
			else {
				breadcrumbs.add(new Breadcrumb(label));
			}
		}
		
	}
	
}
