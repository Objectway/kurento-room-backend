package org.kurento.room.interfaces;

import org.kurento.client.*;
import org.kurento.room.api.KurentoClientProvider;
import org.kurento.room.api.KurentoClientSessionInfo;
import org.kurento.room.api.MutedMediaType;
import org.kurento.room.api.pojo.UserParticipant;
import org.kurento.room.exception.RoomException;

import javax.annotation.PreDestroy;
import java.util.Set;

/**
 * The Kurento room manager represents an SDK for any developer that wants to implement the Room
 * server-side application. They can build their application on top of the manager's Java API and
 * implement their desired business logic without having to consider room or media-specific details.
 * <p/>
 * The application is in control of notifying any remote parties with the outcome of executing the
 * requested actions.
 */
public interface IRoomManager {
    // void init(RoomHandler roomHandler, KurentoClientProvider kcProvider);

    /**
     * Represents a client's request to join a room. The room must exist in order to perform the
     * join.<br/>
     * <strong>Dev advice:</strong> Send notifications to the existing participants in the room to
     * inform about the new peer.
     *
     * @param userName       name or identifier of the user in the room. Will be used to identify
     *                       her WebRTC media
     *                       peer (from the client-side).
     * @param roomName       name or identifier of the room
     * @param dataChannels   enables data channels (if webParticipant)
     * @param webParticipant if <strong>true</strong>, the internal media endpoints will use the
     *                       trickle ICE
     *                       mechanism when establishing connections with external media peers (
     *                       {@link WebRtcEndpoint}); if <strong>false</strong>, the media endpoint
     *                       will be a
     *                       {@link RtpEndpoint}, with no ICE implementation
     * @param webParticipant
     * @param kcSessionInfo  sessionInfo bean to be used to create the room in case it doesn't
     *                       exist (if null, the
     *                       room will not be created)
     * @param participantId  identifier of the participant
     * @return set of existing peers of type {@link UserParticipant}, can be empty if first
     * @throws RoomException on error while joining (like the room is not found or is closing)
     */
    Set<UserParticipant> joinRoom(String userName, String roomName, boolean dataChannels, boolean webParticipant, KurentoClientSessionInfo kcSessionInfo, String participantId) throws RoomException;

    /**
     * Represents a client's notification that she's leaving the room. Will also close the room if
     * there're no more peers.<br/>
     * <strong>Dev advice:</strong> Send notifications to the other participants in the room to inform
     * about the one that's just left.
     *
     * @param participantId identifier of the participant
     * @return set of remaining peers of type {@link UserParticipant}, if empty this method has closed
     * the room
     * @throws RoomException on error leaving the room
     */
    Set<UserParticipant> leaveRoom(String participantId) throws RoomException;

    /**
     * Represents a client's request to start streaming her local media to anyone inside the room. The
     * media elements should have been created using the same pipeline as the publisher's. The
     * streaming media endpoint situated on the server can be connected to itself thus realizing what
     * is known as a loopback connection. The loopback is performed after applying all additional
     * media elements specified as parameters (in the same order as they appear in the params list).
     * <p>
     * <br/>
     * <strong>Dev advice:</strong> Send notifications to the existing participants in the room to
     * inform about the new stream that has been published. Answer to the peer's request by sending it
     * the SDP response (answer or updated offer) generated by the WebRTC endpoint on the server.
     *
     * @param participantId          identifier of the participant
     * @param isOffer                if true, the sdp is an offer from remote, otherwise is the
     *                               answer to the offer
     *                               generated previously by the server endpoint
     * @param sdp                    SDP String <strong>offer</strong> or <strong>answer</strong>,
     *                               that's been generated by
     *                               the client's WebRTC peer
     * @param loopbackAlternativeSrc instead of connecting the endpoint to itself, use this
     *                               {@link MediaElement} as source
     * @param loopbackConnectionType the connection type for the loopback; if null, will stream
     *                               both audio and video media
     * @param doLoopback             loopback flag
     *
     * @return the SDP response generated by the WebRTC endpoint on the server (answer to the client's
     * offer or the updated offer previously generated by the server endpoint)
     * @throws RoomException on error
     */
    String publishMedia(String participantId, final String streamId, final String streamType, boolean isOffer, String sdp, MediaElement loopbackAlternativeSrc, MediaType loopbackConnectionType, boolean doLoopback) throws RoomException;


    /**
     * Represents a client's request to initiate the media connection from the server-side (generate
     * the SDP offer and send it back to the client) and must be followed by processing the SDP answer
     * from the client in order to establish the streaming.
     *
     * @param participantId identifier of the participant
     * @return the SDP offer generated by the WebRTC endpoint on the server
     * @throws RoomException on error
     * @see #publishMedia(String, String, boolean)
     */
    String generatePublishOffer(String participantId, final String streamId) throws RoomException;

    /**
     * Represents a client's request to stop publishing her media stream. All media elements on the
     * server-side connected to this peer will be disconnected and released. The peer is left ready
     * for publishing her media in the future.<br/>
     * <strong>Dev advice:</strong> Send notifications to the existing participants in the room to
     * inform that streaming from this endpoint has ended.
     *
     * @param participantId identifier of the participant
     * @throws RoomException on error
     */
    void unpublishMedia(String participantId, final String streamId) throws RoomException;

    /**
     * Represents a client's request to receive media from room participants that published their
     * media. Will have the same result when a publisher requests its own media stream.<br/>
     * <strong>Dev advice:</strong> Answer to the peer's request by sending it the SDP answer
     * generated by the the receiving WebRTC endpoint on the server.
     *
     * @param remoteName    identification of the remote stream which is effectively the peer's
     *                      name (participant)
     * @param sdpOffer      SDP offer String generated by the client's WebRTC peer
     * @param participantId identifier of the participant
     * @return the SDP answer generated by the receiving WebRTC endpoint on the server
     * @throws RoomException on error
     */
    String subscribe(String remoteName, final String streamId, String sdpOffer, String participantId) throws RoomException;

    /**
     * Represents a client's request to stop receiving media from the remote peer.
     *
     * @param remoteName    identification of the remote stream which is effectively the peer's
     *                      name (participant)
     * @param participantId identifier of the participant
     * @throws RoomException on error
     */
    void unsubscribe(String remoteName, String participantId, final String streamId) throws RoomException;

    /**
     * Request that carries info about an ICE candidate gathered on the client side. This information
     * is required to implement the trickle ICE mechanism. Should be triggered or called whenever an
     * icecandidate event is created by a RTCPeerConnection.
     *
     * @param endpointName  the name of the peer whose ICE candidate was gathered
     * @param candidate     the candidate attribute information
     * @param sdpMLineIndex the index (starting at zero) of the m-line in the SDP this candidate is
     *                      associated
     *                      with
     * @param sdpMid        media stream identification, "audio" or "video", for the m-line this
     *                      candidate is
     *                      associated with
     * @param participantId identifier of the participant
     * @throws RoomException on error
     */
    void onIceCandidate(String endpointName, final String streamId, String candidate, int sdpMLineIndex, String sdpMid, String participantId) throws RoomException;

    /**
     * Mutes the streamed media of this publisher in a selective manner.
     *
     * @param muteType      which leg should be disconnected (audio, video or both)
     * @param participantId identifier of the participant
     * @throws RoomException in case the participant doesn't exist, has been closed, is not
     *                       publishing or on error
     *                       when performing the mute operation
     */
    void mutePublishedMedia(MutedMediaType muteType, String participantId, final String streamId) throws RoomException;

    /**
     * Reverts the effects of the mute operation.
     *
     * @param participantId identifier of the participant
     * @throws RoomException in case the participant doesn't exist, has been closed, is not
     *                       publishing or on error
     *                       when reverting the mute operation
     */
    void unmutePublishedMedia(String participantId, final String streamId) throws RoomException;

    /**
     * Mutes the incoming media stream from the remote publisher in a selective manner.
     *
     * @param remoteName    identification of the remote stream which is effectively the peer's
     *                      name (participant)
     * @param muteType      which leg should be disconnected (audio, video or both)
     * @param participantId identifier of the participant
     * @throws RoomException in case the participant doesn't exist, has been closed, is not
     *                       publishing or on error
     *                       when performing the mute operation
     */
    void muteSubscribedMedia(String remoteName, final String streamId, MutedMediaType muteType, String participantId) throws RoomException;

    /**
     * Reverts any previous mute operation.
     *
     * @param remoteName    identification of the remote stream which is effectively the peer's
     *                      name (participant)
     * @param participantId identifier of the participant
     * @throws RoomException in case the participant doesn't exist, has been closed or on error
     *                       when reverting the
     *                       mute operation
     */
    void unmuteSubscribedMedia(String remoteName, final String streamId, String participantId) throws RoomException;

    // ----------------- ADMIN (DIRECT or SERVER-SIDE) REQUESTS ------------

    /**
     * Closes all resources. This method has been annotated with the @PreDestroy directive
     * (javax.annotation package) so that it will be automatically called when the IRoomManager
     * instance is container-managed. <br/>
     * <strong>Dev advice:</strong> Send notifications to all participants to inform that their room
     * has been forcibly closed.
     *
     * @see IRoomManager#closeRoom(String)
     */
    @PreDestroy
    void close();

    /**
     * @return true after {@link #close()} has been called
     */
    boolean isClosed();

    /**
     * Returns all currently active (opened) rooms.
     *
     * @return set of the rooms' identifiers (names)
     */
    Set<String> getRooms();

    /**
     * Returns all the participants inside a room.
     *
     * @param roomName name or identifier of the room
     * @return set of {@link UserParticipant} POJOS (an instance contains the participant's identifier
     * and her user name)
     * @throws RoomException in case the room doesn't exist
     */
    Set<UserParticipant> getParticipants(String roomName) throws RoomException;

    /**
     * Returns all the publishers (participants streaming their media) inside a room.
     *
     * @param roomName name or identifier of the room
     * @return set of {@link UserParticipant} POJOS representing the existing publishers
     * @throws RoomException in case the room doesn't exist
     */
    Set<UserParticipant> getPublishers(String roomName) throws RoomException;

    /**
     * Returns all the subscribers (participants subscribed to a least one stream of another user)
     * inside a room. A publisher which subscribes to its own stream (loopback) and will not be
     * included in the returned values unless it requests explicitly a connection to another user's
     * stream.
     *
     * @param roomName name or identifier of the room
     * @return set of {@link UserParticipant} POJOS representing the existing subscribers
     * @throws RoomException in case the room doesn't exist
     */
    Set<UserParticipant> getSubscribers(String roomName) throws RoomException;

    /**
     * Returns the peer's publishers (participants from which the peer is receiving media). The own
     * stream doesn't count.
     *
     * @param participantId identifier of the participant
     * @return set of {@link UserParticipant} POJOS representing the publishers this participant is
     * currently subscribed to
     * @throws RoomException in case the participant doesn't exist
     */
    Set<UserParticipant> getPeerPublishers(String participantId) throws RoomException;

    /**
     * Returns the peer's subscribers (participants towards the peer is streaming media). The own
     * stream doesn't count.
     *
     * @param participantId identifier of the participant
     * @return set of {@link UserParticipant} POJOS representing the participants subscribed to this
     * peer
     * @throws RoomException in case the participant doesn't exist
     */
    Set<UserParticipant> getPeerSubscribers(String participantId) throws RoomException;

    /**
     * Checks if a participant is currently streaming media.
     *
     * @param participantId identifier of the participant
     * @return true if the participant is streaming media, false otherwise
     * @throws RoomException in case the participant doesn't exist or has been closed
     */
    boolean isPublisherStreaming(String participantId) throws RoomException;

    /**
     * Creates a room if it doesn't already exist. The room's name will be indicated by the session
     * info bean.
     *
     * @param kcSessionInfo bean that will be passed to the {@link KurentoClientProvider} in order
     *                      to obtain the
     *                      {@link KurentoClient} that will be used by the room
     * @throws RoomException in case of error while creating the room
     */
    void createRoom(KurentoClientSessionInfo kcSessionInfo) throws RoomException;

    /**
     * Closes an existing room by releasing all resources that were allocated for the room. Once
     * closed, the room can be reopened (will be empty and it will use another Media Pipeline).
     * Existing participants will be evicted. <br/>
     * <strong>Dev advice:</strong> The room event handler should send notifications to the existing
     * participants in the room to inform that the room was forcibly closed.
     *
     * @param roomName name or identifier of the room
     * @return set of {@link UserParticipant} POJOS representing the room's participants
     * @throws RoomException in case the room doesn't exist or has been already closed
     */
    Set<UserParticipant> closeRoom(String roomName) throws RoomException;

    /**
     * Returns the media pipeline used by the participant.
     *
     * @param participantId identifier of the participant
     * @return the Media Pipeline object
     * @throws RoomException in case the participant doesn't exist
     */
    MediaPipeline getPipeline(String participantId) throws RoomException;

    /**
     * Finds the room's name of a given participant.
     *
     * @param participantId identifier of the participant
     * @return the name of the room
     * @throws RoomException in case the participant doesn't exist
     */
    String getRoomName(String participantId) throws RoomException;

    /**
     * Finds the participant's username.
     *
     * @param participantId identifier of the participant
     * @return the participant's name
     * @throws RoomException in case the participant doesn't exist
     */
    String getParticipantName(String participantId) throws RoomException;

    /**
     * Searches for the participant using her identifier and returns the corresponding
     * {@link UserParticipant} POJO.
     *
     * @param participantId identifier of the participant
     * @return {@link UserParticipant} POJO containing the participant's name and identifier
     * @throws RoomException in case the participant doesn't exist
     */
    UserParticipant getParticipantInfo(String participantId) throws RoomException;

    // ------------------ HELPERS ------------------------------------------

    IRoom getRoomByName(String name);
}
