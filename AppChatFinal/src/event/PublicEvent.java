/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package event;

/**
 *
 * @author Admin
 */
public class PublicEvent {
      private static PublicEvent instance;
    private EventMain eventMain;
    private EventImageView eventImageView;
    private EventChat eventChat;
    private EventLogin eventLogin;
    private EventMenuLeft evenMenuLeft;
    private EventMenuRight eventMenuRight;
    private EventFriendRequest eventFriendRequest;
    
    public static PublicEvent getInstance(){
      if(instance == null){
      instance = new PublicEvent(); 
      }
    return instance;
    }
    private PublicEvent(){
    
    
    }
    public void addEventMain(EventMain event){
        this.eventMain = event;
    }
    public EventMain getEventMain(){
        return eventMain;
    }
    public void addEventImageView(EventImageView event){
        this.eventImageView = event;
    }
    public EventImageView getEventImageView(){
        return eventImageView;
    }
    public void addEventLogin(EventLogin event){
        this.eventLogin = event;
    
    }
    public void addEventMenuLeft(EventMenuLeft event){
        this.evenMenuLeft = event;
    }
    public EventMenuLeft getEventMenuLeft(){
        return evenMenuLeft;
    }
    public EventLogin getEventLogin(){
        return eventLogin;
    }
    public void addEventChat(EventChat event){
        this.eventChat = event;
    
    }
    public EventChat getEventChat(){
        return eventChat;
    
    }
    public EventMenuRight getEventMenuRight(){
        return eventMenuRight;
    }    public void addEventMenuRight(EventMenuRight event){
        this.eventMenuRight = event;
    }
      public void addEventFriendRequest(EventFriendRequest event){
        // Only replace if we don't have an active handler or if it's different
        if (this.eventFriendRequest == null || this.eventFriendRequest != event) {
            this.eventFriendRequest = event;
            System.out.println("EventFriendRequest handler registered: " + event.getClass().getSimpleName());
        }
    }
    
    public EventFriendRequest getEventFriendRequest(){
        return eventFriendRequest;
    }
     
    
}
