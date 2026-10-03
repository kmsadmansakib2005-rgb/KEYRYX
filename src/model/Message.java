package model;

public class Message {
   private final String sender;
   private final String receiver;
   private final String content;
   private final String timeStamp; /* timeStmp is used to understand
                      in which time the message
                      is sent or received*/

   public Message(String sender, String receiver, String content, String timeStamp)
   {
       this.sender=sender;
       this.receiver= receiver;
       this.content= content;
       this.timeStamp= timeStamp;
   }

            public String getSender()
            {
                return sender;
            }

            public String getReceiver()
            {
                return receiver;
            }

            public String getContent()
            {
                return content;
            }

            public String getTimeStamp()
            {
                return timeStamp;
            }

}