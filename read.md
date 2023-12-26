## Authorisation API's

Base URL: https://expresschat-v6mg.onrender.com

       Signup - METHOD/POST  - "Base URL/signup"
       Login  - METHOD/POST  - "Base URL/login"
       Verify - METHOD/GET   - "Base URL/Verify"
       Logout - METHOD/POST  - "BASE URL/logout"
       Resend - METHOD/POST  - "BASE URL/resend"

## SocketIO Connections

Before making the connection with client,server  validates the reuqest using io.use(middleware) in which server verifies whther a valid combination of access and refresh token were provided or not.

After soon as connection is made , Server Emits 3 events to the client:

1. Below Event is fired so that user can know his id which can help him fetch his self chat(self chat always have id same as the userId)

       io.to(socket.id).emit("user-id", { userId, userName });

2. Below event is fired so that user can display the personal chatList to the user

       io.to(socketId).emit("personal-chat-list", { PersonalChatList});

3. Below event is fired so that user can display the group chatList to the user

        io.to(socketId).emit("group-chat-list", { GroupChatList});

After sending this 3 things to client, all events are carried out when a user asks for it.

### Self Chat

1. For Fetching Self chat(when user clicks on self chat), client sends:

         Socket.emit("fetch-self-chat", {});
   To which server responds with

          io.to(socketId).emit("self-chat", { Messages, Name});

2. For Sending Self Message, Client Sends:

         Socket.emit("send-self-message", { Content });
   To which server responds with nothing as there is no need for notification on self messages

### Private Chat

1. For Fetching Personal chat(when user clicks on personal chat),client sends:

       Socket.emit("fetch-personal-chat", { ChatId, Partner });
   To which server responds with

       io.to(socketId).emit("personal-chat", {  Messages, UserId, User, Partner });
2. For Sending Personal msg, client sends:

       Socket.emit("send-personal-message", { ChatId, Content });
   To which user send message to the receiver (if he is online) using:

       io.to(receiver.socket).emit("receive-personal-message", {ChatId,Content,Sender});
   Receiver Reads the message using:

        Socket.on("receive-personal-message")
   When Receiver Reads msg, he notifies the server via:

        Socket.emit("read-personal-message", { ChatId});
   Server receives it and send an acknowlegment through:

        io.to(sender.socket).emit("read-personal-msg-ack", { ChatId});

### Group Chat

All command of sending and reading msgs are similar to personal chat, list about different functionalities of group are given below:

1. When a user wants to create a group:

        Socket.emit("create-group-chat", {Name,Description,Participants});

2. When admin adds a new member to group, admin send:

        Socket.emit("add-member",{GroupId,Member})

3.  When admin wants someone to kickout of the group:

         Socket.emit("kickout", { GroupId, Member });
4.  When admin want to make someone else as admin:

          Socket.emit("change-admin", { GroupId, Member});
5.  When any user wants to leave a group:

           Socket.emit("leave-grp", { GroupId: activeId });

      
