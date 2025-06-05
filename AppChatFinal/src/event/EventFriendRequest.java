package event;

/**
 * Event interface for friend request operations
 * @author Admin
 */
public interface EventFriendRequest {
    /**
     * Called when a new friend request is received
     */
    public void onFriendRequestReceived();
    
    /**
     * Called when a friend request is accepted
     */
    public void onFriendRequestAccepted();
    
    /**
     * Called when a friend request is rejected
     */
    public void onFriendRequestRejected();
    
    /**
     * Called when friend request list needs to be refreshed
     */
    public void onRefreshFriendRequests();
}
