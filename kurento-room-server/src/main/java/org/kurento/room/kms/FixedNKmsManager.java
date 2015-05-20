/*
 * (C) Copyright 2015 Kurento (http://kurento.org/)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 */

package org.kurento.room.kms;

import java.util.List;

import org.kurento.client.KurentoClient;

public class FixedNKmsManager extends KmsManager {

	public FixedNKmsManager(List<String> kmsWsUri) {
		for (String uri : kmsWsUri)
			this.addKms(new Kms(KurentoClient.create(uri), uri));
	}

	public FixedNKmsManager(List<String> kmsWsUri, int kmsLoadLimit) {
		for (String uri : kmsWsUri) {
			Kms kms = new Kms(KurentoClient.create(uri), uri);
			kms.setLoadManager(new MaxWebRtcLoadManager(kmsLoadLimit));
			this.addKms(kms);
		}
	}

}