/******************************************************************************* 
 * Copyright (c) 2014 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 *   Red Hat, Inc.
 ******************************************************************************/
package com.openshift.internal.client;

import java.net.URL;
import java.util.Map;

import com.openshift.client.IGearGroup;
import com.openshift.client.Messages;
import com.openshift.client.OpenShiftException;
import com.openshift.client.cartridge.IDeployedStandaloneCartridge;
import com.openshift.client.cartridge.IStandaloneCartridge;
import com.openshift.internal.client.httpclient.request.Parameter;
import com.openshift.internal.client.response.CartridgeResourceDTO;
import com.openshift.internal.client.response.Link;
import com.openshift.internal.client.utils.Assert;
import com.openshift.internal.client.utils.IOpenShiftJsonConstants;

/**
 * A deployed standalone cartridge.
 * 
 * @author Jeff Cantrill
 * @author Andre Dietisheim
 */
public class StandaloneCartridgeResource extends AbstractOpenShiftResource implements IStandaloneCartridge,
		IDeployedStandaloneCartridge {

	private static final String LINK_UPDATE_CARTRIDGE = "UPDATE";

	private ApplicationResource application;
	private String name;
	private String displayName;
	private String description;
	private URL url;
	private CartridgeType cartridgeType;
	private boolean obsolete;

	protected StandaloneCartridgeResource(final CartridgeResourceDTO dto, final ApplicationResource application) {
		this(dto.getName(), dto.getDisplayName(), dto.getDescription(), dto.getUrl(), dto.getType(), dto.getObsolete(),
				application, application.getService(), dto.getLinks(), dto.getMessages());
	}

	protected StandaloneCartridgeResource(String name, String displayName, String description, URL url,
			CartridgeType cartridgeType, boolean obsolete, final ApplicationResource application,
			final IRestService service, Map<String, Link> links, Messages messages) {
		super(service, links, messages);
		Assert.isTrue(cartridgeType != null && cartridgeType.equals(CartridgeType.STANDALONE));
		this.name = name;
		this.displayName = displayName;
		this.description = description;
		this.url = url;
		this.cartridgeType = cartridgeType;
		this.obsolete = obsolete;
		this.application = application;
	}

	/**
	 * set the additional gear storage for this cartridge to the given value
	 * 
	 * @param size
	 *            The total size of storage in gigabytes for the gear
	 */
	@Override
	public void setAdditionalGearStorage(int size) {
		new UpdateCartridgeRequest().execute(
				new Parameter(IOpenShiftJsonConstants.PROPERTY_ADDITIONAL_GEAR_STORAGE, String.valueOf(size)));
	}

	@Override
	public int getAdditionalGearStorage() {
		IGearGroup gearGroup = getGearGroup();
		if (gearGroup == null) {
			return IGearGroup.NO_ADDITIONAL_GEAR_STORAGE;
		}
		return gearGroup.getAdditionalStorage();

	}

	@Override
	public IGearGroup getGearGroup() {
		return application.getGearGroup(this);
	}

	/**
	 * Refreshes the content of this embedded cartridge. Causes all embedded
	 * cartridges of the same application to get updated.
	 * 
	 * @see #update(CartridgeResourceDTO)
	 * @see ApplicationResource#refreshEmbeddedCartridges()
	 */
	@Override
	public void refresh() throws OpenShiftException {
		application.refreshEmbeddedCartridges();
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public boolean isDownloadable() {
		return url != null;
	}

	@Override
	public URL getUrl() {
		return url;
	}

	@Override
	public CartridgeType getType() {
		return cartridgeType;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(IDeployedStandaloneCartridge.class.isAssignableFrom(obj.getClass()))) {
			return false;
		}

		IDeployedStandaloneCartridge otherCartridge = (IDeployedStandaloneCartridge) obj;
		// shortcut: downloadable cartridges get their name only when
		// they're deployed thus should equal on url only
		if (getName() == null) {
			if (otherCartridge.getName() != null) {
				return false;
			}
		} else if (!getName().equals(otherCartridge.getName())) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	private class UpdateCartridgeRequest extends ServiceRequest {
		private UpdateCartridgeRequest() {
			super(LINK_UPDATE_CARTRIDGE);
		}
	}

	@Override
	public boolean isObsolete() {
		return obsolete;
	}

	@Override
	public String toString() {
		return "StandaloneCartridgeResource ["
				+ "application=" + application
				+ ", name=" + name
				+ ", displayName=" + displayName
				+ ", description=" + description
				+ ", url=" + url
				+ ", cartridgeType=" + cartridgeType
				+ ", obsolete=" + obsolete
				+ "]";
	}
}
