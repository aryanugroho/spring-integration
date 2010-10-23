/*
 * Copyright 2010 the original author or authors
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package org.springframework.integration.twitter;

import org.springframework.integration.Message;
import org.springframework.integration.twitter.model.GeoLocation;
import org.springframework.integration.twitter.model.Twitter4jGeoLocationImpl;

import org.springframework.util.StringUtils;

import twitter4j.StatusUpdate;


/**
 * Convenience class that can take a seemingly disparate jumble of headers and a payload and do a best-faith attempt at vending a {@link twitter4j.StatusUpdate} instance
 *
 * @author Josh Long
 * @see twitter4j.StatusUpdate
 * @see org.springframework.integration.twitter.TwitterHeaders
 * @since 2.0
 */
public class StatusUpdateSupport {

	/**
	 * convenient, interf-ace-oriented way of obtaining a reference to a {@link org.springframework.integration.twitter.model.Twitter4jGeoLocationImpl}
	 * @param lat the latitude
	 * @param lon the longitude
	 * @return a {@link org.springframework.integration.twitter.model.GeoLocation} instance
	 */
	public GeoLocation fromLatitudeLongitudePair ( double lat, double lon){
		return new Twitter4jGeoLocationImpl(lat, lon);
	}
    /**
     * {@link StatusUpdate} instances are used to drive status updates.
     *
     * @param message the inbound messages
     * @return a {@link StatusUpdate}  that's been materialized from the inbound message
     * @throws Throwable thrown if something goes wrong
     */
    public StatusUpdate fromMessage(Message<?> message)
        throws Throwable {
        Object payload = message.getPayload();
        StatusUpdate statusUpdate = null;

        if (payload instanceof String) {
            statusUpdate = new StatusUpdate((String) payload);

            if (message.getHeaders()
                           .containsKey(TwitterHeaders.TWITTER_IN_REPLY_TO_STATUS_ID)) {
                Long replyId = (Long) message.getHeaders()
                                             .get(TwitterHeaders.TWITTER_IN_REPLY_TO_STATUS_ID);

                if ((replyId != null) && (replyId > 0)) {
                    statusUpdate.inReplyToStatusId(replyId);
                }
            }

            if (message.getHeaders().containsKey(TwitterHeaders.TWITTER_PLACE_ID)) {
                String placeId = (String) message.getHeaders()
                                                 .get(TwitterHeaders.TWITTER_PLACE_ID);

                if (StringUtils.hasText(placeId)) {
                    statusUpdate.placeId(placeId);
                }
            }

            if (message.getHeaders().containsKey(TwitterHeaders.TWITTER_GEOLOCATION)) {

				GeoLocation geoLocation = (GeoLocation) message.getHeaders()
										   .get(TwitterHeaders.TWITTER_GEOLOCATION);
                twitter4j.GeoLocation gl = null;

                if (geoLocation instanceof Twitter4jGeoLocationImpl) {
                    gl = ((Twitter4jGeoLocationImpl) geoLocation).getGeoLocation();

                    if (null != gl) {
                        statusUpdate.location(gl);
                    }
                }
            }

            if (message.getHeaders()
                           .containsKey(TwitterHeaders.TWITTER_DISPLAY_COORDINATES)) {
                Boolean displayCoords = (Boolean) message.getHeaders()
                                                         .get(TwitterHeaders.TWITTER_DISPLAY_COORDINATES);

                if ((displayCoords != null) &&
                        displayCoords.equals(Boolean.TRUE)) {
                    statusUpdate.displayCoordinates(displayCoords);
                }
            }
        }

        if (payload instanceof StatusUpdate) {
            statusUpdate = (StatusUpdate) payload;
        }

        return statusUpdate;
    }
}
